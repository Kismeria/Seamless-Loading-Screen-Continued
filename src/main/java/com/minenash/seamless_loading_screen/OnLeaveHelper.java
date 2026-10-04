package com.minenash.seamless_loading_screen;

import com.minenash.seamless_loading_screen.config.SeamlessLoadingScreenConfig;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.util.Util;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Takes the screenshot when leaving a world. Leaving is delayed by one rendered frame, so the screenshot is taken
 * right after the world is drawn and before any menu is drawn on top of it.
 */
public class OnLeaveHelper {

    private static final Logger LOGGER = LogUtils.getLogger();
    /** Leave anyway if no world frame was rendered in this time, so the game can never get stuck. */
    private static final long FRAME_TIMEOUT_MS = 2000;
    /** Leave anyway if saving the screenshot takes longer than this. */
    private static final long SAVE_TIMEOUT_MS = 10000;

    private static boolean pending = false;
    /** Set once the world frame is captured. */
    private static boolean captured = false;
    /** Set once the screenshot is written, the leave action then runs on the next client tick, outside of rendering. */
    private static volatile boolean saved = false;
    private static long pendingSince = 0;
    private static Runnable onceFinished = () -> {};
    private static CameraType oldCameraType = null;

    public static boolean isPending() {
        return pending;
    }

    /**
     * Requests a screenshot of the next rendered world frame and runs {@code runnable} afterwards.
     */
    public static void beginScreenshotTask(Runnable runnable) {
        var client = Minecraft.getInstance();

        if (pending || ScreenshotLoader.getFilePath() == null || ScreenshotLoader.displayMode != DisplayMode.ENABLED || client.level == null) {
            runnable.run();
            return;
        }

        pending = true;
        captured = false;
        saved = false;
        pendingSince = Util.getMillis();
        onceFinished = runnable;

        oldCameraType = client.options.getCameraType();
        client.options.setCameraType(CameraType.FIRST_PERSON);
    }

    /**
     * Called every client tick. Runs the leave action once the screenshot is taken, or when no world frame
     * arrives in time.
     */
    public static void tick() {
        if (!pending) return;

        long elapsed = Util.getMillis() - pendingSince;
        if (saved) {
            finish();
        } else if (!captured && elapsed > FRAME_TIMEOUT_MS) {
            LOGGER.warn("[SeamlessLoadingScreen] No world frame was rendered in time, leaving without a screenshot");
            finish();
        } else if (elapsed > SAVE_TIMEOUT_MS) {
            LOGGER.warn("[SeamlessLoadingScreen] Saving the screenshot took too long, leaving anyway");
            finish();
        }
    }

    /**
     * Called right after the world is rendered, before the GUI.
     */
    public static void onWorldRendered() {
        if (!pending || captured) return;
        captured = true;

        var client = Minecraft.getInstance();
        Path path = ScreenshotLoader.getFilePath();
        boolean updateIcon = SeamlessLoadingScreenConfig.get().updateWorldIcon && client.getSingleplayerServer() != null;
        Path iconPath = updateIcon ? client.getSingleplayerServer().getWorldScreenshotFile().orElse(null) : null;

        //? if >=26.2 {
        var target = client.gameRenderer.mainRenderTarget();
        //?} else {
        /*var target = client.getMainRenderTarget();
        *///?}

        //? if >=1.21.5 {
        Screenshot.takeScreenshot(target, image -> save(image, path, iconPath));
        //?} else {
        /*save(Screenshot.takeScreenshot(target), path, iconPath);
        *///?}
    }

    private static void finish() {
        if (!pending) return;
        pending = false;
        captured = false;

        if (oldCameraType != null) {
            Minecraft.getInstance().options.setCameraType(oldCameraType);
            oldCameraType = null;
        }

        Runnable runnable = onceFinished;
        onceFinished = () -> {};
        runnable.run();
    }

    private static void save(NativeImage image, Path path, Path iconPath) {
        Util.ioPool().execute(() -> {
            try {
                Files.createDirectories(path.getParent());
                image.writeToFile(path);

                if (SeamlessLoadingScreenConfig.get().archiveScreenshots) {
                    String name = path.getFileName().toString();
                    name = name.substring(0, name.length() - 4) + "_" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + ".png";

                    Path archive = path.getParent().getParent().resolve("archive").resolve(name);
                    Files.createDirectories(archive.getParent());
                    image.writeToFile(archive);
                }

                if (iconPath != null) writeIcon(image, iconPath);
            } catch (IOException e) {
                LOGGER.error("[SeamlessLoadingScreen] Unable to save the screenshot {}", path, e);
            } finally {
                image.close();
                saved = true;
            }
        });
    }

    private static void writeIcon(NativeImage image, Path iconPath) throws IOException {
        int width = image.getWidth();
        int height = image.getHeight();
        int x = 0;
        int y = 0;
        if (width > height) {
            x = (width - height) / 2;
            width = height;
        } else {
            y = (height - width) / 2;
            height = width;
        }

        try (NativeImage icon = new NativeImage(64, 64, false)) {
            image.resizeSubRectTo(x, y, width, height, icon);
            icon.writeToFile(iconPath);
        }
    }
}
