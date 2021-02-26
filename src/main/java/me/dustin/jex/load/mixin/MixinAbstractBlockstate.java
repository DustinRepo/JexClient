package me.dustin.jex.load.mixin;

import me.dustin.jex.event.render.EventBlockBrightness;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class MixinAbstractBlockstate {

    @Shadow
    @Final
    private int luminance;

    @Shadow
    public abstract Block getBlock();

    @Inject(method = "getLuminance", at = @At("HEAD"), cancellable = true)
    public void getLuminance(CallbackInfoReturnable<Integer> cir) {
        EventBlockBrightness eventBlockBrightness = new EventBlockBrightness(this.getBlock(), this.luminance).run();
        cir.setReturnValue(eventBlockBrightness.getBrightness());
    }

}
