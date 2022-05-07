package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.misc.EventControlLlama;
import me.dustin.jex.event.misc.EventHorseIsSaddled;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.load.impl.IAbstractHorseEntity;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Change how ridable entities work.")
public class EntityRider extends Feature {

    @Op(name = "Horse")
    public boolean horse = true;
    @OpChild(name = "Always Saddle", parent = "Horse")
    public boolean alwaysSaddleHorse = true;
    @OpChild(name = "Horse Instant Jump", parent = "Horse")
    public boolean horseInstantJump = true;
    @OpChild(name = "Horse Speed", min = 0.1f, max = 2, inc = 0.05f, parent = "Horse")
    public float horseSpeed = 1;
    @OpChild(name = "Horse Jump", min = 0.1f, max = 2, inc = 0.05f, parent = "Horse")
    public float horseJump = 1;

    @Op(name = "Llama")
    public boolean llama = true;
    @OpChild(name = "Always Saddle", parent = "Llama")
    public boolean alwaysSaddleLlama = true;
    @OpChild(name = "Llama Control", parent = "Llama")
    public boolean llamaControl = true;
    @OpChild(name = "Llama Instant Jump", parent = "Llama")
    public boolean llamaInstantJump = true;
    @OpChild(name = "Llama Speed", min = 0.1f, max = 2, inc = 0.05f, parent = "Llama")
    public float llamaSpeed = 1;
    @OpChild(name = "Llama Jump", min = 0.1f, max = 2, inc = 0.05f, parent = "Llama")
    public float llamaJump = 1;

    @Op(name = "Boat")
    public boolean boat = true;
    @OpChild(name = "Allow Jump/Fly", parent = "Boat")
    public boolean allowBoatFly = true;
    @OpChild(name = "Boat Speed", min = 0.1f, max = 2, inc = 0.05f, parent = "Boat")
    public float boatSpeed = 1;
    @OpChild(name = "Boat Jump", min = 0.1f, max = 2, inc = 0.05f, parent = "Boat")
    public float boatJump = 1;

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getLocalPlayer().getVehicle() == null)
            return;
        Entity vehicle = Wrapper.INSTANCE.getLocalPlayer().getVehicle();
        if (horse && isHorse(vehicle)) {
            AbstractHorse horseBaseEntity = (AbstractHorse) Wrapper.INSTANCE.getLocalPlayer().getVehicle();
            IAbstractHorseEntity iAbstractHorseEntity = (IAbstractHorseEntity) horseBaseEntity;
            iAbstractHorseEntity.setJumpStrength(horseJump);
            iAbstractHorseEntity.setSpeed(horseSpeed);
            if (horseInstantJump)
                iAbstractHorseEntity.setJumpPower(Wrapper.INSTANCE.getOptions().keyJump.isDown() ? 1 : 0);
        }
        if (llama && isLlama(vehicle)) {
            AbstractHorse horseBaseEntity = (AbstractHorse) Wrapper.INSTANCE.getLocalPlayer().getVehicle();
            IAbstractHorseEntity iAbstractHorseEntity = (IAbstractHorseEntity) horseBaseEntity;
            iAbstractHorseEntity.setJumpStrength(llamaJump);
            iAbstractHorseEntity.setSpeed(llamaSpeed);
            if (llamaInstantJump)
                iAbstractHorseEntity.setJumpPower(Wrapper.INSTANCE.getOptions().keyJump.isDown() ? 1 : 0);
        }
        if (boat && vehicle instanceof Boat boatEntity) {
            boatEntity.moveRelative(boatSpeed / 10.0f, new Vec3(Wrapper.INSTANCE.getLocalPlayer().input.leftImpulse, 0, Wrapper.INSTANCE.getLocalPlayer().input.forwardImpulse));
            if (allowBoatFly)
                if (Wrapper.INSTANCE.getOptions().keyJump.isDown()) {
                    boatEntity.push(0, boatJump / 10.0f, 0);
                } else if (KeyboardHelper.INSTANCE.isPressed(GLFW.GLFW_KEY_INSERT)) {
                    boatEntity.push(0, -boatJump / 10.0f, 0);
                }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventHorseIsSaddled> eventHorseIsSaddledEventListener = new EventListener<>(event -> {
        if (horse && alwaysSaddleHorse && isHorse(event.getEntity())) {
            ((EventHorseIsSaddled) event).setSaddled(true);
            event.cancel();
        }
        if (llama && alwaysSaddleLlama && isLlama(event.getEntity())) {
            ((EventHorseIsSaddled) event).setSaddled(true);
            event.cancel();
        }
    });

    @EventPointer
    private final EventListener<EventControlLlama> eventControlLlamaEventListener = new EventListener<>(event -> {
        event.setControl(llamaControl);
        event.cancel();
    });

    private boolean isHorse(Entity entity) {
        return entity instanceof Horse || entity instanceof Donkey || entity instanceof Mule || entity instanceof SkeletonHorse;
    }

    private boolean isLlama(Entity entity) {
        return entity instanceof Llama;
    }
}
