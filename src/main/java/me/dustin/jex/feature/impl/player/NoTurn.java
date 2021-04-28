package me.dustin.jex.feature.impl.player;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventJoinWorld;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.load.impl.IPlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

@Feat(name = "NoTurn", category = FeatureCategory.PLAYER, description = "Ignore the server telling you to look somewhere.")
public class NoTurn extends Feature {

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
                    ((IPlayerPositionLookS2CPacket) packet).setYaw(PlayerHelper.INSTANCE.getYaw());
                    ((IPlayerPositionLookS2CPacket) packet).setPitch(PlayerHelper.INSTANCE.getPitch());
                }
            }
        }
        if (event.equals(EventJoinWorld.class)) {
            reconnected = true;
        }
    }

}
