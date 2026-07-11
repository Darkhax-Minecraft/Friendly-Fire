package net.darkhax.friendlyfire.common;

import net.darkhax.pricklemc.common.api.annotations.Value;

public class Config {

    @Value(comment = "Should the mod prevent owners from attacking their pets?")
    public boolean protectPetsFromOwner = true;

    @Value(comment = "Should the mod prevent pets from attacking other pets with the same owner?")
    public boolean protectPetsFromPets = true;

    @Value(comment = "Should the mod prevent non-hostile child/baby mods from being hurt?")
    public boolean protectChildren = false;

    @Value(comment = "Should players see a warning message when attacking protected mobs?")
    public boolean displayHitWarning = true;
}