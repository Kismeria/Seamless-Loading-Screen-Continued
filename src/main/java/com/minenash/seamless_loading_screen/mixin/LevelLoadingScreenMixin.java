package com.minenash.seamless_loading_screen.mixin;

import com.minenash.seamless_loading_screen.ScreenshotLoader;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LevelLoadingScreen.class)
public abstract class LevelLoadingScreenMixin {

    /**
     * Makes the not yet generated chunks of the chunk map see-through, so they don't cover the screenshot.
     */
    //? if >=26.1 {
    @ModifyArg(method = "extractChunksForRendering", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V"), index = 4)
    //?} else if >=1.21.2 {
    /*@ModifyArg(method = "renderChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V"), index = 4)
    *///?}
    // On 1.21.1 the chunk map is drawn inside a lambda, so the chunks stay opaque there
    //? if >=1.21.2 {
    private static int seamless_loading_screen$translucentChunks(int color) {
        return ScreenshotLoader.replaceBackground && color == 0xFF000000 ? 0xAA000000 : color;
    }
    //?}
}
