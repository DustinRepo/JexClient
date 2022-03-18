package me.dustin.jex.feature.mod.impl.movement.fly;

import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.events.core.priority.Priority;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventGetPose;
import me.dustin.jex.event.player.EventIsPlayerTouchingWater;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.fly.impl.CreativeFly;
import me.dustin.jex.feature.mod.impl.movement.fly.impl.NormalFly;
import me.dustin.jex.feature.mod.impl.movement.fly.impl.ThreeDFly;
import me.dustin.jex.feature.mod.impl.movement.fly.impl.TightFly;
import me.dustin.jex.feature.mod.impl.player.Freecam;
import me.dustin.jex.feature.mod.impl.world.Excavator;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import me.dustin.jex.helper.world.PathingHelper;
import me.dustin.jex.helper.world.wurstpathfinder.PathProcessor;
import net.minecraft.entity.EntityPose;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.lwjgl.glfw.GLFW;

@Feature.Manifest(category = Feature.Category.MOVEMENT, description = "Fly in survival", key = GLFW.GLFW_KEY_F)
public class Fly extends Feature {

    @Op(name = "Mode", all = {"Normal", "Creative", "Tight", "3D"})
    public String mode = "Normal";
    @Op(name = "Speed", min = 0.1f, max = 5f, inc = 0.1f)
    public float speed = 0.5f;
    @Op(name = "Walk Animation")
    public boolean walkAnimation = true;
    @Op(name = "Fly Check Bypass")
    public boolean flyCheckBypass;
    @Op(name = "Glide")
    public boolean glide = false;
    @OpChild(name = "Glide Speed", min = 0.01f, max = 2, inc = 0.01f, parent = "Glide")
    public float glideSpeed = 0.034f;

    private float strideDistance;
    private String lastMode;

    public Fly() {
        new NormalFly();
        new TightFly();
        new ThreeDFly();
        new CreativeFly();
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (walkAnimation) {
            float g;
            if (!Wrapper.INSTANCE.getLocalPlayer().isDead() && !Wrapper.INSTANCE.getLocalPlayer().isSwimming()) {
                g = Math.min(0.1F, (float) Wrapper.INSTANCE.getLocalPlayer().getVelocity().horizontalLength());
            } else {
                g = 0.0F;
            }

            float lastStrideDist = strideDistance;
            strideDistance += (g - strideDistance) * 0.4F;
            Wrapper.INSTANCE.getLocalPlayer().strideDistance = strideDistance;
            Wrapper.INSTANCE.getLocalPlayer().prevStrideDistance = lastStrideDist;
        }
        sendEvent(event);
        this.setSuffix(mode);
    }, Priority.LAST, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventMove> eventMoveEventListener = new EventListener<>(event -> {
        sendEvent(event);
    });

    @EventPointer
    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
        if (!flyCheckBypass || Feature.getState(Freecam.class))
            return;
        PlayerMoveC2SPacket playerMoveC2SPacket = (PlayerMoveC2SPacket) event.getPacket();
        if (Wrapper.INSTANCE.getLocalPlayer().age % 3 == 1) {
            if (EntityHelper.INSTANCE.distanceFromGround(Wrapper.INSTANCE.getLocalPlayer()) > 2) {
                PlayerMoveC2SPacket modified = new PlayerMoveC2SPacket.Full(playerMoveC2SPacket.getX(Wrapper.INSTANCE.getLocalPlayer().getX()), playerMoveC2SPacket.getY(Wrapper.INSTANCE.getLocalPlayer().getY()) - 0.1, playerMoveC2SPacket.getZ(Wrapper.INSTANCE.getLocalPlayer().getZ()), playerMoveC2SPacket.getYaw(PlayerHelper.INSTANCE.getYaw()), playerMoveC2SPacket.getPitch(PlayerHelper.INSTANCE.getPitch()), true);
                event.setPacket(modified);
            }
        }
    }, new ClientPacketFilter(EventPacketSent.Mode.PRE, PlayerMoveC2SPacket.class));

    @EventPointer
    private final EventListener<EventIsPlayerTouchingWater> eventIsPlayerTouchingWaterEventListener = new EventListener<>(event -> {
        event.setTouchingWater(false);
        event.cancel();
    });

    @EventPointer
    private final EventListener<EventGetPose> eventGetPoseEventListener = new EventListener<>(event -> {
        if (event.getPose() == EntityPose.SWIMMING) {
            event.setPose(EntityPose.STANDING);
            event.cancel();
        }
    });

    private void sendEvent(Event event) {
        if (!mode.equalsIgnoreCase(lastMode) && lastMode != null) {
            FeatureExtension.get(lastMode, this).disable();
            FeatureExtension.get(mode, this).enable();
        }
        FeatureExtension.get(mode, this).pass(event);
        lastMode = mode;
    }

    @Override
    public void onEnable() {
        FeatureExtension.get(mode, this).enable();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        FeatureExtension.get(mode, this).disable();
        super.onDisable();
    }
}
