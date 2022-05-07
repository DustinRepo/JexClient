package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.player.EventCurrentItemAttackStrengthDelay;
import me.dustin.jex.event.player.EventWalkOffBlock;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class MixinPlayer {

    @Inject(method = "getCurrentItemAttackStrengthDelay", at = @At("HEAD"), cancellable = true)
    public void getAttackCooldownProgress(CallbackInfoReturnable<Float> ci) {
        EventCurrentItemAttackStrengthDelay eventCurrentItemAttackStrengthDelay = new EventCurrentItemAttackStrengthDelay().run();
        if (eventCurrentItemAttackStrengthDelay.getValue() != -1)
            ci.setReturnValue((float) (eventCurrentItemAttackStrengthDelay.getValue() / ((Player) (Object) this).getAttribute(Attributes.ATTACK_SPEED).getValue() * 20.0D));
    }

    @Inject(method = "isStayingOnGroundSurface", at = @At("HEAD"), cancellable = true)
    public void clipAtLedge(CallbackInfoReturnable<Boolean> cir) {
        EventWalkOffBlock eventWalkOffBlock = new EventWalkOffBlock().run();
        cir.setReturnValue(((Player) (Object) this).isShiftKeyDown() || eventWalkOffBlock.isCancelled());
    }
}
