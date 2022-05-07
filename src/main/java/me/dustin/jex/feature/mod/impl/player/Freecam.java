package me.dustin.jex.feature.mod.impl.player;

import com.mojang.authlib.GameProfile;
import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.events.core.priority.Priority;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.*;
import me.dustin.jex.feature.mod.impl.movement.fly.Fly;
import me.dustin.jex.helper.entity.FakePlayerEntity;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.render.EventMarkChunkClosed;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

@Feature.Manifest(category = Feature.Category.PLAYER, description = "Take a look around like a ghost.")
public class Freecam extends Feature {

    @Op(name = "Stealth")
    public boolean stealth;
    @Op(name = "Reset Pos on Disable")
    public boolean resetPos = true;
    @Op(name = "Speed", min = 0.1f, max = 5f, inc = 0.1f)
    public float speed = 0.5f;

    private Vec3 savedCoords = Vec3.ZERO;
    private RotationVector lookVec = new RotationVector(0, 0);
    public static Player playerEntity;

    @EventPointer
    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
        if (stealth) {
            if (!(event.getPacket() instanceof ServerboundKeepAlivePacket || event.getPacket() instanceof ServerboundChatPacket))
                event.cancel();
        } else if (event.getPacket() instanceof ServerboundMovePlayerPacket) {
            ServerboundMovePlayerPacket playerMoveC2SPacket = new ServerboundMovePlayerPacket.PosRot(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), playerEntity.getYRot(), playerEntity.getXRot(), true);
            event.setPacket(playerMoveC2SPacket);
        }
    }, new ClientPacketFilter(EventPacketSent.Mode.PRE));

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (playerEntity != null) {
            if (hasMoved(playerEntity)) {
                ServerboundMovePlayerPacket playerMoveC2SPacket = new ServerboundMovePlayerPacket.PosRot(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), playerEntity.getYRot(), playerEntity.getXRot(), playerEntity.isOnGround());
                NetworkHelper.INSTANCE.sendPacket(playerMoveC2SPacket);
            }
            playerEntity.tick();
            playerEntity.setDeltaMovement(0, playerEntity.getDeltaMovement().y(), 0);
            if (!playerEntity.isOnGround() && !playerEntity.isInWater() && !playerEntity.isInLava()) {
                playerEntity.setDeltaMovement(0, playerEntity.getDeltaMovement().y() - 0.06499, 0);
            }
            if (Feature.getState(Fly.class))
                playerEntity.setDeltaMovement(0, 0, 0);
            if (!stealth) {
                playerEntity.setItemInHand(InteractionHand.MAIN_HAND, Wrapper.INSTANCE.getLocalPlayer().getMainHandItem());
                playerEntity.setItemInHand(InteractionHand.OFF_HAND, Wrapper.INSTANCE.getLocalPlayer().getOffhandItem());
            }
        }
    }, Priority.FIRST, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventMove> eventMoveEventListener = new EventListener<>(event -> {
        playerEntity.move(MoverType.PLAYER, playerEntity.getDeltaMovement());

        if (!PlayerHelper.INSTANCE.isMoving()) {
            event.setX(0);
            event.setZ(0);
        } else {
            PlayerHelper.INSTANCE.setMoveSpeed(event, speed);
        }
        event.setY(0);
        if (Wrapper.INSTANCE.getOptions().keyShift.isDown())
            event.setY(-speed);
        if (Wrapper.INSTANCE.getOptions().keyJump.isDown())
            event.setY(speed);
    });

    @EventPointer
    private final EventListener<EventPlayerUpdates> eventPlayerUpdatesEventListener = new EventListener<>(event -> {
        Wrapper.INSTANCE.getLocalPlayer().attackAnim += 400.0F;
        Wrapper.INSTANCE.getLocalPlayer().noPhysics = true;
    });

    @EventPointer
    private final EventListener<EventPushOutOfBlocks> eventPushOutOfBlocksEventListener = new EventListener<>(event -> event.cancel());

    @EventPointer
    private final EventListener<EventMarkChunkClosed> eventMarkChunkClosedEventListener = new EventListener<>(event -> event.cancel());

    @EventPointer
    private final EventListener<EventGetPose> eventGetPoseEventListener = new EventListener<>(event -> {
           event.setPose(Pose.STANDING);
           event.cancel();
    });

    @Override
    public void onEnable() {
        if (Wrapper.INSTANCE.getLocalPlayer() != null) {
            Wrapper.INSTANCE.getWorldRenderer().allChanged();
            savedCoords = new Vec3(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ());
            lookVec = new RotationVector(Wrapper.INSTANCE.getLocalPlayer());

            playerEntity = new FakePlayerEntity(Wrapper.INSTANCE.getWorld(), new GameProfile(UUID.randomUUID(), Wrapper.INSTANCE.getMinecraft().getUser().getName()));
            playerEntity.restoreFrom(Wrapper.INSTANCE.getLocalPlayer());
            playerEntity.copyPosition(Wrapper.INSTANCE.getLocalPlayer());
            Wrapper.INSTANCE.getWorld().putNonPlayerEntity(69420, playerEntity);
        }
        super.onEnable();
    }


    @Override
    public void onDisable() {
        super.onDisable();
        if (Wrapper.INSTANCE.getLocalPlayer() != null && resetPos) {
            Wrapper.INSTANCE.getLocalPlayer().noPhysics = false;
            Wrapper.INSTANCE.getWorldRenderer().allChanged();
            if (playerEntity == null) {
                Wrapper.INSTANCE.getLocalPlayer().setPosRaw(savedCoords.x(), savedCoords.y(), savedCoords.z());
                NetworkHelper.INSTANCE.sendPacket(new ServerboundMovePlayerPacket.Pos(savedCoords.x(), savedCoords.y(), savedCoords.z(), false));
            } else {
                Wrapper.INSTANCE.getLocalPlayer().copyPosition(playerEntity);
                NetworkHelper.INSTANCE.sendPacket(new ServerboundMovePlayerPacket.Pos(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), false));
            }
        }
        savedCoords = Vec3.ZERO;
        if (playerEntity != null) {
            playerEntity.setPosRaw(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            if (Wrapper.INSTANCE.getWorld() != null)
                Wrapper.INSTANCE.getWorld().removeEntity(playerEntity.getId(), Entity.RemovalReason.DISCARDED);
            playerEntity = null;
        }
        if (Wrapper.INSTANCE.getLocalPlayer() != null)
            Wrapper.INSTANCE.getLocalPlayer().setDeltaMovement(0, 0, 0);
    }

    private boolean hasMoved(Player playerEntity) {
        return playerEntity.xo != playerEntity.getX() || playerEntity.yo != playerEntity.getY() || playerEntity.zo != playerEntity.getZ() || playerEntity.yRotO != playerEntity.getYRot() || playerEntity.xRotO != playerEntity.getXRot();
    }
}
