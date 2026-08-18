package com.demonicrous.furusato.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Draws the deferred vanilla selector after hotbar items. */
public final class HotbarAnimationEvents {
    @SubscribeEvent
    public void afterHotbar(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR) {
            HotbarAnimation.renderDeferred(Minecraft.getMinecraft());
        }
    }
}
