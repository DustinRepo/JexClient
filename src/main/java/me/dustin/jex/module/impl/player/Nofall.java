package me.dustin.jex.module.impl.player;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.load.impl.IPlayerMoveC2SPacket;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

@ModClass(name = "Nofall", category = ModCategory.PLAYER, description = "Remove fall damage.")
public class Nofall extends Module {

    @EventListener(events = {EventPlayerPackets.class})
    private void runEvent(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            if (Wrapper.INSTANCE.getLocalPlayer().fallDistance > 2.5f) {
                NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket(true));
            }
        }
    }

    @EventListener(events = {EventPacketSent.class})
    private void runEvent(EventPacketSent eventPacketSent) {
        if (eventPacketSent.getPacket() instanceof PlayerMoveC2SPacket && Wrapper.INSTANCE.getLocalPlayer().fallDistance > 2.5f) {
            IPlayerMoveC2SPacket iPlayerMoveC2SPacket = (IPlayerMoveC2SPacket) (PlayerMoveC2SPacket) eventPacketSent.getPacket();
            iPlayerMoveC2SPacket.setOnGround(true);
        }
    }
}
