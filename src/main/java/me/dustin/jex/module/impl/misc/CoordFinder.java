package me.dustin.jex.module.impl.misc;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;

@ModClass(name = "CoordFinder", category = ModCategory.MISC, description = "sniff packets lol")
public class CoordFinder extends Module {

    @EventListener(events = {EventPacketReceive.class})
    private void run(Event event) {
        if (event.equals(EventPacketReceive.class)) {
            EventPacketReceive eventPacketReceive = (EventPacketReceive) event;
            if (eventPacketReceive.getPacket() instanceof WorldEventS2CPacket) {
                WorldEventS2CPacket worldEventS2CPacket = (WorldEventS2CPacket) eventPacketReceive.getPacket();
                if (worldEventS2CPacket.isGlobal()) {
                    System.out.println(worldEventS2CPacket.getEventId());
                    String server = Wrapper.INSTANCE.getMinecraft().isIntegratedServerRunning() ? Wrapper.INSTANCE.getMinecraft().getServer().getName() : Wrapper.INSTANCE.getMinecraft().getCurrentServerEntry().address;
                    int color = ColorHelper.INSTANCE.getColorViaHue((int) (Math.random() * 270)).getRGB();
                    switch (worldEventS2CPacket.getEventId()) {
                        case 1023: //Wither Boss
                            ChatHelper.INSTANCE.addClientMessage("Wither spawned at: " + worldEventS2CPacket.getPos().toShortString());
                            break;
                        case 1038://End Portal
                            ChatHelper.INSTANCE.addClientMessage("End Portal Activated at: " + worldEventS2CPacket.getPos().toShortString());
                            break;
                        case 1028://Ender Dragon
                            ChatHelper.INSTANCE.addClientMessage("Ender Dragon killed at: " + worldEventS2CPacket.getPos().toShortString());
                            break;
                        default:
                            ChatHelper.INSTANCE.addClientMessage("Unknown global event at: " + worldEventS2CPacket.getPos().toShortString());
                    }
                }
            }
        }
    }

}
