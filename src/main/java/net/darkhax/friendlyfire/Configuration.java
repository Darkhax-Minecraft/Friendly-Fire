package net.darkhax.friendlyfire;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;

public class Configuration {
    
    private final ForgeConfigSpec spec;
    
    private final BooleanValue protectPetsFromOwner;
    private final BooleanValue protectPetsFromPets;
    private final BooleanValue protectChildren;
    private final BooleanValue reflectDamage;
    
    public Configuration() {
        
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        
        // General Configs
        builder.comment("General settings for the mod.");
        builder.push("general");
        
        builder.comment("Should pets be protected from damage dealt by their owners?");
        this.protectPetsFromOwner = builder.define("protectPetsFromOwner", true);
        
        builder.comment("Should pets be protected from damage dealt by other pets with the same owner?");
        this.protectPetsFromPets = builder.define("protectPetsFromOtherPets", true);
        
        builder.comment("Should children mobs be protected from damage?");
        this.protectChildren = builder.define("protectChildren", true);
        
        builder.comment("Should damage against friendly mobs be reflected back on the damage dealer?");
        this.reflectDamage = builder.define("reflectDamage", false);
        
        this.spec = builder.build();
    }
    
    public ForgeConfigSpec getSpec () {
        
        return this.spec;
    }
    
    public boolean shouldProtectPetsFromOwners () {
        
        return this.protectPetsFromOwner.get();
    }
    
    public boolean shouldProtectPetsFromPets () {
        
        return this.protectPetsFromPets.get();
    }
    
    public boolean shouldProtectChildren () {
        
        return this.protectChildren.get();
    }
    
    public boolean shouldReflectDamage () {
        
        return this.reflectDamage.get();
    }
}