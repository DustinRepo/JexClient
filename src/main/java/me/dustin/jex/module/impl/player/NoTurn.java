package me.dustin.jex.module.impl.player;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventJoinWorld;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.load.impl.IPlayerPositionLookS2CPacket;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

@ModClass(name = "NoTurn", category = ModCategory.PLAYER, description = "Ignore the server telling you to look somewhere.")
public class NoTurn extends Module {

    boolean reconnected;

    @EventListener(events = {EventPacketReceive.class, EventJoinWorld.class})
    public void runEvent(Event event) {
        if (event.equals(EventPacketReceive.class)) {
            if (((EventPacketReceive) event).getPacket() instanceof PlayerPositionLookS2CPacket) {
                if (reconnected) {
                    reconnected = false;
                    return;
                }
                PlayerPositionLookS2CPacket packet = (PlayerPositionLookS2CPacket) ((EventPacketReceive) event).getPacket();
                if ((Wrapper.INSTANCE.getLocalPlayer() != null)) {
                    ((IPlayerPositionLookS2CPacket) packet).setYaw(Wrapper.INSTANCE.getLocalPlayer().yaw);
                    ((IPlayerPositionLookS2CPacket) packet).setPitch(Wrapper.INSTANCE.getLocalPlayer().pitch);
                }
            }
        }
        if (event.equals(EventJoinWorld.class)) {
            reconnected = true;
        }
    }

}
