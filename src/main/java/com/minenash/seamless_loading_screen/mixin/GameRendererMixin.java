package com.minenash.seamless_loading_screen.mixin;

import com.minenash.seamless_loading_screen.OnLeaveHelper;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    /**
     * Vanilla takes its world icon screenshot here: right after the world is drawn, before the GUI.
     */
    @Inject(method = "tryTakeScreenshotIfNeeded", at = @At("HEAD"))
    private void seamless_loading_screen$takeScreenshot(CallbackInfo ci) {
        OnLeaveHelper.onWorldRendered();
    }
}
