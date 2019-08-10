package net.darkhax.friendlyfire;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;

public class Configuration {

    private final ForgeConfigSpec spec;
    
    private final BooleanValue protectPetsFromOwner;
    private final BooleanValue protectPetsFromPets;
    private final BooleanValue protectChildren;
    private final BooleanValue reflectDamage;
    
    public Configuration () {

        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        // General Configs
        builder.comment("General settings for the mod.");
        builder.push("general");

        builder.comment("Should pets be protected from damage dealt by their owners?");
        protectPetsFromOwner = builder.define("protectPetsFromOwner", true);
        
        builder.comment("Should pets be protected from damage dealt by other pets with the same owner?");
        protectPetsFromPets = builder.define("protectPetsFromOtherPets", true);
        
        builder.comment("Should children mobs be protected from damage?");
        protectChildren = builder.define("protectChildren", true);
        
        builder.comment("Should damage against friendly mobs be reflected back on the damage dealer?");
        reflectDamage = builder.define("reflectDamage", false);
        
        this.spec = builder.build();
    }
    
    public ForgeConfigSpec getSpec() {
    	
    	return this.spec;
    }
    
    public boolean shouldProtectPetsFromOwners() {
    	
    	return protectPetsFromOwner.get();
    }

    public boolean shouldProtectPetsFromPets() {
    	
    	return protectPetsFromPets.get();
    }
    
    public boolean shouldProtectChildren() {
    	
    	return protectChildren.get();
    }
    
    public boolean shouldReflectDamage() {
    	
    	return reflectDamage.get();
    }
}