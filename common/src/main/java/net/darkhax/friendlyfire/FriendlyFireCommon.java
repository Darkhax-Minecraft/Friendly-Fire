package net.darkhax.friendlyfire;

import net.darkhax.bookshelf.api.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.io.File;
import java.util.UUID;

public class FriendlyFireCommon {

    private static final TagKey<Item> BYPASS_PET = TagKey.create(Registries.ITEM, new ResourceLocation("friendlyfire", "bypass_pet"));
    private static final TagKey<Item> BYPASS_ALL = TagKey.create(Registries.ITEM, new ResourceLocation("friendlyfire", "bypass_all_protection"));
    private static final TagKey<EntityType<?>> GENERAL_PROTECTION = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("friendlyfire", "general_protection"));
    private static final TagKey<EntityType<?>> PLAYER_PROTECTION = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("friendlyfire", "player_protection"));
    private static final TagKey<EntityType<?>> BYPASSED_PROTECTION = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("friendlyfire", "bypassed_entity_types"));

    private static final Config CONFIG = Config.load(new File(Services.PLATFORM.getConfigDirectory(), "friendlyfire.json"));

    public static void init() {

        Constants.LOG.debug("Protect children = {}", CONFIG.protectChildren);
        Constants.LOG.debug("Protect pets from owner = {}", CONFIG.protectPetsFromOwner);
        Constants.LOG.debug("Protect pets from pets = {}", CONFIG.protectPetsFromPets);
        Constants.LOG.debug("Reflect damage = {}", CONFIG.reflectDamage);
    }

    public static boolean preventAttack(Entity target, DamageSource source, float amount) {

        final Entity attacker = source.getEntity();
        final boolean preventDamage = source != null && isProtected(target, attacker, amount);

        if (preventDamage && attacker instanceof ServerPlayer player && CONFIG.displayHitWarning) {

            player.displayClientMessage(Component.translatable("notif.friendlyfire.protected", target.getName()), true);
        }

        return preventDamage;
    }

    private static boolean isProtected(Entity target, Entity attacker, float amount) {

        if (target.getType().is(BYPASSED_PROTECTION)) {

            return false;
        }

        // Null targets or sources can not be protected. Sneaking will bypass this mod
        // entirely.
        if (target == null || attacker == null || attacker.isCrouching()) {

            return false;
        }

        // The item used by the attacker.
        final ItemStack heldItem = attacker instanceof LivingEntity attackerLiving ? attackerLiving.getMainHandItem() : ItemStack.EMPTY;

        // Items in the bypass all tag will always cause damage.
        if (heldItem.is(BYPASS_ALL)) {

            return false;
        }

        // Mobs with general protection tag are almost always protected.
        if (target.getType().is(GENERAL_PROTECTION)) {

            return true;
        }

        // Mobs with player protection are protected from players.
        if (attacker instanceof Player player && target.getType().is(PLAYER_PROTECTION)) {

            return true;
        }

        // Gets the pet owner ID, will be null if not a pet mob.
        final UUID ownerId = getOwner(target);

        if (ownerId != null && !heldItem.is(BYPASS_PET)) {

            // Protects owners from hurting their pets.
            if (CONFIG.protectPetsFromOwner && ownerId.equals(attacker.getUUID())) {

                // Reflection causes players to hurt themselves instead.
                if (CONFIG.reflectDamage) {

                    attacker.hurt(attacker.level().damageSources().generic(), amount);
                }

                return true;
            }

            // Protect pets from pets with the same owner.
            else if (CONFIG.protectPetsFromPets && ownerId.equals(getOwner(attacker))) {

                return true;
            }
        }

        // Check if child mobs can be killed.
        if (CONFIG.protectChildren && !(target instanceof Enemy) && target instanceof AgeableMob agable && agable.isBaby() && !attacker.isCrouching()) {

            return true;
        }

        return false;
    }

    @Nullable
    private static UUID getOwner(Entity entity) {

        if (entity instanceof OwnableEntity ownable) {

            return ownable.getOwnerUUID();
        }

        // Thanks Mojang
        if (entity instanceof AbstractHorse horse) {

            return horse.getOwnerUUID();
        }

        return null;
    }
}