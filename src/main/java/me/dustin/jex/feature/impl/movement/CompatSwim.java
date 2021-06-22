package me.dustin.jex.feature.impl.movement;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.events.core.enums.EventPriority;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.feature.impl.player.Jesus;
import net.minecraft.util.math.Vec3d;

@Feat(name = "CompatSwim", category = FeatureCategory.MOVEMENT, description = "Change swim speed to work on pre 1.13 servers with anticheats")
public class CompatSwim extends Feature {

    @EventListener(events = {EventMove.class, EventPlayerPackets.class}, priority = EventPriority.HIGH)
    private void runMethod(Event event) {
        if (event instanceof EventMove eventMove) {

            if (WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getLocalPlayer())) {
                PlayerHelper.INSTANCE.setMoveSpeed(eventMove, PlayerHelper.INSTANCE.getWaterSpeed());
            }
        }
        if (event instanceof EventPlayerPackets) {
            if (((EventPlayerPackets) event).getMode() == EventPlayerPackets.Mode.PRE) {
                if (WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getLocalPlayer()) && !Feature.get(Jesus.class).getState()) {
                    Vec3d orig = Wrapper.INSTANCE.getLocalPlayer().getVelocity();
                    if (Wrapper.INSTANCE.getOptions().keyJump.isPressed()) {
                        double y = ClientMathHelper.INSTANCE.cap((float) orig.getY(), 0, Wrapper.INSTANCE.getLocalPlayer().horizontalCollision ? 0.07f : 0.011f);
                        Wrapper.INSTANCE.getLocalPlayer().setVelocity(orig.getX(), y, orig.getZ());
                    } else if (!Wrapper.INSTANCE.getLocalPlayer().isSneaking() && Wrapper.INSTANCE.getLocalPlayer().isSwimming()) {
                        Wrapper.INSTANCE.getLocalPlayer().setVelocity(orig.getX(), -0.025, orig.getZ());
                    }
                }
            }
        }
    }
}