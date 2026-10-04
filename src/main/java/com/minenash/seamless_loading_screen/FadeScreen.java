package com.minenash.seamless_loading_screen;

import com.minenash.seamless_loading_screen.config.SeamlessLoadingScreenConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import org.slf4j.Logger;

/**
 * Shown after the world has loaded: keeps the screenshot on screen for a while, then fades it out over the world.
 */
public class FadeScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final int holdTicks;
    private final int fadeTicks;
    private int age = 0;
    private boolean done = false;

    public FadeScreen(int holdTicks, int fadeTicks) {
        super(Component.translatable("seamless_loading_screen.screen.loading_chunks"));
        this.holdTicks = Math.max(0, holdTicks);
        this.fadeTicks = Math.max(1, fadeTicks);
    }

    @Override
    protected void init() {
        if (age > 0) return;

        var config = SeamlessLoadingScreenConfig.get();
        if (!config.playSoundEffect) return;

        Identifier id = Identifier.tryParse(config.soundEffect);
        if (id == null) {
            LOGGER.error("[SeamlessLoadingScreen] The sound effect is not a valid identifier: {}", config.soundEffect);
            return;
        }

        SoundEvent sound = SoundEvent.createVariableRangeEvent(id);
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, config.soundPitch, config.soundVolume));
    }

    @Override
    public void tick() {
        age++;
        if (age >= holdTicks + fadeTicks) {
            SeamlessLoadingScreen.setScreen(null);
        }
    }

    @Override
    public void removed() {
        finish();
        super.removed();
    }

    private void finish() {
        if (done) return;
        done = true;
        ScreenshotLoader.endBackgroundReplacement();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private float alpha(float partialTick) {
        float elapsed = age + partialTick;
        if (elapsed <= holdTicks) return 1f;
        return Math.max(0f, 1f - (elapsed - holdTicks) / fadeTicks);
    }

    //? if >=26.1 {
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        drawBackground(graphics, partialTick);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        drawTitle(graphics, partialTick);
    }
    //?} else {
    /*@Override
    public void renderBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        drawBackground(graphics, partialTick);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        drawTitle(graphics, partialTick);
    }
    *///?}

    private void drawBackground(GuiGraphicsExtractor graphics, float partialTick) {
        if (!ScreenshotLoader.loaded) return;
        ScreenshotLoader.render(graphics, width, height, alpha(partialTick), partialTick);
    }

    private void drawTitle(GuiGraphicsExtractor graphics, float partialTick) {
        if (alpha(partialTick) >= 1f) {
            Gfx.centeredText(graphics, title, width / 2, 70);
        }
    }
}
