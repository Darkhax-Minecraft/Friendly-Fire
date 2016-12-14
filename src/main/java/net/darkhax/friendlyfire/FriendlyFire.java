package net.darkhax.friendlyfire;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = "friendlyfire", name = "Friendly Fire", version = "1.1.0", acceptableRemoteVersions = "*")
public class FriendlyFire {
    
    @EventHandler
    public void init (FMLPreInitializationEvent event) {
        
        MinecraftForge.EVENT_BUS.register(this);
        new ConfigHandler(event.getSuggestedConfigurationFile());
    }
    
    @SubscribeEvent
    public void onMobHit(LivingHurtEvent event) {
        
        if (event.entityLiving != null && event.source != null && event.source.getEntity() != null) {
            
            final EntityLivingBase living = event.entityLiving;
            final Entity source = event.source.getEntity();
            
            if (ConfigHandler.protectPets && living instanceof IEntityOwnable) {
                
                final Entity owner = ((IEntityOwnable) living).getOwner();
                
                if (owner != null && owner.getUniqueID().equals(source.getUniqueID()) && !source.isSneaking())
                    event.setCanceled(true);
            }
            
            if (ConfigHandler.protectBabies && living instanceof EntityAgeable && ((EntityAgeable) living).isChild() && !source.isSneaking() && ConfigHandler.protectPets)
                event.setCanceled(true);
        }
    }
}
