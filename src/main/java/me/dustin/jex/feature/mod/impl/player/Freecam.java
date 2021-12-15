package me.dustin.jex.feature.mod.impl.player;

import com.mojang.authlib.GameProfile;
import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.helper.entity.FakePlayerEntity;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.event.player.EventPlayerUpdates;
import me.dustin.jex.event.player.EventPushOutOfBlocks;
import me.dustin.jex.event.render.EventMarkChunkClosed;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

@Feature.Manifest(category = Feature.Category.PLAYER, description = "Take a look around like a ghost.")
public class Freecam extends Feature {

    @Op(name = "Stealth")
    public boolean stealth;
    @Op(name = "Reset Pos on Disable")
    public boolean resetPos = true;
    @Op(name = "Speed", min = 0.1f, max = 5f, inc = 0.1f)
    public float speed = 0.5f;

    private Vec3d savedCoords = Vec3d.ZERO;
    private RotationVector lookVec = new RotationVector(0, 0);
    public static PlayerEntity playerEntity;

    @EventPointer
    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
        if (stealth) {
            if (!(event.getPacket() instanceof KeepAliveC2SPacket || event.getPacket() instanceof ChatMessageC2SPacket))
                event.cancel();
        } else if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            PlayerMoveC2SPacket playerMoveC2SPacket = new PlayerMoveC2SPacket.Full(savedCoords.getX(), savedCoords.getY(), savedCoords.getZ(), lookVec.getYaw(), lookVec.getPitch(), true);
            event.setPacket(playerMoveC2SPacket);
        }
    }, new ClientPacketFilter(EventPacketSent.Mode.PRE));

    @EventPointer
    private final EventListener<EventMove> eventMoveEventListener = new EventListener<>(event -> {
        if (!PlayerHelper.INSTANCE.isMoving()) {
            event.setX(0);
            event.setZ(0);
        } else {
            PlayerHelper.INSTANCE.setMoveSpeed(event, speed);
        }
        event.setY(0);
        if (Wrapper.INSTANCE.getOptions().keySneak.isPressed())
            event.setY(-speed);
        if (Wrapper.INSTANCE.getOptions().keyJump.isPressed())
            event.setY(speed);
    });

    @EventPointer
    private final EventListener<EventPlayerUpdates> eventPlayerUpdatesEventListener = new EventListener<>(event -> {
        Wrapper.INSTANCE.getLocalPlayer().handSwingProgress += 400.0F;
        Wrapper.INSTANCE.getLocalPlayer().noClip = true;
    });

    @EventPointer
    private final EventListener<EventPushOutOfBlocks> eventPushOutOfBlocksEventListener = new EventListener<>(Event::cancel);

    @EventPointer
    private final EventListener<EventMarkChunkClosed> eventMarkChunkClosedEventListener = new EventListener<>(Event::cancel);

    @Override
    public void onEnable() {
        if (Wrapper.INSTANCE.getLocalPlayer() != null) {
            Wrapper.INSTANCE.getWorldRenderer().reload();
            savedCoords = new Vec3d(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ());
            lookVec = new RotationVector(Wrapper.INSTANCE.getLocalPlayer());

            playerEntity = new FakePlayerEntity(Wrapper.INSTANCE.getWorld(), new GameProfile(UUID.randomUUID(), Wrapper.INSTANCE.getMinecraft().getSession().getUsername()));
            playerEntity.copyFrom(Wrapper.INSTANCE.getLocalPlayer());
            playerEntity.copyPositionAndRotation(Wrapper.INSTANCE.getLocalPlayer());
            Wrapper.INSTANCE.getWorld().addEntity(69420, playerEntity);
        }
        super.onEnable();
    }


    @Override
    public void onDisable() {
        super.onDisable();
        if (Wrapper.INSTANCE.getLocalPlayer() != null && resetPos) {
            Wrapper.INSTANCE.getLocalPlayer().noClip = false;
            Wrapper.INSTANCE.getWorldRenderer().reload();
            Wrapper.INSTANCE.getLocalPlayer().setPos(savedCoords.getX(), savedCoords.getY(), savedCoords.getZ());
            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(savedCoords.getX(), savedCoords.getY(), savedCoords.getZ(), false));
            PlayerHelper.INSTANCE.setYaw(lookVec.getYaw());
            PlayerHelper.INSTANCE.setPitch(lookVec.getPitch());
        }
        savedCoords = Vec3d.ZERO;
        if (playerEntity != null) {
            playerEntity.setPos(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            if (Wrapper.INSTANCE.getWorld() != null)
                Wrapper.INSTANCE.getWorld().removeEntity(playerEntity.getId(), Entity.RemovalReason.DISCARDED);
            playerEntity = null;
        }
        if (Wrapper.INSTANCE.getLocalPlayer() != null)
            Wrapper.INSTANCE.getLocalPlayer().setVelocity(0, 0, 0);
    }
}
