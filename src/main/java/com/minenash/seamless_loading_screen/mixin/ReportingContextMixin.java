package com.minenash.seamless_loading_screen.mixin;

import com.minenash.seamless_loading_screen.OnLeaveHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ReportingContext.class)
public abstract class ReportingContextMixin {

    /**
     * The disconnect button of the pause menu leaves the world through this method.
     */
    @ModifyVariable(method = "draftReportHandled", at = @At("HEAD"), argsOnly = true)
    private Runnable seamless_loading_screen$screenshotOnLeave(Runnable onDiscard, Minecraft minecraft, Screen screen) {
        if (!(screen instanceof PauseScreen)) return onDiscard;
        return () -> OnLeaveHelper.beginScreenshotTask(onDiscard);
    }
}
