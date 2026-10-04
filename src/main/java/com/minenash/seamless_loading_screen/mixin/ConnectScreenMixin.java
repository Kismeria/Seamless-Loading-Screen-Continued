package com.minenash.seamless_loading_screen.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.minenash.seamless_loading_screen.ScreenshotLoader;
import com.minenash.seamless_loading_screen.ServerInfoExtension;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public abstract class ConnectScreenMixin {

    @Inject(method = "connect", at = @At("HEAD"))
    private void seamless_loading_screen$setServerScreenshot(CallbackInfo ci, @Local(argsOnly = true) ServerAddress address, @Local(argsOnly = true) ServerData server) {
        ScreenshotLoader.displayMode = ((ServerInfoExtension) server).seamless_loading_screen$getDisplayMode();
        ScreenshotLoader.setScreenshot(address.getHost(), address.getPort());
    }
}
