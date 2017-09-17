package net.darkhax.friendlyfire;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = "friendlyfire", name = "Friendly Fire", version = "@VERSION@", acceptableRemoteVersions = "*", certificateFingerprint = "@FINGERPRINT@")
public class FriendlyFire {

    private static final Logger LOG = LogManager.getLogger("Friendly Fire");
    
    @EventHandler
    public void init (FMLPreInitializationEvent event) {

        MinecraftForge.EVENT_BUS.register(this);
        new ConfigHandler(event.getSuggestedConfigurationFile());
    }

    @SubscribeEvent
    public void onMobHit (LivingHurtEvent event) {

        if (event.getEntityLiving() != null && event.getSource() != null && event.getSource().getTrueSource() != null) {

            final EntityLivingBase living = event.getEntityLiving();
            final Entity source = event.getSource().getTrueSource();

            if (living instanceof IEntityOwnable) {

                if (ConfigHandler.protectPets) {

                    final Entity owner = ((IEntityOwnable) living).getOwner();

                    if (owner != null && owner.getUniqueID().equals(source.getUniqueID()) && !source.isSneaking()) {

                        event.setCanceled(true);
                        return;
                    }
                }

                else if (ConfigHandler.protectPetsFromPets && source instanceof IEntityOwnable) {

                    final boolean sameOwner = ((IEntityOwnable) living).getOwnerId().equals(((IEntityOwnable) source).getOwnerId());

                    if (sameOwner) {

                        living.setRevengeTarget(null);
                        ((EntityLivingBase) source).setRevengeTarget(null);
                        event.setCanceled(true);
                        return;
                    }
                }
            }

            if (ConfigHandler.protectBabies && living instanceof EntityAgeable && ((EntityAgeable) living).isChild() && !source.isSneaking()) {
                event.setCanceled(true);
            }
        }
    }
    
    @EventHandler
    public void onFingerprintViolation (FMLFingerprintViolationEvent event) {

        LOG.error("Invalid fingerprint detected! The file " + event.getSource().getName() + " may have been tampered with. This version will NOT be supported by the author!");
    }
}