package com.demonicrous.furusato.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Adds a direct Font Settings entry to the vanilla video settings screen. */
public final class ClientGuiEvents {
    private static final int FONT_SETTINGS = 178301;

    @SubscribeEvent
    public void onGuiInitialized(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!(event.getGui() instanceof GuiVideoSettings)) {
            return;
        }

        int center = event.getGui().width / 2;
        for (GuiButton button : event.getButtonList()) {
            if (button.id == 200) {
                button.x = center + 2;
                button.width = 98;
                event.getButtonList().add(new GuiButton(
                        FONT_SETTINGS,
                        center - 100,
                        button.y,
                        98,
                        20,
                        net.minecraft.client.resources.I18n.format("furusato.font.open")
                ));
                return;
            }
        }
    }

    @SubscribeEvent
    public void onButtonPressed(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (event.getGui() instanceof GuiVideoSettings
                && event.getButton().id == FONT_SETTINGS) {
            event.setCanceled(true);
            event.getGui().mc.displayGuiScreen(new GuiFontSettings(event.getGui()));
        }
    }
}

