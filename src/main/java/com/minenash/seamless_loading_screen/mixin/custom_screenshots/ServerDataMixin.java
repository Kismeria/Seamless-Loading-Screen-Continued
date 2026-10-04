package com.minenash.seamless_loading_screen.mixin.custom_screenshots;

import com.minenash.seamless_loading_screen.DisplayMode;
import com.minenash.seamless_loading_screen.ServerInfoExtension;
import com.minenash.seamless_loading_screen.config.SeamlessLoadingScreenConfig;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerData.class)
public abstract class ServerDataMixin implements ServerInfoExtension {

    @Unique
    private static final String DISPLAY_MODE_KEY = "screenshotDisplayMode";

    @Shadow
    public String ip;

    /** Null until the player picks a mode, then the config default is used. */
    @Unique
    private DisplayMode seamless_loading_screen$displayMode = null;

    @Inject(method = "read", at = @At("RETURN"))
    private static void seamless_loading_screen$read(CompoundTag tag, CallbackInfoReturnable<ServerData> cir) {
        //? if >=1.21.5 {
        String mode = tag.getStringOr(DISPLAY_MODE_KEY, "");
        //?} else {
        /*String mode = tag.contains(DISPLAY_MODE_KEY, 8) ? tag.getString(DISPLAY_MODE_KEY) : "";
        *///?}
        if (mode.isEmpty()) return;

        try {
            ((ServerInfoExtension) cir.getReturnValue()).seamless_loading_screen$setDisplayMode(DisplayMode.valueOf(mode));
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void seamless_loading_screen$write(CallbackInfoReturnable<CompoundTag> cir) {
        if (seamless_loading_screen$displayMode != null) {
            cir.getReturnValue().putString(DISPLAY_MODE_KEY, seamless_loading_screen$displayMode.name());
        }
    }

    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void seamless_loading_screen$copyFrom(ServerData other, CallbackInfo ci) {
        seamless_loading_screen$displayMode = ((ServerDataMixin) (Object) other).seamless_loading_screen$displayMode;
    }

    @Override
    public DisplayMode seamless_loading_screen$getDisplayMode() {
        var config = SeamlessLoadingScreenConfig.get();

        for (String blacklistedAddress : config.blacklistedAddresses) {
            if (!blacklistedAddress.isBlank() && this.ip.contains(blacklistedAddress)) {
                return DisplayMode.DISABLED;
            }
        }

        return seamless_loading_screen$displayMode != null ? seamless_loading_screen$displayMode : config.defaultServerMode;
    }

    @Override
    public void seamless_loading_screen$setDisplayMode(DisplayMode mode) {
        seamless_loading_screen$displayMode = mode;
    }
}
