package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.misc.EventHorseIsSaddled;
import me.dustin.jex.load.impl.IAbstractHorseEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(AbstractHorse.class)
public class MixinAbstractHorse implements IAbstractHorseEntity {

    @Shadow protected float playerJumpPendingScale;

    @Inject(method = "isSaddled", at = @At("HEAD"), cancellable = true)
    public void isSaddled(CallbackInfoReturnable<Boolean> ci) {
        EventHorseIsSaddled eventHorseIsSaddled = new EventHorseIsSaddled((AbstractHorse) (Object) this).run();
        if (eventHorseIsSaddled.isCancelled())
            ci.setReturnValue(eventHorseIsSaddled.isSaddled());
    }

    @Override
    public void setJumpPower(double power) {
        this.playerJumpPendingScale = (float) power;
    }

    @Override
    public void setJumpStrength(double strength) {
        Objects.requireNonNull(((AbstractHorse) (Object) this).getAttribute(Attributes.JUMP_STRENGTH)).setBaseValue(strength);
    }

    @Override
    public void setSpeed(double speed) {
        Objects.requireNonNull(((AbstractHorse) (Object) this).getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(speed);
    }
}
