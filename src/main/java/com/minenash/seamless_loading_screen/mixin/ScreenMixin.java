package com.minenash.seamless_loading_screen.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.minenash.seamless_loading_screen.FadeScreen;
import com.minenash.seamless_loading_screen.ScreenshotLoader;
import com.minenash.seamless_loading_screen.SeamlessLoadingScreen;
import com.minenash.seamless_loading_screen.config.SeamlessLoadingScreenConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    //? if >=26.1 {
    @WrapOperation(method = "extractRenderStateWithTooltipAndSubtitles", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"))
    //?} else if >=1.21.9 {
    /*@WrapOperation(method = "renderWithTooltipAndSubtitles", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;renderBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"))
    *///?} else if >=1.21.6 {
    /*@WrapOperation(method = "renderWithTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;renderBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"))
    *///?} else {
    /*@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;renderBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"))
    *///?}
    private void seamless_loading_screen$replaceBackground(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, Operation<Void> original) {
        if (ScreenshotLoader.replaceBackground && ScreenshotLoader.loaded && SeamlessLoadingScreen.isLoadingScreen(screen)) {
            ScreenshotLoader.render(graphics, screen.width, screen.height, 1f, partialTick);
        } else {
            original.call(screen, graphics, mouseX, mouseY, partialTick);
        }
    }

    /**
     * The terrain screen closes itself once the world is ready, show the fade-out instead.
     */
    @Inject(method = "onClose", at = @At("HEAD"), cancellable = true)
    private void seamless_loading_screen$startFade(CallbackInfo ci) {
        Screen self = (Screen) (Object) this;

        if (ScreenshotLoader.replaceBackground && ScreenshotLoader.loaded
                && SeamlessLoadingScreen.isTerrainScreen(self) && Minecraft.getInstance().level != null) {
            ci.cancel();

            var config = SeamlessLoadingScreenConfig.get();
            SeamlessLoadingScreen.setScreen(new FadeScreen(config.time, config.fade));
        }
    }
}
