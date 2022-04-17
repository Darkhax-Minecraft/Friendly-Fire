package net.darkhax.friendlyfire;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fmllegacy.network.FMLNetworkConstants;

import javax.annotation.Nullable;
import java.util.UUID;

@Mod("friendlyfire")
public class FriendlyFire {

    public static final Tags.IOptionalNamedTag<Item> BYPASS_PET = ItemTags.createOptional(new ResourceLocation("friendlyfire", "bypass_pet"));
    public static final Tags.IOptionalNamedTag<Item> BYPASS_ALL = ItemTags.createOptional(new ResourceLocation("friendlyfire", "bypass_all_protection"));
    public static final Tags.IOptionalNamedTag<EntityType<?>> GENERAL_PROTECTION = EntityTypeTags.createOptional(new ResourceLocation("friendlyfire", "general_protection"));

    private final Configuration configuration = new Configuration();

    public FriendlyFire() {

        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        MinecraftForge.EVENT_BUS.addListener(this::onEntityAttack);
        MinecraftForge.EVENT_BUS.addListener(this::onEntityHurt);
        ModLoadingContext.get().registerConfig(Type.COMMON, this.configuration.getSpec());
    }

    private void onEntityAttack(LivingAttackEvent event) {

        if (this.preventAttack(event.getEntityLiving(), event.getSource(), event.getAmount())) {

            event.setCanceled(true);
            event.getEntityLiving().setLastHurtByMob(null);

            final Entity trueSource = event.getSource().getEntity();

            if (trueSource instanceof LivingEntity) {

                ((LivingEntity) trueSource).setLastHurtByMob(null);
            }
        }
    }

    private void onEntityHurt(LivingHurtEvent event) {

        if (this.preventAttack(event.getEntityLiving(), event.getSource(), event.getAmount())) {

            event.setCanceled(true);
            event.setAmount(0f);
            event.getEntityLiving().setLastHurtByMob(null);

            final Entity trueSource = event.getSource().getEntity();

            if (trueSource instanceof LivingEntity) {

                ((LivingEntity) trueSource).setLastHurtByMob(null);
            }
        }
    }

    private boolean preventAttack(Entity living, DamageSource source, float amount) {

        return source != null && this.preventAttack(living, source.getEntity(), amount);
    }

    @Nullable
    private UUID getOwner(Entity entity) {

        if (entity instanceof OwnableEntity ownable) {

            return ownable.getOwnerUUID();
        }

        // Seriously Mojang
        if (entity instanceof AbstractHorse horse) {

            return horse.getOwnerUUID();
        }

        return null;
    }

    private boolean preventAttack(Entity living, Entity source, float amount) {

        // Null targets or sources can not be protected. Sneaking will bypass this mod
        // entirely.
        if (living == null || source == null || source.isCrouching()) {

            return false;
        }

        // The item used by the attacker.
        final ItemStack heldItem = source instanceof LivingEntity ? ((LivingEntity) source).getMainHandItem() : ItemStack.EMPTY;

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
            if (this.configuration.shouldProtectPetsFromOwners() && ownerId.equals(source.getUUID())) {

                // Reflection causes players to hurt themselves instead.
                if (this.configuration.shouldReflectDamage()) {

                    source.hurt(DamageSource.GENERIC, amount);
                }

                return true;
            }

            // Protect pets from pets with the same owner.
            else if (this.configuration.shouldProtectPetsFromPets() && ownerId.equals(this.getOwner(source))) {

                return true;
            }
        }

        // Check if child mobs can be killed.
        if (this.configuration.shouldProtectChildren() && !(living instanceof Enemy) && living instanceof AgeableMob agable && agable.isBaby() && !source.isCrouching()) {

            return true;
        }

        return false;
    }
}