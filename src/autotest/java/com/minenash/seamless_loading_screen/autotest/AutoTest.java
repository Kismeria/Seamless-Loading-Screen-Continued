package com.minenash.seamless_loading_screen.autotest;

import com.minenash.seamless_loading_screen.DisplayMode;
import com.minenash.seamless_loading_screen.FadeScreen;
import com.minenash.seamless_loading_screen.ServerInfoExtension;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.realmsclient.RealmsMainScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import com.minenash.seamless_loading_screen.ScreenshotLoader;
import com.minenash.seamless_loading_screen.SeamlessLoadingScreen;
import com.minenash.seamless_loading_screen.config.SeamlessLoadingScreenConfig;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

//? if >=1.21.9 {
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.ManageServerScreen;
import net.minecraft.client.input.KeyEvent;
//?} else {
/*import net.minecraft.client.gui.screens.EditServerScreen;
*///?}

/**
 * Dev-only smoke test, enabled with -Dsls.autotest=true. Creates a world, leaves it through the pause menu,
 * rejoins it, then closes the game. Results go to the log ("[SLS-TEST]") and to sls-test-result.txt.
 */
public class AutoTest implements ClientModInitializer {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String WORLD = "slstest";

    private enum Phase { START, EDIT_SERVER, CONNECTING, CREATING, IN_WORLD_1, LEAVING, REJOINING, IN_WORLD_2, DONE }

    private Phase phase = Phase.START;
    private int phaseTicks = 0;
    private int totalTicks = 0;
    private boolean sawReplacedBackground = false;
    private boolean sawFadeScreen = false;
    private boolean sawServerBackground = false;
    private ServerData editedServer;
    private final Set<String> joinScreens = new LinkedHashSet<>();
    private final List<String> results = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        if (!Boolean.getBoolean("sls.autotest")) return;
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void setPhase(Phase next) {
        log("phase " + phase + " -> " + next + " after " + phaseTicks + " ticks");
        phase = next;
        phaseTicks = 0;
    }

    private void tick(Minecraft client) {
        phaseTicks++;
        totalTicks++;
        Screen screen = SeamlessLoadingScreen.currentScreen();

        if (totalTicks > 20 * 300 && phase != Phase.DONE) {
            fail("timeout in phase " + phase + ", screen " + name(screen));
            finish(client);
            return;
        }

        switch (phase) {
            case START -> {
                if (screen instanceof AccessibilityOnboardingScreen) {
                    SeamlessLoadingScreen.setScreen(new TitleScreen());
                } else if (screen instanceof TitleScreen && phaseTicks > 40 && !hasOverlay(client)) {
                    deleteWorld(client);
                    // Load the classes whose mixins are not used by this test, so a broken mixin crashes here
                    log("loaded " + List.of(ClientCommonPacketListenerImpl.class, RealmsMainScreen.class, ConnectScreen.class));
                    serverDataTest();

                    editedServer = new ServerData("test", "example.com", ServerData.Type.OTHER);
                    //? if >=1.21.9 {
                    SeamlessLoadingScreen.setScreen(new ManageServerScreen(new TitleScreen(), Component.literal("test"), ok -> {}, editedServer));
                    //?} else {
                    /*SeamlessLoadingScreen.setScreen(new EditServerScreen(new TitleScreen(), ok -> {}, editedServer));
                    *///?}
                    setPhase(Phase.EDIT_SERVER);
                }
            }
            case EDIT_SERVER -> {
                if (phaseTicks == 5) {
                    String prefix = Component.translatable("seamless_loading_screen.config.displayMode").getString();
                    Button modeButton = null;
                    for (GuiEventListener child : screen.children()) {
                        if (child instanceof Button button && button.getMessage().getString().startsWith(prefix)) modeButton = button;
                    }
                    check(modeButton != null, "display mode button on the edit server screen");
                    if (modeButton != null) {
                        grab(client, "edit server screen");
                        press(modeButton);
                        check(mode(editedServer) == DisplayMode.FREEZE, "display mode button cycles the mode: " + mode(editedServer));
                    }
                } else if (phaseTicks == 10) {
                    // A server that refuses the connection, with a screenshot already saved for it
                    try {
                        Path file = FabricLoader.getInstance().getGameDir().resolve("screenshots/worlds/servers/127.0.0.1_1.png");
                        Files.createDirectories(file.getParent());
                        try (NativeImage image = new NativeImage(16, 9, false)) {
                            image.writeToFile(file);
                        }
                    } catch (IOException e) {
                        fail("cannot write the server screenshot: " + e);
                    }
                    ConnectScreen.startConnecting(new TitleScreen(), client, ServerAddress.parseString("127.0.0.1:1"),
                            new ServerData("refused", "127.0.0.1:1", ServerData.Type.OTHER), false, null);
                    setPhase(Phase.CONNECTING);
                }
            }
            case CONNECTING -> {
                if (screen instanceof ConnectScreen && ScreenshotLoader.replaceBackground && !sawServerBackground) {
                    sawServerBackground = true;
                    grab(client, "connect screen");
                }
                if (!(screen instanceof ConnectScreen) && phaseTicks > 5) {
                    Path file = ScreenshotLoader.getFilePath();
                    check(file != null && file.endsWith(Path.of("servers", "127.0.0.1_1.png")), "server screenshot path: " + file);
                    check(sawServerBackground, "screenshot used as connect screen background");
                    log("screen after failed connection: " + name(screen));

                    //? if >=1.21.9 {
                    CreateWorldScreen.openFresh(client, () -> SeamlessLoadingScreen.setScreen(new TitleScreen()));
                    //?} else {
                    /*CreateWorldScreen.openFresh(client, new TitleScreen());
                    *///?}
                    setPhase(Phase.CREATING);
                }
            }
            case CREATING -> {
                if (screen instanceof CreateWorldScreen create && phaseTicks > 5) {
                    create.getUiState().setName(WORLD);
                    press(findButton(create, "selectWorld.create"));
                    setPhase(Phase.IN_WORLD_1);
                }
            }
            case IN_WORLD_1 -> {
                if (client.level == null || client.player == null) {
                    phaseTicks = 0;
                    return;
                }
                if (screen != null && !(screen instanceof PauseScreen)) {
                    phaseTicks = 0;
                    return;
                }
                if (phaseTicks == 100) {
                    check(ScreenshotLoader.getFilePath() != null, "screenshot path is set after creating the world: " + ScreenshotLoader.getFilePath());
                    SeamlessLoadingScreen.setScreen(new PauseScreen(true));
                } else if (phaseTicks == 105) {
                    press(findButton(screen, "menu.returnToMenu"));
                    setPhase(Phase.LEAVING);
                }
            }
            case LEAVING -> {
                if (screen instanceof TitleScreen && client.level == null && phaseTicks > 20) {
                    Path file = ScreenshotLoader.getFilePath();
                    if (file != null && Files.exists(file)) {
                        check(true, "screenshot saved on leave: " + file);
                        client.createWorldOpenFlows().openWorld(WORLD, () -> SeamlessLoadingScreen.setScreen(new TitleScreen()));
                        setPhase(Phase.REJOINING);
                    } else if (phaseTicks > 200) {
                        fail("no screenshot saved on leave: " + file);
                        finish(client);
                    }
                }
            }
            case REJOINING -> {
                if (screen != null) joinScreens.add(name(screen));

                if (screen != null && SeamlessLoadingScreen.isLoadingScreen(screen) && ScreenshotLoader.replaceBackground) {
                    if (!sawReplacedBackground) {
                        sawReplacedBackground = true;
                        grab(client, "loading screen " + name(screen));
                    }
                }

                if (screen instanceof FadeScreen) {
                    if (!sawFadeScreen) {
                        sawFadeScreen = true;
                        phaseTicks = 0;
                    }
                    var config = SeamlessLoadingScreenConfig.get();
                    if (phaseTicks == 5 || phaseTicks == config.time + config.fade / 2) {
                        grab(client, "fade screen tick " + phaseTicks);
                    }
                }

                if (sawFadeScreen && screen == null && client.level != null) {
                    setPhase(Phase.IN_WORLD_2);
                } else if (!sawFadeScreen && client.level != null && screen == null && phaseTicks > 400) {
                    setPhase(Phase.IN_WORLD_2);
                }
            }
            case IN_WORLD_2 -> {
                if (phaseTicks == 10) grab(client, "in world after fade");
                if (phaseTicks == 40) {
                    log("screens while joining: " + joinScreens);
                    check(sawReplacedBackground, "screenshot used as loading screen background");
                    check(sawFadeScreen, "fade screen shown after join");
                    check(!ScreenshotLoader.replaceBackground, "background replacement ended after fade");
                    try {
                        Path file = ScreenshotLoader.getFilePath();
                        results.add("MTIME_BEFORE_CLOSE " + Files.getLastModifiedTime(file).toMillis() + " " + file);
                    } catch (IOException e) {
                        fail("cannot read screenshot mtime: " + e);
                    }
                    finish(client);
                }
            }
            case DONE -> {
            }
        }
    }

    private static boolean hasOverlay(Minecraft client) {
        //? if >=26.2 {
        return client.gui.overlay() != null;
        //?} else {
        /*return client.getOverlay() != null;
        *///?}
    }

    private void serverDataTest() {
        ServerData data = new ServerData("test", "example.com", ServerData.Type.OTHER);
        check(mode(data) == DisplayMode.ENABLED, "default server display mode is enabled");

        ((ServerInfoExtension) data).seamless_loading_screen$setDisplayMode(DisplayMode.DISABLED);
        ServerData copy = ServerData.read(data.write());
        check(mode(copy) == DisplayMode.DISABLED, "server display mode is saved: " + mode(copy));

        ServerData blacklisted = new ServerData("test", "play.wynncraft.com", ServerData.Type.OTHER);
        check(mode(blacklisted) == DisplayMode.DISABLED, "blacklisted server is disabled");
    }

    private static DisplayMode mode(ServerData data) {
        return ((ServerInfoExtension) data).seamless_loading_screen$getDisplayMode();
    }

    private static Button findButton(Screen screen, String translationKey) {
        String text = Component.translatable(translationKey).getString();
        for (GuiEventListener child : screen.children()) {
            if (child instanceof Button button && button.getMessage().getString().equals(text)) return button;
        }
        throw new IllegalStateException("No button '" + text + "' on " + name(screen));
    }

    private static void press(Button button) {
        log("pressing " + button.getMessage().getString());
        //? if >=1.21.9 {
        button.onPress(new KeyEvent(InputConstants.KEY_RETURN, 0, 0));
        //?} else {
        /*button.onPress();
        *///?}
    }

    private void finish(Minecraft client) {
        setPhase(Phase.DONE);
        results.add("END");
        try {
            Files.write(FabricLoader.getInstance().getGameDir().resolve("sls-test-result.txt"), results);
        } catch (IOException e) {
            LOGGER.error("[SLS-TEST] cannot write results", e);
        }
        // Closing the window with a world open also tests the screenshot on close
        client.stop();
    }

    private void deleteWorld(Minecraft client) {
        Path gameDir = FabricLoader.getInstance().getGameDir();
        deleteRecursive(gameDir.resolve("saves").resolve(WORLD));
        deleteRecursive(gameDir.resolve("screenshots"));
    }

    private static void deleteRecursive(Path path) {
        if (!Files.exists(path)) return;
        try (var walk = Files.walk(path)) {
            walk.sorted((a, b) -> b.getNameCount() - a.getNameCount()).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException ignore) {
                }
            });
        } catch (IOException ignore) {
        }
    }

    private void grab(Minecraft client, String what) {
        log("grab: " + what);
        //? if >=26.2 {
        var target = client.gameRenderer.mainRenderTarget();
        //?} else {
        /*var target = client.getMainRenderTarget();
        *///?}
        Screenshot.grab(client.gameDirectory, target, message -> log("grabbed " + what + ": " + message.getString()));
    }

    private void check(boolean ok, String what) {
        String line = (ok ? "PASS " : "FAIL ") + what;
        results.add(line);
        log(line);
    }

    private void fail(String what) {
        check(false, what);
    }

    private static String name(Screen screen) {
        return screen == null ? "null" : screen.getClass().getSimpleName();
    }

    private static void log(String message) {
        LOGGER.info("[SLS-TEST] {}", message);
    }
}
