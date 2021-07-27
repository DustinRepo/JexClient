package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.misc.EventHorseIsSaddled;
import me.dustin.jex.load.impl.IHorseBaseEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.HorseBaseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(HorseBaseEntity.class)
public class MixinHorseBaseEntity implements IHorseBaseEntity {

    @Shadow
    protected float jumpStrength;

    @Inject(method = "isSaddled", at = @At("HEAD"), cancellable = true)
    public void isSaddled(CallbackInfoReturnable<Boolean> ci) {
        EventHorseIsSaddled eventHorseIsSaddled = new EventHorseIsSaddled((HorseBaseEntity) (Object) this).run();
        if (eventHorseIsSaddled.isCancelled())
            ci.setReturnValue(eventHorseIsSaddled.isSaddled());
    }

    @Override
    public void setJumpPower(double power) {
        this.jumpStrength = (float) power;
    }

    @Override
    public void setJumpStrength(double strength) {
        Objects.requireNonNull(((HorseBaseEntity) (Object) this).getAttributeInstance(EntityAttributes.HORSE_JUMP_STRENGTH)).setBaseValue(strength);
    }

    @Override
    public void setSpeed(double speed) {
        Objects.requireNonNull(((HorseBaseEntity) (Object) this).getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).setBaseValue(speed);
    }
}
