package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import me.dustin.jex.feature.mod.core.Feature;

@Feature.Manifest(category = Feature.Category.MISC, description = "Keep items in your crafting space in inventory.")
public class InventoryPlus extends Feature {

    @EventPointer
    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof InventoryScreen) {
            event.cancel();
        }
    }, new ClientPacketFilter(EventPacketSent.Mode.PRE, CloseHandledScreenC2SPacket.class));
}
