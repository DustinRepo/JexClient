package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.render.EventBlockBrightness;
import me.dustin.jex.event.render.EventIsBlockOpaque;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class MixinBlockStateBase {

    @Shadow public abstract Block getBlock();

    @Shadow @Final private int lightEmission;

    @Shadow @Final private boolean canOcclude;

    @Inject(method = "getLightEmission", at = @At("HEAD"), cancellable = true)
    public void getLuminance1(CallbackInfoReturnable<Integer> cir) {
        EventBlockBrightness eventBlockBrightness = new EventBlockBrightness(this.getBlock(), this.lightEmission).run();
        cir.setReturnValue(eventBlockBrightness.getBrightness());
    }

    @Inject(method = "canOcclude", at = @At("HEAD"), cancellable = true)
    public void isOpaque1(CallbackInfoReturnable<Boolean> cir) {
        EventIsBlockOpaque eventIsBlockOpaque = new EventIsBlockOpaque(canOcclude).run();
        cir.setReturnValue(eventIsBlockOpaque.isOpaque());
    }
}
