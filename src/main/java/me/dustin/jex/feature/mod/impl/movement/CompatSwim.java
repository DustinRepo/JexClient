package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.events.core.priority.Priority;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventGetPose;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.player.Jesus;

@Feature.Manifest(category = Feature.Category.MOVEMENT, description = "Change swim speed to work on pre 1.13 servers with anticheats")
public class CompatSwim extends Feature {

    @EventPointer
    private final EventListener<EventMove> eventMoveEventListener = new EventListener<>(event -> {
        if (WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getLocalPlayer())) {
            PlayerHelper.INSTANCE.setMoveSpeed(event, PlayerHelper.INSTANCE.getWaterSpeed());
        }
    }, Priority.SECOND);

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getLocalPlayer()) && !Feature.get(Jesus.class).getState()) {
            Vec3 orig = Wrapper.INSTANCE.getLocalPlayer().getDeltaMovement();
            if (Wrapper.INSTANCE.getOptions().keyJump.isDown()) {
                double y = ClientMathHelper.INSTANCE.cap((float) orig.y(), 0, Wrapper.INSTANCE.getLocalPlayer().horizontalCollision ? 0.07f : 0.011f);
                Wrapper.INSTANCE.getLocalPlayer().setDeltaMovement(orig.x(), y, orig.z());
            } else if (!Wrapper.INSTANCE.getLocalPlayer().isShiftKeyDown() && Wrapper.INSTANCE.getLocalPlayer().isSwimming()) {
                Wrapper.INSTANCE.getLocalPlayer().setDeltaMovement(orig.x(), -0.025, orig.z());
            }
        }
    }, Priority.SECOND, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventGetPose> eventGetPoseEventListener = new EventListener<>(event -> {
        if (event.getPose() == Pose.SWIMMING) {
            event.setPose(Pose.STANDING);
            event.cancel();
        }
    });
}