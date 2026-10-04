package com.minenash.seamless_loading_screen;

import com.minenash.seamless_loading_screen.config.SeamlessLoadingScreenConfig;
import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

//? if <1.21.9 {
/*import net.minecraft.client.gui.screens.ReceivingLevelScreen;
*///?}

public class SeamlessLoadingScreen {

    public static final String MODID = "seamless_loading_screen";

    private static final Logger LOGGER = LogUtils.getLogger();

    public static void onInitializeClient() {
        SeamlessLoadingScreenConfig.load();

        try {
            Path path = FabricLoader.getInstance().getGameDir().resolve("screenshots/worlds");
            Files.createDirectories(path.resolve("singleplayer"));
            Files.createDirectories(path.resolve("servers"));
            Files.createDirectories(path.resolve("realms"));
            Files.createDirectories(path.resolve("archive"));
        } catch (IOException e) {
            LOGGER.error("[SeamlessLoadingScreen] Unable to create the screenshot directories", e);
        }
    }

    public static void openSettingsScreen() {
        setScreen(SeamlessLoadingScreenConfig.getInstance().generateScreen(currentScreen()));
    }

    public static @Nullable Screen currentScreen() {
        //? if >=26.2 {
        return Minecraft.getInstance().gui.screen();
        //?} else {
        /*return Minecraft.getInstance().screen;
        *///?}
    }

    public static void setScreen(@Nullable Screen screen) {
        //? if >=26.2 {
        Minecraft.getInstance().gui.setScreen(screen);
        //?} else {
        /*Minecraft.getInstance().setScreen(screen);
        *///?}
    }

    /**
     * Screens shown while joining a world, which get the screenshot as their background.
     */
    public static boolean isLoadingScreen(Screen screen) {
        //? if <1.21.9 {
        /*if (screen instanceof ReceivingLevelScreen) return true;
        *///?}
        return screen instanceof LevelLoadingScreen
                || screen instanceof ConnectScreen
                || screen instanceof ProgressScreen
                || screen instanceof GenericMessageScreen;
    }

    /**
     * The screen that is closed once the world is ready to be played.
     */
    public static boolean isTerrainScreen(Screen screen) {
        //? if <1.21.9 {
        /*return screen instanceof ReceivingLevelScreen;
        *///?} else {
        return screen instanceof LevelLoadingScreen;
        //?}
    }
}
