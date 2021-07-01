package me.dustin.jex.load.mixin;

import me.dustin.jex.event.render.EventBlockBrightness;
import me.dustin.jex.event.render.EventIsBlockOpaque;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractBlock.AbstractBlockState.class)
public abstract class MixinAbstractBlockstate {

    @Shadow
    @Final
    private int luminance;

    @Shadow
    @Final
    private boolean opaque;

    @Shadow public abstract Block getBlock();

    @Inject(method = "getLuminance", at = @At("HEAD"), cancellable = true)
    public void getLuminance1(CallbackInfoReturnable<Integer> cir) {
        EventBlockBrightness eventBlockBrightness = new EventBlockBrightness(this.getBlock(), this.luminance).run();
        cir.setReturnValue(eventBlockBrightness.getBrightness());
    }

    @Inject(method = "isOpaque", at = @At("HEAD"), cancellable = true)
    public void isOpaque1(CallbackInfoReturnable<Boolean> cir) {
        EventIsBlockOpaque eventIsBlockOpaque = new EventIsBlockOpaque(opaque).run();
        cir.setReturnValue(eventIsBlockOpaque.isOpaque());
    }
}
