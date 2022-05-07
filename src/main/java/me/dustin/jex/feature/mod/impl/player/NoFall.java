package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.fly.Fly;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

@Feature.Manifest(category = Feature.Category.PLAYER, description = "Remove fall damage.")
public class NoFall extends Feature {

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (Feature.getState(Fly.class) && !Feature.get(Fly.class).mode.equalsIgnoreCase("Creative") && Wrapper.INSTANCE.getLocalPlayer().isShiftKeyDown() && Wrapper.INSTANCE.getLocalPlayer().tickCount % 10 == 0) return;
        if (Wrapper.INSTANCE.getLocalPlayer().isFallFlying() && !isFallSpeedDangerous()) return;
        if (Wrapper.INSTANCE.getLocalPlayer().fallDistance > 2.5f) {
            NetworkHelper.INSTANCE.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(true));
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getLocalPlayer().fallDistance > 2.5f) {
            ServerboundMovePlayerPacket origPacket = (ServerboundMovePlayerPacket) event.getPacket();
            if ((Feature.getState(Fly.class) && Wrapper.INSTANCE.getLocalPlayer().isShiftKeyDown()) || Wrapper.INSTANCE.getLocalPlayer().isFallFlying()) return;
            ServerboundMovePlayerPacket playerMoveC2SPacket = new ServerboundMovePlayerPacket.PosRot(origPacket.getX(Wrapper.INSTANCE.getLocalPlayer().getX()), origPacket.getY(Wrapper.INSTANCE.getLocalPlayer().getY()), origPacket.getZ(Wrapper.INSTANCE.getLocalPlayer().getZ()), origPacket.getYRot(PlayerHelper.INSTANCE.getYaw()), origPacket.getXRot(PlayerHelper.INSTANCE.getPitch()), true);
            event.setPacket(playerMoveC2SPacket);
        }
    }, new ClientPacketFilter(EventPacketSent.Mode.PRE, ServerboundMovePlayerPacket.class));

    private boolean isFallSpeedDangerous() {
        return Wrapper.INSTANCE.getLocalPlayer().getDeltaMovement().y < -0.5;
    }
}
