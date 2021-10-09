package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.Fly;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

@Feature.Manifest(name = "Nofall", category = Feature.Category.PLAYER, description = "Remove fall damage.")
public class Nofall extends Feature {

    @EventListener(events = {EventPlayerPackets.class})
    private void runEvent(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            if (Feature.get(Fly.class).getState() && Wrapper.INSTANCE.getLocalPlayer().isSneaking()) return;
            if (Wrapper.INSTANCE.getLocalPlayer().isFallFlying()) return;
            if (Wrapper.INSTANCE.getLocalPlayer().fallDistance > 2.5f) {
                NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
            }
        }
    }

    @EventListener(events = {EventPacketSent.class})
    private void runEvent(EventPacketSent eventPacketSent) {
        if (eventPacketSent.getMode() != EventPacketSent.Mode.PRE) return;
        if (eventPacketSent.getPacket() instanceof PlayerMoveC2SPacket origPacket && Wrapper.INSTANCE.getLocalPlayer().fallDistance > 2.5f) {
            if ((Feature.get(Fly.class).getState() && Wrapper.INSTANCE.getLocalPlayer().isSneaking()) || Wrapper.INSTANCE.getLocalPlayer().isFallFlying()) return;
            PlayerMoveC2SPacket playerMoveC2SPacket = new PlayerMoveC2SPacket.Full(origPacket.getX(Wrapper.INSTANCE.getLocalPlayer().getX()), origPacket.getY(Wrapper.INSTANCE.getLocalPlayer().getY()), origPacket.getZ(Wrapper.INSTANCE.getLocalPlayer().getZ()), origPacket.getYaw(PlayerHelper.INSTANCE.getYaw()), origPacket.getPitch(PlayerHelper.INSTANCE.getPitch()), true);
            eventPacketSent.setPacket(playerMoveC2SPacket);
        }
    }
}
