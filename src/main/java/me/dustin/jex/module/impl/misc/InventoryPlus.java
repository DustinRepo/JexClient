package me.dustin.jex.module.impl.misc;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

@ModClass(name = "InventoryPlus", category = ModCategory.MISC, description = "Keep items in your crafting space in inventory.")
public class InventoryPlus extends Module {

    @EventListener(events = {EventPacketSent.class})
    private void runMethod(EventPacketSent eventPacketSent) {
        if (eventPacketSent.getPacket() instanceof CloseHandledScreenC2SPacket) {
            if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof InventoryScreen) {
                eventPacketSent.cancel();
            }
        }
    }

}
