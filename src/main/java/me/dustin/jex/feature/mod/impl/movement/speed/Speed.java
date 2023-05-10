package me.dustin.jex.feature.mod.impl.movement.speed;

import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.FeatureExtension;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.speed.impl.StrafeSpeed;
import me.dustin.jex.feature.mod.impl.movement.speed.impl.VanillaSpeed;
import me.dustin.jex.feature.property.Property;
import org.lwjgl.glfw.GLFW;

public class Speed extends Feature {
    public static Speed INSTANCE;

    public final Property<Mode> modeProperty = new Property.PropertyBuilder<Mode>(this.getClass())
            .name("Mode")
            .value(Mode.VANILLA)
            .build();
    public final Property<Float> vanillaSpeedProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Vanilla Speed")
            .value(1f)
            .min(0.1f)
            .max(20f)
            .inc(0.1f)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.VANILLA)
            .build();
     public final Property<Long> spaceSpeedProperty = new Property.PropertyBuilder<Long>(this.getClass())
            .name("Space Speed")
            .value(110L)
            .min(110L)
            .max(15000000L)
            .inc(150000L)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.SPACE)
            .build();
    public final Property<Float> strafeSpeedProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Strafe Speed")
            .value(1f)
            .min(0.1f)
            .max(20f)
            .inc(0.1f)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.STRAFE)
            .build();
    public final Property<Float> hopAmountProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Hop Amount")
            .value(0.05f)
            .min(0.04f)
            .max(1f)
            .inc(0.01f)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.STRAFE)
            .build();

    private Mode lastMode;

    public Speed() {
        super(Category.MOVEMENT, "", GLFW.GLFW_KEY_C);
        new StrafeSpeed();
        new VanillaSpeed();
        INSTANCE = this;
    }

    @EventPointer
    private final EventListener<EventMove> eventMoveEventListener = new EventListener<>(event -> sendEvent(event));

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        this.setSuffix(modeProperty.value());
        sendEvent(event);
    });

    @EventPointer
    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> sendEvent(event));

    private void sendEvent(Event event) {
        if (modeProperty.value() != lastMode && lastMode != null) {
            FeatureExtension.get(lastMode, this).disable();
            FeatureExtension.get(modeProperty.value(), this).enable();
        }
        FeatureExtension.get(modeProperty.value(), this).pass(event);
        lastMode = modeProperty.value();
    }

    @Override
    public void onEnable() {
        FeatureExtension.get(modeProperty.value(), this).enable();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        FeatureExtension.get(modeProperty.value(), this).disable();
        super.onDisable();
    }

    public enum Mode {
        VANILLA, STRAFE, SPACE
    }
}
