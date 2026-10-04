package com.minenash.seamless_loading_screen.mixin;

import com.minenash.seamless_loading_screen.OnLeaveHelper;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.DisconnectionDetails;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ClientCommonPacketListenerImplMixin {

    @Shadow
    public abstract void onDisconnect(DisconnectionDetails details);

    @Unique
    private boolean seamless_loading_screen$screenshotRequested = false;

    /**
     * Takes the screenshot when the server closes the connection.
     */
    @Inject(method = "onDisconnect", at = @At("HEAD"), cancellable = true)
    private void seamless_loading_screen$screenshotOnDisconnect(DisconnectionDetails details, CallbackInfo ci) {
        if (seamless_loading_screen$screenshotRequested) return;

        seamless_loading_screen$screenshotRequested = true;
        ci.cancel();
        OnLeaveHelper.beginScreenshotTask(() -> this.onDisconnect(details));
    }
}
