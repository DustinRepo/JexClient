package me.dustin.jex.feature.mod.impl.movement.speed;

import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.extension.FeatureExtension;
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
            .value(0.6f)
            .min(0.3f)
            .max(3)
            .inc(0.01f)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.VANILLA)
            .build();
    public final Property<Float> strafeSpeedProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Strafe Speed")
            .value(0.6f)
            .min(0.3f)
            .max(3)
            .inc(0.01f)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.STRAFE)
            .build();
    public final Property<Float> hopAmountProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Hop Amount")
            .value(0.42f)
            .min(0.05f)
            .inc(0.01f)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.STRAFE)
            .build();

    private Mode lastMode;

    public Speed() {
        super(Category.MOVEMENT, "Sanic gotta go fast.", GLFW.GLFW_KEY_C);
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
        VANILLA, STRAFE
    }
}
