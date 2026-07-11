package net.darkhax.friendlyfire.fabric;

import net.darkhax.friendlyfire.common.FriendlyFire;
import net.fabricmc.api.ModInitializer;

public class FriendlyFireFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        FriendlyFire.init();
    }
}
