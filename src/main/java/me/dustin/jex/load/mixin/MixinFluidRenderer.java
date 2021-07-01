package me.dustin.jex.load.mixin;

import me.dustin.jex.event.render.EventRenderFluid;
import me.dustin.jex.event.render.EventShouldDrawSide;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidRenderer.class)
public class MixinFluidRenderer {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void tesselate(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, FluidState state, CallbackInfoReturnable<Boolean> cir) {
        EventRenderFluid eventRenderFluid = new EventRenderFluid(state.getBlockState().getBlock()).run();
        if (eventRenderFluid.isCancelled())
            cir.setReturnValue(false);
    }

    @Inject(method = "isSideCovered(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;F)Z", at = @At("HEAD"), cancellable = true)
    private static void isSideCovered1(BlockView world, BlockPos pos, Direction direction, float maxDeviation, CallbackInfoReturnable<Boolean> cir) {
        EventShouldDrawSide eventShouldDrawSide = new EventShouldDrawSide(WorldHelper.INSTANCE.getBlock(pos), direction, pos).run();
        if (eventShouldDrawSide.isCancelled())
            cir.setReturnValue(!eventShouldDrawSide.isShouldDrawSide());
    }

    //Keep this? doesn't work on lava
    /*@ModifyArg(method = "vertex", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumer;color(FFFF)Lnet/minecraft/client/render/VertexConsumer;"), index = 3)
    public float getAlpha(float alpha) {
        int a = (int)(alpha * 255);
        EventBufferQuadAlpha eventBufferQuadAlpha = new EventBufferQuadAlpha(a).run();
        return eventBufferQuadAlpha.getAlpha() / 255.f;
    }*/
}
