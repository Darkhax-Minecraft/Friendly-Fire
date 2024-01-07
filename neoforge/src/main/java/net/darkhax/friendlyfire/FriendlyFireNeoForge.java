package net.darkhax.friendlyfire;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;
import net.neoforged.neoforge.event.entity.living.LivingHurtEvent;

@Mod(Constants.MOD_ID)
public class FriendlyFireNeoForge {

    public FriendlyFireNeoForge() {

        FriendlyFireCommon.init();
        NeoForge.EVENT_BUS.addListener(FriendlyFireNeoForge::onEntityAttack);
        NeoForge.EVENT_BUS.addListener(FriendlyFireNeoForge::onEntityHurt);
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