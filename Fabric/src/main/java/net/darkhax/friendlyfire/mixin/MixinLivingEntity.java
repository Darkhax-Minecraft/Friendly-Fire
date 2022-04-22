package net.darkhax.friendlyfire.mixin;

import net.darkhax.friendlyfire.FriendlyFireCommon;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {

    @Inject(method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At("HEAD"), cancellable = true)
    private void onLivingHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cbi) {

        LivingEntity self = (LivingEntity) (Object) this;

        if (FriendlyFireCommon.preventAttack(self, source, amount)) {

            self.setLastHurtByMob(null);

            if (source.getEntity() instanceof LivingEntity trueSource) {

                trueSource.setLastHurtByMob(null);
            }

            cbi.setReturnValue(false);
        }
    }
}