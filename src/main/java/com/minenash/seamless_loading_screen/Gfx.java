package com.minenash.seamless_loading_screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

//? if >=1.21.6 {
import net.minecraft.client.renderer.RenderPipelines;
//?} else if >=1.21.2 {
/*import net.minecraft.client.renderer.RenderType;
*///?} else {
/*import com.mojang.blaze3d.systems.RenderSystem;
*///?}

/**
 * Small layer over the GUI drawing calls that differ between Minecraft versions.
 */
public final class Gfx {

    private Gfx() {
    }

    public static void blit(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int width, int height, float alpha) {
        //? if >=1.21.6 {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, width, height, width, height, argb(alpha, 0xFFFFFF));
        //?} else if >=1.21.2 {
        /*graphics.blit(RenderType::guiTextured, texture, x, y, 0, 0, width, height, width, height, argb(alpha, 0xFFFFFF));
        *///?} else {
        /*RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        graphics.blit(texture, x, y, 0, 0, width, height, width, height);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
        *///?}
    }

    public static void fill(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int argb) {
        graphics.fill(x0, y0, x1, y1, argb);
    }

    public static void centeredText(GuiGraphicsExtractor graphics, Component text, int x, int y) {
        var font = Minecraft.getInstance().font;
        //? if >=26.1 {
        graphics.centeredText(font, text, x, y, -1);
        //?} else {
        /*graphics.drawCenteredString(font, text, x, y, -1);
        *///?}
    }

    /**
     * Blurs everything drawn before this call, using the vanilla menu blur strength.
     */
    public static void blur(GuiGraphicsExtractor graphics, float partialTick) {
        var client = Minecraft.getInstance();
        if (client.options.getMenuBackgroundBlurriness() < 1) return;

        //? if >=1.21.6 {
        graphics.blurBeforeThisStratum();
        //?} else if >=1.21.5 {
        /*graphics.flush();
        client.gameRenderer.processBlurEffect();
        *///?} else if >=1.21.2 {
        /*graphics.flush();
        client.gameRenderer.processBlurEffect();
        client.getMainRenderTarget().bindWrite(false);
        *///?} else {
        /*graphics.flush();
        client.gameRenderer.processBlurEffect(partialTick);
        client.getMainRenderTarget().bindWrite(false);
        *///?}
    }

    public static int argb(float alpha, int rgb) {
        int a = Math.round(Math.max(0f, Math.min(1f, alpha)) * 255);
        return a << 24 | (rgb & 0xFFFFFF);
    }
}
