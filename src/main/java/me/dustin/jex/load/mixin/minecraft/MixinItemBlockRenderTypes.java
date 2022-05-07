package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.render.EventGetRenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemBlockRenderTypes.class)
public class MixinItemBlockRenderTypes {
    @Inject(method = "getChunkRenderType", at = @At("HEAD"), cancellable = true)
    private static void getBlockLayer(BlockState state, CallbackInfoReturnable<RenderType> cir) {
        EventGetRenderType eventGetRenderType = new EventGetRenderType(state, cir.getReturnValue()).run();
        if (eventGetRenderType.isCancelled()) {
            cir.setReturnValue(eventGetRenderType.getRenderType());
            cir.cancel();
        }
    }
}
