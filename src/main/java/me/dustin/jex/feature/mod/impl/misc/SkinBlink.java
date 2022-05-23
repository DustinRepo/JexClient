package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.render.entity.PlayerModelPart;
import me.dustin.jex.feature.mod.core.Feature;
import java.util.ArrayList;
import java.util.Random;

public class SkinBlink extends Feature {

    public final Property<Mode> modeProperty = new Property.PropertyBuilder<Mode>(this.getClass())
            .name("Mode")
            .value(Mode.RANDOM)
            .build();
    public final Property<Boolean> headProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Head")
            .description("Enables toggling the head cover")
            .value(true)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.CUSTOM)
            .build();
    public final Property<Boolean> jacketProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Jacket")
            .description("Enables toggling the jacket cover")
            .value(true)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.CUSTOM)
            .build();
    public final Property<Boolean> capeProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Cape")
            .description("Enables toggling the cape")
            .value(true)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.CUSTOM)
            .build();
    public final Property<Boolean> leftArmProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Left Arm")
            .description("Enables toggling the left arm cover")
            .value(true)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.CUSTOM)
            .build();
    public final Property<Boolean> leftLegProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Left Leg")
            .description("Enables toggling the left leg cover")
            .value(true)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.CUSTOM)
            .build();
    public final Property<Boolean> rightArmProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Right Arm")
            .description("Enables toggling the right arm cover")
            .value(true)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.CUSTOM)
            .build();
    public final Property<Boolean> rightLegProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Right Leg")
            .description("Enables toggling the right leg cover")
            .value(true)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.CUSTOM)
            .build();
    public final Property<Long> delayProperty = new Property.PropertyBuilder<Long>(this.getClass())
            .name("Delay (MS)")
            .value(250L)
            .min(50)
            .max(5000)
            .build();

    private final Random random = new Random();
    private final ArrayList<PlayerModelPart> savedEnabled = new ArrayList<>();
    private final StopWatch stopWatch = new StopWatch();
    private boolean toggleCustom;

    public SkinBlink() {
        super(Category.MISC, "Make your skin flash your layers on and off");
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (!stopWatch.hasPassed(delayProperty.value()))
            return;
        switch (modeProperty.value()) {
            case RANDOM:
                for (PlayerModelPart value : PlayerModelPart.values()) {
                    Wrapper.INSTANCE.getOptions().togglePlayerModelPart(value, random.nextBoolean());
                }
                break;
            case FULL_FLASH:
                boolean on = Wrapper.INSTANCE.getOptions().isPlayerModelPartEnabled(PlayerModelPart.HAT);
                for (PlayerModelPart value : PlayerModelPart.values()) {
                    Wrapper.INSTANCE.getOptions().togglePlayerModelPart(value, on);
                }
                break;
            case CUSTOM:
                if (headProperty.value())
                    Wrapper.INSTANCE.getOptions().togglePlayerModelPart(PlayerModelPart.HAT, toggleCustom);
                if (capeProperty.value())
                    Wrapper.INSTANCE.getOptions().togglePlayerModelPart(PlayerModelPart.CAPE, toggleCustom);
                if (jacketProperty.value())
                    Wrapper.INSTANCE.getOptions().togglePlayerModelPart(PlayerModelPart.JACKET, toggleCustom);
                if (leftArmProperty.value())
                    Wrapper.INSTANCE.getOptions().togglePlayerModelPart(PlayerModelPart.LEFT_SLEEVE, toggleCustom);
                if (leftLegProperty.value())
                    Wrapper.INSTANCE.getOptions().togglePlayerModelPart(PlayerModelPart.LEFT_PANTS_LEG, toggleCustom);
                if (rightArmProperty.value())
                    Wrapper.INSTANCE.getOptions().togglePlayerModelPart(PlayerModelPart.RIGHT_SLEEVE, toggleCustom);
                if (rightLegProperty.value())
                    Wrapper.INSTANCE.getOptions().togglePlayerModelPart(PlayerModelPart.RIGHT_PANTS_LEG, toggleCustom);
                toggleCustom = !toggleCustom;
                break;
        }
        stopWatch.reset();
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @Override
    public void onEnable() {
        if (Wrapper.INSTANCE.getOptions() != null) {
            savedEnabled.clear();
            for (PlayerModelPart playerModelPart : PlayerModelPart.values()) {
                if (Wrapper.INSTANCE.getOptions().isPlayerModelPartEnabled(playerModelPart))
                    savedEnabled.add(playerModelPart);
            }
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (Wrapper.INSTANCE.getLocalPlayer() != null) {
            for (PlayerModelPart value : PlayerModelPart.values()) {
                Wrapper.INSTANCE.getOptions().togglePlayerModelPart(value, savedEnabled.contains(value));
            }
        }
        super.onDisable();
    }

    public enum Mode {
        RANDOM, FULL_FLASH, CUSTOM
    }
}
