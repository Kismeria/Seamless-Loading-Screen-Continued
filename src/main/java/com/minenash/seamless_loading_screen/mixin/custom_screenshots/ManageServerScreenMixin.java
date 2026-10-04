package com.minenash.seamless_loading_screen.mixin.custom_screenshots;

import com.minenash.seamless_loading_screen.ServerInfoExtension;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

//? if >=1.21.9 {
import net.minecraft.client.gui.screens.ManageServerScreen;
//?} else {
/*import net.minecraft.client.gui.screens.EditServerScreen;
*///?}

/**
 * Adds the display mode button to the add/edit server screen. The button takes the row of the "Done" button,
 * which moves next to "Cancel".
 */
//? if >=1.21.9 {
@Mixin(ManageServerScreen.class)
//?} else {
/*@Mixin(EditServerScreen.class)
*///?}
public abstract class ManageServerScreenMixin extends Screen {

    @Shadow
    @Final
    private ServerData serverData;

    protected ManageServerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void seamless_loading_screen$addDisplayModeButton(CallbackInfo ci) {
        List<Button> buttons = new ArrayList<>();
        for (GuiEventListener child : this.children()) {
            if (child instanceof Button button) buttons.add(button);
        }
        if (buttons.size() < 2) return;

        Button done = buttons.get(buttons.size() - 2);
        Button cancel = buttons.get(buttons.size() - 1);

        int x = done.getX();
        int y = done.getY();
        int width = done.getWidth();
        int halfWidth = (width - 4) / 2;

        done.setY(cancel.getY());
        done.setWidth(halfWidth);
        cancel.setX(x + width - halfWidth);
        cancel.setWidth(halfWidth);

        this.addRenderableWidget(Button.builder(seamless_loading_screen$getText(), button -> {
            var server = (ServerInfoExtension) serverData;
            server.seamless_loading_screen$setDisplayMode(server.seamless_loading_screen$getDisplayMode().next());
            button.setMessage(seamless_loading_screen$getText());
        }).bounds(x, y, width, 20).build());
    }

    @Unique
    private Component seamless_loading_screen$getText() {
        String mode = ((ServerInfoExtension) serverData).seamless_loading_screen$getDisplayMode().name().toLowerCase();
        return Component.translatable("seamless_loading_screen.config.displayMode")
                .append(": ")
                .append(Component.translatable("seamless_loading_screen.config.displayMode." + mode));
    }
}
