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

    @Inject(method = "isSideCovered", at = @At("HEAD"), cancellable = true)
    private static void isSideCovered1(BlockView world, BlockPos pos, Direction direction, float maxDeviation, CallbackInfoReturnable<Boolean> cir) {
        EventShouldDrawSide eventShouldDrawSide = new EventShouldDrawSide(WorldHelper.INSTANCE.getBlock(pos), pos).run();
        if (eventShouldDrawSide.isCancelled())
            cir.setReturnValue(!eventShouldDrawSide.isShouldDrawSide());
    }

}
