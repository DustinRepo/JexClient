package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.misc.EventControlLlama;
import me.dustin.jex.event.misc.EventHorseIsSaddled;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.load.impl.IAbstractHorseEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.MuleEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;
import me.dustin.jex.feature.mod.core.Feature;
import org.lwjgl.glfw.GLFW;

public class EntityRider extends Feature {

    public final Property<Boolean> horseProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Horse")
            .value(true)
            .build();
    public final Property<Boolean> alwaysSaddleHorseProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Horse Saddle")
            .value(true)
            .parent(horseProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> horseInstantJumpProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Horse Instant Jump")
            .value(true)
            .parent(horseProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Float> horseSpeedProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Horse Speed")
            .value(1f)
            .min(0.1f)
            .max(2)
            .inc(0.02f)
            .parent(horseProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Float> horseJumpProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Horse Jump")
            .value(1f)
            .min(0.1f)
            .max(2)
            .inc(0.02f)
            .parent(horseProperty)
            .depends(parent -> (boolean) parent.value())
            .build();

    public final Property<Boolean> llamaProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Llama")
            .value(true)
            .build();
    public final Property<Boolean> alwaysSaddleLlamaProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Llama Saddle")
            .value(true)
            .parent(llamaProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> llamaControlProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Llama Control")
            .value(true)
            .parent(llamaProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> llamaInstantJumpProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Llama Instant Jump")
            .value(true)
            .parent(llamaProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Float> llamaSpeedProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Llama Speed")
            .value(1f)
            .min(0.1f)
            .max(2)
            .inc(0.02f)
            .parent(llamaProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Float> llamaJumpProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Llama Jump")
            .value(1f)
            .min(0.1f)
            .max(2)
            .inc(0.02f)
            .parent(llamaProperty)
            .depends(parent -> (boolean) parent.value())
            .build();

    public final Property<Boolean> boatProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Boat")
            .value(true)
            .build();
    public final Property<Boolean> allowBoatFlyProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Boat")
            .value(true)
            .parent(boatProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Float> boatSpeedProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Boat Speed")
            .value(1f)
            .min(0.1f)
            .max(2)
            .inc(0.02f)
            .parent(boatProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Float> boatJumpProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Boat Jump")
            .value(1f)
            .min(0.1f)
            .max(2)
            .inc(0.02f)
            .parent(boatProperty)
            .depends(parent -> (boolean) parent.value())
            .build();

    public EntityRider() {
        super(Category.WORLD, "Change how ridable entities work.");
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getLocalPlayer().getVehicle() == null)
            return;
        Entity vehicle = Wrapper.INSTANCE.getLocalPlayer().getVehicle();
        if (horseProperty.value() && isHorse(vehicle)) {
            AbstractHorseEntity horseBaseEntity = (AbstractHorseEntity) Wrapper.INSTANCE.getLocalPlayer().getVehicle();
            IAbstractHorseEntity iAbstractHorseEntity = (IAbstractHorseEntity) horseBaseEntity;
            iAbstractHorseEntity.setJumpStrength(horseJumpProperty.value());
            iAbstractHorseEntity.setSpeed(horseSpeedProperty.value());
            if (horseInstantJumpProperty.value())
                iAbstractHorseEntity.setJumpPower(Wrapper.INSTANCE.getOptions().jumpKey.isPressed() ? 1 : 0);
        }
        if (llamaProperty.value() && isLlama(vehicle)) {
            AbstractHorseEntity horseBaseEntity = (AbstractHorseEntity) Wrapper.INSTANCE.getLocalPlayer().getVehicle();
            IAbstractHorseEntity iAbstractHorseEntity = (IAbstractHorseEntity) horseBaseEntity;
            iAbstractHorseEntity.setJumpStrength(llamaJumpProperty.value());
            iAbstractHorseEntity.setSpeed(llamaSpeedProperty.value());
            if (llamaInstantJumpProperty.value())
                iAbstractHorseEntity.setJumpPower(Wrapper.INSTANCE.getOptions().jumpKey.isPressed() ? 1 : 0);
        }
        if (boatProperty.value() && vehicle instanceof BoatEntity boatEntity) {
            boatEntity.updateVelocity(boatSpeedProperty.value() / 10.0f, new Vec3d(Wrapper.INSTANCE.getLocalPlayer().input.movementSideways, 0, Wrapper.INSTANCE.getLocalPlayer().input.movementForward));
            if (allowBoatFlyProperty.value())
                if (Wrapper.INSTANCE.getOptions().jumpKey.isPressed()) {
                    boatEntity.addVelocity(0, boatJumpProperty.value() / 10.0f, 0);
                } else if (KeyboardHelper.INSTANCE.isPressed(GLFW.GLFW_KEY_INSERT)) {
                    boatEntity.addVelocity(0, -boatJumpProperty.value() / 10.0f, 0);
                }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventHorseIsSaddled> eventHorseIsSaddledEventListener = new EventListener<>(event -> {
        if (horseProperty.value() && alwaysSaddleHorseProperty.value() && isHorse(event.getEntity())) {
            ((EventHorseIsSaddled) event).setSaddled(true);
            event.cancel();
        }
        if (llamaProperty.value() && alwaysSaddleLlamaProperty.value() && isLlama(event.getEntity())) {
            ((EventHorseIsSaddled) event).setSaddled(true);
            event.cancel();
        }
    });

    @EventPointer
    private final EventListener<EventControlLlama> eventControlLlamaEventListener = new EventListener<>(event -> {
        event.setControl(llamaControlProperty.value());
        event.cancel();
    });

    private boolean isHorse(Entity entity) {
        return entity instanceof HorseEntity || entity instanceof DonkeyEntity || entity instanceof MuleEntity || entity instanceof SkeletonHorseEntity;
    }

    private boolean isLlama(Entity entity) {
        return entity instanceof LlamaEntity;
    }
}
