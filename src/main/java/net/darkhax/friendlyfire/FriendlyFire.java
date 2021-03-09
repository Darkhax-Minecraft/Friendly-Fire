package net.darkhax.friendlyfire;

import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.network.FMLNetworkConstants;

@Mod("friendlyfire")
public class FriendlyFire {
    
    public static final INamedTag<Item> BYPASS_PET = ItemTags.makeWrapperTag("friendlyfire:bypass_pet");
    public static final INamedTag<Item> BYPASS_ALL = ItemTags.makeWrapperTag("friendlyfire:bypass_all_protection");
    public static final INamedTag<EntityType<?>> GENERAL_PROTECTION = EntityTypeTags.getTagById("friendlyfire:general_protection");
    
    private final Configuration configuration = new Configuration();
    
    public FriendlyFire() {
        
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of( () -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        MinecraftForge.EVENT_BUS.addListener(this::onEntityAttack);
        MinecraftForge.EVENT_BUS.addListener(this::onEntityHurt);
        ModLoadingContext.get().registerConfig(Type.COMMON, this.configuration.getSpec());
    }
    
    private final void onEntityAttack (LivingAttackEvent event) {
        
        if (this.preventAttack(event.getEntityLiving(), event.getSource(), event.getAmount())) {
            
            event.setCanceled(true);
            event.getEntityLiving().setRevengeTarget(null);
            
            final Entity trueSource = event.getSource().getTrueSource();
            
            if (trueSource instanceof LivingEntity) {
                
                ((LivingEntity) trueSource).setRevengeTarget(null);
            }
        }
    }
    
    private final void onEntityHurt (LivingHurtEvent event) {
        
        if (this.preventAttack(event.getEntityLiving(), event.getSource(), event.getAmount())) {
            
            event.setCanceled(true);
            event.setAmount(0f);
            event.getEntityLiving().setRevengeTarget(null);
            
            final Entity trueSource = event.getSource().getTrueSource();
            
            if (trueSource instanceof LivingEntity) {
                
                ((LivingEntity) trueSource).setRevengeTarget(null);
            }
        }
    }
    
    private final boolean preventAttack (Entity living, DamageSource source, float amount) {
        
        return source != null ? this.preventAttack(living, source.getTrueSource(), amount) : false;
    }
    
    @Nullable
    private UUID getOwner (Entity entity) {
        
        if (entity instanceof TameableEntity) {
            
            return ((TameableEntity) entity).getOwnerId();
        }
        
        if (entity instanceof AbstractHorseEntity) {
            
            return ((AbstractHorseEntity) entity).getOwnerUniqueId();
        }
        
        return null;
    }
    
    private final boolean preventAttack (Entity living, Entity source, float amount) {
        
        // Null targets or sources can not be protected. Sneaking will bypass this mod
        // entirely.
        if (living == null || source == null || source.isSneaking()) {
            
            return false;
        }
        
        // The item used by the attacker.
        final ItemStack heldItem = source instanceof LivingEntity ? ((LivingEntity) source).getHeldItemMainhand() : ItemStack.EMPTY;
        
        // Items in the bypass all tag will always cause damage.
        if (BYPASS_ALL.contains(heldItem.getItem())) {
            
            return false;
        }
        
        // Mobs with general protection tag are almost always protected.
        if (GENERAL_PROTECTION.contains(living.getType())) {
            
            return true;
        }
        
        // Gets the pet owner ID, will be null if not a pet mob.
        final UUID ownerId = this.getOwner(living);
        
        if (ownerId != null && !BYPASS_PET.contains(heldItem.getItem())) {
            
            // Protects owners from hurting their pets.
            if (this.configuration.shouldProtectPetsFromOwners() && ownerId.equals(source.getUniqueID())) {
                
                // Reflection causes players to hurt themselves instead.
                if (this.configuration.shouldReflectDamage()) {
                    
                    source.attackEntityFrom(DamageSource.GENERIC, amount);
                }
                
                return true;
            }
            
            // Protect pets from pets with the same owner.
            else if (this.configuration.shouldProtectPetsFromPets() && ownerId.equals(this.getOwner(source))) {
                
                return true;
            }
        }
        
        // Check if child mobs can be killed.
        if (this.configuration.shouldProtectChildren() && !(living instanceof IMob) && living instanceof AgeableEntity && ((AgeableEntity) living).isChild() && !source.isSneaking()) {
            
            return true;
        }
        
        return false;
    }
}