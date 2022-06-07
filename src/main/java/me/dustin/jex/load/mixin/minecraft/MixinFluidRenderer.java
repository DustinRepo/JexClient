package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.render.EventRenderFluid;
import me.dustin.jex.event.render.EventShouldDrawSide;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.BlockState;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidRenderer.class)
public class MixinFluidRenderer {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void tesselate(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
        EventRenderFluid eventRenderFluid = new EventRenderFluid(fluidState.getBlockState().getBlock()).run();
        if (eventRenderFluid.isCancelled())
            ci.cancel();
    }

    @Inject(method = "isSideCovered(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;FLnet/minecraft/block/BlockState;)Z", at = @At("HEAD"), cancellable = true)
    private static void isSideCovered1(BlockView blockView, BlockPos blockPos, Direction direction, float maxDeviation, BlockState blockState, CallbackInfoReturnable<Boolean> cir) {
        EventShouldDrawSide eventShouldDrawSide = new EventShouldDrawSide(WorldHelper.INSTANCE.getBlock(blockPos), direction, blockPos).run();
        if (eventShouldDrawSide.isCancelled())
            cir.setReturnValue(!eventShouldDrawSide.isShouldDrawSide());
    }
}
