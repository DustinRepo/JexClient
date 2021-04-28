package me.dustin.jex.feature.impl.player;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.event.player.EventPlayerUpdates;
import me.dustin.jex.event.player.EventPushOutOfBlocks;
import me.dustin.jex.event.render.EventMarkChunkClosed;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.math.RotationVector;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.load.impl.IKeyBinding;
import me.dustin.jex.load.impl.IPlayerMoveC2SPacket;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

@Feat(name = "Freecam", category = FeatureCategory.PLAYER, description = "Take a look around like a ghost.")
public class Freecam extends Feature {

    @Op(name = "Stealth")
    public boolean stealth;
    @Op(name = "Reset Pos on Disable")
    public boolean resetPos = true;
    @Op(name = "Speed", min = 0.1f, max = 5f, inc = 0.1f)
    public float speed = 0.5f;

    private Vec3d savedCoords = Vec3d.ZERO;
    private RotationVector lookVec = new RotationVector(0, 0);

    @EventListener(events = {EventPacketSent.class, EventMove.class, EventPlayerUpdates.class, EventPushOutOfBlocks.class, EventRender3D.class, EventMarkChunkClosed.class})
    public void runEvent(Event event) {
        if (event instanceof EventMarkChunkClosed)
            event.cancel();
        if (event instanceof EventPacketSent) {
            EventPacketSent packetSent = (EventPacketSent) event;
            if (stealth) {
                if (!(packetSent.getPacket() instanceof KeepAliveC2SPacket || packetSent.getPacket() instanceof ChatMessageC2SPacket))
                    packetSent.cancel();
            } else if (packetSent.getPacket() instanceof PlayerMoveC2SPacket) {
                IPlayerMoveC2SPacket iPlayerMoveC2SPacket = (IPlayerMoveC2SPacket) packetSent.getPacket();
                iPlayerMoveC2SPacket.setX(savedCoords.getX());
                iPlayerMoveC2SPacket.setY(savedCoords.getY());
                iPlayerMoveC2SPacket.setZ(savedCoords.getZ());
                iPlayerMoveC2SPacket.setYaw(lookVec.getYaw());
                iPlayerMoveC2SPacket.setPitch(lookVec.getPitch());
            }
        }
        if (event instanceof EventMove) {
            EventMove move = (EventMove) event;
            if (!PlayerHelper.INSTANCE.isMoving()) {
                move.setX(0);
                move.setZ(0);
            } else {
                PlayerHelper.INSTANCE.setMoveSpeed(move, speed);
            }
            move.setY(0);
            if (((IKeyBinding) Wrapper.INSTANCE.getOptions().keySneak).getPressed())
                move.setY(-speed);
            if (((IKeyBinding) Wrapper.INSTANCE.getOptions().keyJump).getPressed())
                move.setY(speed);
        }
        if (event instanceof EventPlayerUpdates) {
            Wrapper.INSTANCE.getLocalPlayer().handSwingProgress += 400.0F;
            Wrapper.INSTANCE.getLocalPlayer().noClip = true;
        }
        if (event instanceof EventPushOutOfBlocks) {
            event.cancel();
        }
        if (event instanceof EventRender3D) {
            Vec3d renderVec = Render3DHelper.INSTANCE.getRenderPosition(savedCoords);
            Render3DHelper.INSTANCE.drawEntityBox(((EventRender3D) event).getMatrixStack(), Wrapper.INSTANCE.getLocalPlayer(), renderVec.getX(), renderVec.getY(), renderVec.getZ(), ColorHelper.INSTANCE.getClientColor());
        }
    }

    @Override
    public void onEnable() {
        if (Wrapper.INSTANCE.getLocalPlayer() != null) {
            Wrapper.INSTANCE.getMinecraft().gameRenderer.reset();
            savedCoords = new Vec3d(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ());
            lookVec = new RotationVector(Wrapper.INSTANCE.getLocalPlayer());
        }
        super.onEnable();
    }


    @Override
    public void onDisable() {
        super.onDisable();
        if (Wrapper.INSTANCE.getLocalPlayer() != null && resetPos) {
            Wrapper.INSTANCE.getLocalPlayer().noClip = false;
            Wrapper.INSTANCE.getMinecraft().gameRenderer.reset();
            Wrapper.INSTANCE.getLocalPlayer().setPos(savedCoords.getX(), savedCoords.getY(), savedCoords.getZ());
            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(savedCoords.getX(), savedCoords.getY(), savedCoords.getZ(), false));
            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(savedCoords.getX(), -1337.0, savedCoords.getZ(), true));
            PlayerHelper.INSTANCE.setYaw(lookVec.getYaw());
            PlayerHelper.INSTANCE.setPitch(lookVec.getPitch());
        }
    }
}
