package me.dustin.jex.feature.impl.player;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.entity.FakePlayerEntity;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.event.player.EventPlayerUpdates;
import me.dustin.jex.event.player.EventPushOutOfBlocks;
import me.dustin.jex.event.render.EventMarkChunkClosed;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.helper.math.RotationVector;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.load.impl.IKeyBinding;
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
    public static FakePlayerEntity playerEntity;

    @EventListener(events = {EventPacketSent.class, EventMove.class, EventPlayerUpdates.class, EventPushOutOfBlocks.class, EventMarkChunkClosed.class})
    public void runEvent(Event event) {
        if (event instanceof EventMarkChunkClosed)
            event.cancel();
        if (event instanceof EventPacketSent) {
            EventPacketSent packetSent = (EventPacketSent) event;
            if (stealth) {
                if (!(packetSent.getPacket() instanceof KeepAliveC2SPacket || packetSent.getPacket() instanceof ChatMessageC2SPacket))
                    packetSent.cancel();
            } else if (packetSent.getPacket() instanceof PlayerMoveC2SPacket) {
                PlayerMoveC2SPacket playerMoveC2SPacket = new PlayerMoveC2SPacket.Both(savedCoords.getX(), savedCoords.getY(), savedCoords.getZ(), lookVec.getYaw(), lookVec.getPitch(), true);
                packetSent.setPacket(playerMoveC2SPacket);
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
    }

    @Override
    public void onEnable() {
        if (Wrapper.INSTANCE.getLocalPlayer() != null) {
            Wrapper.INSTANCE.getMinecraft().gameRenderer.reset();
            savedCoords = new Vec3d(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ());
            lookVec = new RotationVector(Wrapper.INSTANCE.getLocalPlayer());

            playerEntity = new FakePlayerEntity(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer().getGameProfile());
            playerEntity.copyFrom(Wrapper.INSTANCE.getLocalPlayer());
            playerEntity.copyPositionAndRotation(Wrapper.INSTANCE.getLocalPlayer());
            Wrapper.INSTANCE.getWorld().addEntity(-69, playerEntity);
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
            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionOnly(savedCoords.getX(), savedCoords.getY(), savedCoords.getZ(), false));
            if (!Wrapper.INSTANCE.getMinecraft().isInSingleplayer())
                NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionOnly(savedCoords.getX(), -1337.0, savedCoords.getZ(), true));
            Wrapper.INSTANCE.getLocalPlayer().yaw = lookVec.getYaw();
            Wrapper.INSTANCE.getLocalPlayer().pitch = lookVec.getPitch();
        }
        savedCoords = Vec3d.ZERO;
        if (playerEntity != null) {
            if (Wrapper.INSTANCE.getWorld() != null)
                Wrapper.INSTANCE.getWorld().removeEntity(-69);
            playerEntity = null;
        }
    }
}
