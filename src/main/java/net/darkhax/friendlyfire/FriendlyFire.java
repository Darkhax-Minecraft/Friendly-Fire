package net.darkhax.friendlyfire;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;

@Mod("friendlyfire")
public class FriendlyFire {
    
	private Configuration configuration = new Configuration();
	
    public FriendlyFire() {

    	MinecraftForge.EVENT_BUS.addListener(this::onEntityAttack);
    	MinecraftForge.EVENT_BUS.addListener(this::onEntityHurt);
    	ModLoadingContext.get().registerConfig(Type.COMMON, configuration.getSpec());
    }
    
    private final void onEntityAttack (LivingAttackEvent event) {
    	
    	if (preventAttack(event.getEntityLiving(), event.getSource(), event.getAmount())) {
    		
    		event.setCanceled(true);
    		event.getEntityLiving().setRevengeTarget(null);
    		
    		final Entity trueSource = event.getSource().getTrueSource();
    		
    		if (trueSource instanceof LivingEntity) {
    			
    			((LivingEntity) trueSource).setRevengeTarget(null);
    		}
    	}
    }
    
    private final void onEntityHurt (LivingHurtEvent event) {

    	if (preventAttack(event.getEntityLiving(), event.getSource(), event.getAmount())) {
    		
    		event.setCanceled(true);
    		event.setAmount(0f);
    		event.getEntityLiving().setRevengeTarget(null);
    		
    		final Entity trueSource = event.getSource().getTrueSource();
    		
    		if (trueSource instanceof LivingEntity) {
    			
    			((LivingEntity) trueSource).setRevengeTarget(null);
    		}
    	}
    }
    
    private final boolean preventAttack(Entity living, DamageSource source, float amount) {
    	
    	return source != null ? preventAttack(living, source.getTrueSource(), amount) : false;
    }
    
    private final boolean preventAttack(Entity living, Entity source, float amount) {
    	
        // Check if the entity can be owned by the player
        if (living instanceof TameableEntity) {

        	// Checks if pets should be protected from players
            if (configuration.shouldProtectPetsFromOwners()) {

                final Entity owner = ((TameableEntity) living).getOwner();
                
                // Check if it is the owner dealing the damage, and the owner is not sneaking.
                if (owner != null && owner.getUniqueID().equals(source.getUniqueID()) && !source.isSneaking()) {
                    
                	// If reflection is set to true, the player will be damaged when attacking their pets.
                	if (configuration.shouldReflectDamage()) {
                		
                		owner.attackEntityFrom(DamageSource.GENERIC, amount);
                	}
                	
                	return true;
                }
            }

            // Check if pets should be protected from pets with the same owner.
            else if (configuration.shouldProtectPetsFromPets() && source instanceof TameableEntity) {

                final boolean sameOwner = ((TameableEntity) living).getOwnerId().equals(((TameableEntity) source).getOwnerId());

                if (sameOwner) {

                	return true;
                }
            }
        }

        // Check if child mobs can be killed.
        if (configuration.shouldProtectChildren() && living instanceof AgeableEntity && ((AgeableEntity) living).isChild() && !source.isSneaking()) {

        	return true;
        }
        
    	return false;
    }
}