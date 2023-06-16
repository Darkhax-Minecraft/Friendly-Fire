package net.darkhax.friendlyfire;

import net.fabricmc.api.ModInitializer;

public class FriendlyFireFabric implements ModInitializer {

    @Override
    public void onInitialize() {

        FriendlyFireCommon.init();
    }
}
