package com.minenash.seamless_loading_screen;

import com.minenash.seamless_loading_screen.config.SeamlessLoadingScreenConfig;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Pattern;

public class ScreenshotLoader {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Pattern RESERVED_FILENAMES_PATTERN = Pattern.compile(".*\\.|(?:COM|CLOCK\\$|CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\..*)?", Pattern.CASE_INSENSITIVE);
    private static final char[] ILLEGAL_FILE_CHARACTERS = {'/', '\n', '\r', '\t', '\u0000', '\f', '`', '?', '*', '\\', '<', '>', '|', '"', ':'};

    public static final Identifier SCREENSHOT = Identifier.fromNamespaceAndPath(SeamlessLoadingScreen.MODID, "screenshot");

    public static double imageRatio = 1;
    public static boolean loaded = false;
    public static DisplayMode displayMode = DisplayMode.ENABLED;
    /** True from the moment a screenshot is loaded for a join until the fade-in has finished. */
    public static boolean replaceBackground = false;

    private static Path filePath = null;

    public static Path getFilePath() {
        return filePath;
    }

    public static void setScreenshot(String address, int port) {
        setFile("servers", cleanFileName(address) + "_" + port);
    }

    public static void setScreenshot(String worldName) {
        displayMode = DisplayMode.ENABLED;
        setFile("singleplayer", cleanFileName(worldName));
    }

    public static void setRealmScreenshot(String realmName) {
        displayMode = DisplayMode.ENABLED;
        setFile("realms", cleanFileName(realmName));
    }

    private static void setFile(String category, String name) {
        Path base = FabricLoader.getInstance().getGameDir().resolve("screenshots");

        if (SeamlessLoadingScreenConfig.get().saveScreenshotsByUsername) {
            base = base.resolve(userDirectory());
        }

        filePath = base.resolve("worlds").resolve(category).resolve(name + ".png");
        loadScreenshot();
    }

    private static String userDirectory() {
        var user = Minecraft.getInstance().getUser();
        UUID offlineUUID = UUIDUtil.createOfflinePlayerUUID(user.getName());
        UUID sessionUUID = user.getProfileId();

        return sessionUUID != null && !sessionUUID.equals(offlineUUID) ? cleanFileName(user.getName()) : "offline";
    }

    private static void loadScreenshot() {
        loaded = false;
        replaceBackground = false;

        if (displayMode == DisplayMode.DISABLED) return;

        try (InputStream in = Files.newInputStream(filePath)) {
            if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                LOGGER.info("[SeamlessLoadingScreen] Loading screenshot {}", filePath);
            }

            NativeImage image = NativeImage.read(in);
            imageRatio = image.getWidth() / (double) image.getHeight();

            //? if >=1.21.5 {
            DynamicTexture texture = new DynamicTexture(() -> "Seamless Loading Screen screenshot", image);
            //?} else {
            /*DynamicTexture texture = new DynamicTexture(image);
            *///?}
            Minecraft.getInstance().getTextureManager().register(SCREENSHOT, texture);

            loaded = true;
            replaceBackground = true;
        } catch (NoSuchFileException ignore) {
        } catch (IOException e) {
            LOGGER.error("[SeamlessLoadingScreen] Unable to load the screenshot {}", filePath, e);
        }
    }

    public static void endBackgroundReplacement() {
        replaceBackground = false;
    }

    private static String cleanFileName(String fileName) {
        for (char c : ILLEGAL_FILE_CHARACTERS) fileName = fileName.replace(c, '_');

        if (RESERVED_FILENAMES_PATTERN.matcher(fileName).matches()) fileName = "_" + fileName + "_";

        if (fileName.length() > 255 - 4) fileName = fileName.substring(0, 255 - 4);

        return fileName;
    }

    /**
     * Draws the screenshot centered over the whole screen, with the configured blur and tint.
     */
    public static void render(GuiGraphicsExtractor graphics, int width, int height, float alpha, float partialTick) {
        int w = (int) (imageRatio * height);
        int x = width / 2 - w / 2;

        if (w < width) Gfx.fill(graphics, 0, 0, width, height, Gfx.argb(alpha, 0x000000));
        Gfx.blit(graphics, SCREENSHOT, x, 0, w, height, alpha);

        var config = SeamlessLoadingScreenConfig.get();
        if (config.enableScreenshotBlur) Gfx.blur(graphics, partialTick);

        Color color = config.tintColor;
        Gfx.fill(graphics, 0, 0, width, height, Gfx.argb(config.tintStrength * alpha, color.getRGB()));
    }
}
