package net.darkhax.friendlyfire;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class FriendlyFireForge {

    public FriendlyFireForge() {

        FriendlyFireCommon.init();
        MinecraftForge.EVENT_BUS.addListener(FriendlyFireForge::onEntityAttack);
        MinecraftForge.EVENT_BUS.addListener(FriendlyFireForge::onEntityHurt);
    }

    private static void onEntityAttack(LivingAttackEvent event) {

        if (FriendlyFireCommon.preventAttack(event.getEntity(), event.getSource(), event.getAmount())) {

            event.setCanceled(true);
            event.getEntity().setLastHurtByMob(null);

            if (event.getSource().getEntity() instanceof LivingEntity trueSource) {

                trueSource.setLastHurtByMob(null);
            }
        }
    }

    private static void onEntityHurt(LivingHurtEvent event) {

        if (FriendlyFireCommon.preventAttack(event.getEntity(), event.getSource(), event.getAmount())) {

            event.setCanceled(true);
            event.setAmount(0f);
            event.getEntity().setLastHurtByMob(null);

            if (event.getSource().getEntity() instanceof LivingEntity trueSource) {

                trueSource.setLastHurtByMob(null);
            }
        }
    }
}