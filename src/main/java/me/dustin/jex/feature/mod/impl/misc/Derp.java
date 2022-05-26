package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import me.dustin.jex.feature.mod.core.Feature;
import java.util.Random;

public class Derp extends Feature {

    public final Property<Mode> modeProperty = new Property.PropertyBuilder<Mode>(this.getClass())
            .name("Mode")
            .value(Mode.RANDOM)
            .build();
    public final Property<Boolean> normalizeAnglesProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Normalize Angles")
            .description("Keeps your angles vanilla.")
            .value(true)
            .build();
    public final Property<Boolean> swingProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Swing")
            .value(true)
            .build();
    public final Property<Boolean> showSwingProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Show Swing")
            .description("Show your arm swinging client-side.")
            .value(true)
            .parent(swingProperty)
            .depends(parent -> (boolean) parent.value())
            .build();

    private int yaw, pitch;

    public Derp() {
        super(Category.MISC, "Randomly look around for other players");
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        Random random = new Random();
        switch (modeProperty.value()) {
            case RANDOM -> {
                event.setYaw(random.nextFloat() * 180);
                if (random.nextBoolean())
                    event.setYaw(-event.getYaw());
                event.setPitch(random.nextFloat() * 90);
                if (random.nextBoolean())
                    event.setPitch(-event.getPitch());
            }
            case PITCH_ROLL -> {
                pitch++;
                event.setPitch(pitch);
                if (pitch > 90)
                    pitch = -90;
            }
            case YAW_ROLL -> {
                yaw++;
                event.setYaw(yaw);
            }
            case BOTH_ROLL -> {
                pitch++;
                yaw++;
                event.setYaw(yaw);
                event.setPitch(pitch);
                if (pitch > 90)
                    pitch = -90;
            }
        }
        if (normalizeAnglesProperty.value()) {
            RotationVector rotation = event.getRotation();
            rotation.normalize();
            event.setRotation(rotation);
        }
        if (swingProperty.value()) {
            Hand hand = random.nextBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
            if (showSwingProperty.value()) {
                Wrapper.INSTANCE.getLocalPlayer().swingHand(hand);
            } else {
                NetworkHelper.INSTANCE.sendPacket(new HandSwingC2SPacket(hand));
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    public enum Mode {
        RANDOM, PITCH_ROLL, YAW_ROLL, BOTH_ROLL
    }
}
