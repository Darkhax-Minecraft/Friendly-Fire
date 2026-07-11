package net.darkhax.friendlyfire.common.mixin;

import net.darkhax.friendlyfire.common.FriendlyFire;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntity.class, Wolf.class, Parrot.class})
public class MixinLivingEntity {

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void onLivingHurt(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
        if (FriendlyFire.preventAttack((LivingEntity) (Object) this, source)) {
            cir.setReturnValue(false);
        }
    }
}