package com.minenash.seamless_loading_screen.fabric;

import com.minenash.seamless_loading_screen.OnLeaveHelper;
import com.minenash.seamless_loading_screen.SeamlessLoadingScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;

//? if >=26.1 {
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
//?} else {
/*import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
*///?}

public class SeamlessLoadingScreenFabric implements ClientModInitializer {

    //? if >=1.21.9 {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.MISC;
    //?} else {
    /*private static final String CATEGORY = "key.categories.misc";
    *///?}

    private static final KeyMapping OPEN_SETTINGS = new KeyMapping(
            "seamless_loading_screen.keybind.config", InputConstants.KEY_G, CATEGORY);

    @Override
    public void onInitializeClient() {
        //? if >=26.1 {
        KeyMappingHelper.registerKeyMapping(OPEN_SETTINGS);
        //?} else {
        /*KeyBindingHelper.registerKeyBinding(OPEN_SETTINGS);
        *///?}

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_SETTINGS.consumeClick()) SeamlessLoadingScreen.openSettingsScreen();
            OnLeaveHelper.tick();
        });

        SeamlessLoadingScreen.onInitializeClient();
    }
}
