package net.darkhax.friendlyfire.common.mixin;

import net.darkhax.friendlyfire.common.FriendlyFire;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntity.class})
public class MixinLivingEntity {

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void onLivingHurt(ServerLevel level, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (FriendlyFire.preventAttack((LivingEntity) (Object) this, source)) {
            cir.setReturnValue(true);
        }
    }
}