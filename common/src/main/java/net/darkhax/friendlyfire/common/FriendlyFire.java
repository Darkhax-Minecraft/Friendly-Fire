package net.darkhax.friendlyfire.common;

import net.darkhax.pricklemc.common.api.config.ConfigManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FriendlyFire {

    public static final String MOD_ID = "friendlyfire";
    public static final String MOD_NAME = "Friendly Fire";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    /**
     * Items in this tag always bypass protection offered by this mod.
     */
    private static final TagKey<Item> BYPASS_ALL = TagKey.create(Registries.ITEM, id("bypass_all_protection"));

    /**
     * Items in this tag always bypass protections when attacking pets.
     */
    private static final TagKey<Item> BYPASS_PET = TagKey.create(Registries.ITEM, id("bypass_pet"));

    /**
     * Mobs in this tag are never protected.
     */
    private static final TagKey<EntityType<?>> UNPROTECTED = TagKey.create(Registries.ENTITY_TYPE, id("bypassed_entity_types"));

    /**
     * Mobs in this tag are always protected from other entities.
     */
    private static final TagKey<EntityType<?>> GENERAL_PROTECTION = TagKey.create(Registries.ENTITY_TYPE, id("general_protection"));

    /**
     * Mobs in this tag are always protected from players.
     */
    private static final TagKey<EntityType<?>> PLAYER_PROTECTION = TagKey.create(Registries.ENTITY_TYPE, id("player_protection"));

    private static final Config CONFIG = ConfigManager.load(MOD_ID, new Config());

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void init() {
        FriendlyFire.LOG.debug("Protect children = {}", CONFIG.protectChildren);
        FriendlyFire.LOG.debug("Protect pets from owner = {}", CONFIG.protectPetsFromOwner);
        FriendlyFire.LOG.debug("Protect pets from pets = {}", CONFIG.protectPetsFromPets);
    }

    public static boolean preventAttack(Entity target, DamageSource source) {
        if (target != null && source != null && source.getEntity() instanceof LivingEntity livingAttacker) {
            final boolean preventDamage = shouldPreventAttack(target, livingAttacker);
            if (preventDamage && livingAttacker instanceof ServerPlayer player && CONFIG.displayHitWarning) {
                player.sendSystemMessage(Component.translatable("notif.friendlyfire.protected", target.getName()), true);
            }
            return preventDamage;
        }
        return false;
    }

    private static boolean shouldPreventAttack(Entity victim, Entity attacker) {
        // Entities in the bypass tag are not protected. Sneaking also bypasses protection.
        if (victim.is(UNPROTECTED) || attacker.isCrouching()) {
            return false;
        }

        // Always allow attacks if weapon is in a valid bypass tag.
        final ItemStack heldItem = attacker instanceof LivingEntity attackerLiving ? attackerLiving.getMainHandItem() : ItemStack.EMPTY;
        if (heldItem.is(BYPASS_ALL) || (isTamed(victim) && heldItem.is(BYPASS_PET))) {
            return false;
        }

        // Protect mobs that are always protected, or protected from players.
        if (victim.is(GENERAL_PROTECTION) || (attacker instanceof Player && victim.is(PLAYER_PROTECTION))) {
            return true;
        }

        // Protects owners from hurting their pets.
        if (CONFIG.protectPetsFromOwner && isOwner(attacker, victim)) {
            return true;
        }

        // Protect pets from pets with the same owner.
        if (CONFIG.protectPetsFromPets && sameOwner(attacker, victim)) {
            return true;
        }

        // Check if child mobs can be killed.
        if (CONFIG.protectChildren && attacker instanceof Player && !(victim instanceof Enemy) && victim instanceof AgeableMob ageable && ageable.isBaby()) {
            return true;
        }

        return false;
    }

    private static boolean isTamed(Entity e) {
        return e instanceof OwnableEntity ownable && ownable.getOwnerReference() != null;
    }

    private static boolean isOwner(Entity attacker, Entity victim) {
        return isTamed(victim) && victim instanceof OwnableEntity ownable && (attacker == ownable.getOwner() || attacker == ownable.getRootOwner());
    }

    private static boolean sameOwner(Entity aEntity, Entity bEntity) {
        if (aEntity instanceof OwnableEntity a && bEntity instanceof OwnableEntity b) {
            final Entity aOwner = a.getOwner();
            final Entity aRoot = a.getRootOwner();
            final Entity bOwner = b.getOwner();
            final Entity bRoot = b.getRootOwner();
            return (aOwner != null && (aOwner == bOwner || aOwner == bRoot)) || (aRoot != null && (aRoot == bOwner || aRoot == bRoot));
        }
        return false;
    }
}