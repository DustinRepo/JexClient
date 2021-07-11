package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

@Feature.Manifest(name = "InventoryPlus", category = Feature.Category.MISC, description = "Keep items in your crafting space in inventory.")
public class InventoryPlus extends Feature {

    @EventListener(events = {EventPacketSent.class})
    private void runMethod(EventPacketSent eventPacketSent) {
        if (eventPacketSent.getPacket() instanceof CloseHandledScreenC2SPacket) {
            if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof InventoryScreen) {
                eventPacketSent.cancel();
            }
        }
    }

}
