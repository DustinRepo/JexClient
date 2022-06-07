package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.render.EventGetRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderLayers.class)
public class MixinRenderLayers {
    @Inject(method = "getBlockLayer", at = @At("HEAD"), cancellable = true)
    private static void getBlockLayer(BlockState state, CallbackInfoReturnable<RenderLayer> cir) {
        EventGetRenderType eventGetRenderType = new EventGetRenderType(state, cir.getReturnValue()).run();
        if (eventGetRenderType.isCancelled()) {
            cir.setReturnValue(eventGetRenderType.getRenderType());
            cir.cancel();
        }
    }
}
