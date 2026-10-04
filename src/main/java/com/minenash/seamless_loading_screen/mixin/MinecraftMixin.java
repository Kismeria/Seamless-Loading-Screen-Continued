package com.minenash.seamless_loading_screen.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.minenash.seamless_loading_screen.OnLeaveHelper;
import com.minenash.seamless_loading_screen.ScreenshotLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    public ClientLevel level;

    @Shadow
    public abstract void stop();

    @Unique
    private boolean seamless_loading_screen$stopping = false;

    /**
     * Every way of opening a singleplayer world (world list, new world, quick play) ends up here.
     */
    @Inject(method = "doWorldLoad", at = @At("HEAD"))
    private void seamless_loading_screen$setWorldScreenshot(CallbackInfo ci, @Local(argsOnly = true) LevelStorageSource.LevelStorageAccess access) {
        ScreenshotLoader.setScreenshot(access.getLevelId());
    }

    @Inject(method = "stop", at = @At("HEAD"), cancellable = true)
    private void seamless_loading_screen$screenshotOnClose(CallbackInfo ci) {
        if (seamless_loading_screen$stopping || level == null) return;

        seamless_loading_screen$stopping = true;
        ci.cancel();
        OnLeaveHelper.beginScreenshotTask(this::stop);
    }
}
