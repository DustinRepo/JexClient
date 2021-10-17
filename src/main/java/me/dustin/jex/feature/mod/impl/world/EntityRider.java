package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventControlLlama;
import me.dustin.jex.event.misc.EventHorseIsSaddled;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.load.impl.IHorseBaseEntity;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;
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

    @EventListener(events = {EventPlayerPackets.class, EventHorseIsSaddled.class, EventControlLlama.class})
    private void runMethod(Event event) {
        if (event instanceof EventPlayerPackets) {
            if (Wrapper.INSTANCE.getLocalPlayer().getVehicle() == null)
                return;
            Entity vehicle = Wrapper.INSTANCE.getLocalPlayer().getVehicle();
            if (horse && isHorse(vehicle)) {
                HorseBaseEntity horseBaseEntity = (HorseBaseEntity) Wrapper.INSTANCE.getLocalPlayer().getVehicle();
                IHorseBaseEntity iHorseBaseEntity = (IHorseBaseEntity) horseBaseEntity;
                iHorseBaseEntity.setJumpStrength(horseJump);
                iHorseBaseEntity.setSpeed(horseSpeed);
                if (horseInstantJump)
                    iHorseBaseEntity.setJumpPower(Wrapper.INSTANCE.getOptions().keyJump.isPressed() ? 1 : 0);
            }
            if (llama && isLlama(vehicle)) {
                HorseBaseEntity horseBaseEntity = (HorseBaseEntity) Wrapper.INSTANCE.getLocalPlayer().getVehicle();
                IHorseBaseEntity iHorseBaseEntity = (IHorseBaseEntity) horseBaseEntity;
                iHorseBaseEntity.setJumpStrength(llamaJump);
                iHorseBaseEntity.setSpeed(llamaSpeed);
                if (llamaInstantJump)
                    iHorseBaseEntity.setJumpPower(Wrapper.INSTANCE.getOptions().keyJump.isPressed() ? 1 : 0);
            }
            if (boat && vehicle instanceof BoatEntity boatEntity) {
                boatEntity.updateVelocity(boatSpeed / 10.0f, new Vec3d(Wrapper.INSTANCE.getLocalPlayer().input.movementSideways, 0, Wrapper.INSTANCE.getLocalPlayer().input.movementForward));
                if (allowBoatFly)
                    if (Wrapper.INSTANCE.getOptions().keyJump.isPressed()) {
                        boatEntity.addVelocity(0, boatJump / 10.0f, 0);
                    } else if (KeyboardHelper.INSTANCE.isPressed(GLFW.GLFW_KEY_INSERT)) {
                        boatEntity.addVelocity(0, -boatJump / 10.0f, 0);
                    }
            }
        }
        if (event instanceof EventHorseIsSaddled) {
            if (horse && alwaysSaddleHorse && isHorse(((EventHorseIsSaddled) event).getEntity())) {
                ((EventHorseIsSaddled) event).setSaddled(true);
                event.cancel();
            }
            if (llama && alwaysSaddleLlama && isLlama(((EventHorseIsSaddled) event).getEntity())) {
                ((EventHorseIsSaddled) event).setSaddled(true);
                event.cancel();
            }
        }
        if (event instanceof EventControlLlama) {
            ((EventControlLlama) event).setControl(llamaControl);
            event.cancel();
        }
    }

    private boolean isHorse(Entity entity) {
        return entity instanceof HorseEntity || entity instanceof DonkeyEntity || entity instanceof MuleEntity || entity instanceof SkeletonHorseEntity;
    }

    private boolean isLlama(Entity entity) {
        return entity instanceof LlamaEntity;
    }
}
