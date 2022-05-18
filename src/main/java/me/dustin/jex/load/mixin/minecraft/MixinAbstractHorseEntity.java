package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.misc.EventHorseIsSaddled;
import me.dustin.jex.load.impl.IAbstractHorseEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.AbstractHorseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(AbstractHorseEntity.class)
public class MixinAbstractHorseEntity implements IAbstractHorseEntity {

    @Shadow
    protected float jumpStrength;

    @Inject(method = "isSaddled", at = @At("HEAD"), cancellable = true)
    public void isSaddled(CallbackInfoReturnable<Boolean> ci) {
        EventHorseIsSaddled eventHorseIsSaddled = new EventHorseIsSaddled((AbstractHorseEntity) (Object) this).run();
        if (eventHorseIsSaddled.isCancelled())
            ci.setReturnValue(eventHorseIsSaddled.isSaddled());
    }

    @Override
    public void setJumpPower(double power) {
        this.jumpStrength = (float) power;
    }

    @Override
    public void setJumpStrength(double strength) {
        Objects.requireNonNull(((AbstractHorseEntity) (Object) this).getAttributeInstance(EntityAttributes.HORSE_JUMP_STRENGTH)).setBaseValue(strength);
    }

    @Override
    public void setSpeed(double speed) {
        Objects.requireNonNull(((AbstractHorseEntity) (Object) this).getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).setBaseValue(speed);
    }
}
