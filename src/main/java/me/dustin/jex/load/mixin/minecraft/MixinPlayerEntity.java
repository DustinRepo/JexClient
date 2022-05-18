package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.player.EventCurrentItemAttackStrengthDelay;
import me.dustin.jex.event.player.EventWalkOffBlock;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity {

    @Inject(method = "getAttackCooldownProgressPerTick", at = @At("HEAD"), cancellable = true)
    public void getAttackCooldownProgress(CallbackInfoReturnable<Float> ci) {
        EventCurrentItemAttackStrengthDelay eventCurrentItemAttackStrengthDelay = new EventCurrentItemAttackStrengthDelay().run();
        if (eventCurrentItemAttackStrengthDelay.getValue() != -1)
            ci.setReturnValue((float) (eventCurrentItemAttackStrengthDelay.getValue() / ((PlayerEntity) (Object) this).getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED).getValue() * 20.0D));
    }

    @Inject(method = "clipAtLedge", at = @At("HEAD"), cancellable = true)
    public void clipAtLedge(CallbackInfoReturnable<Boolean> cir) {
        EventWalkOffBlock eventWalkOffBlock = new EventWalkOffBlock().run();
        cir.setReturnValue(((PlayerEntity) (Object) this).isSneaking() || eventWalkOffBlock.isCancelled());
    }
}
