package me.dustin.jex.load.mixin.minecraft;

import com.mojang.blaze3d.vertex.VertexConsumer;
import me.dustin.jex.event.render.EventRenderFluid;
import me.dustin.jex.event.render.EventShouldRenderFace;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LiquidBlockRenderer.class)
public class MixinLiquidBlockRenderer {

    @Inject(method = "tesselate", at = @At("HEAD"), cancellable = true)
    public void tesselate(BlockAndTintGetter world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
        EventRenderFluid eventRenderFluid = new EventRenderFluid(fluidState.createLegacyBlock().getBlock()).run();
        if (eventRenderFluid.isCancelled())
            ci.cancel();
    }

    @Inject(method = "isFaceOccludedByNeighbor", at = @At("HEAD"), cancellable = true)
    private static void isSideCovered1(BlockGetter blockView, BlockPos blockPos, Direction direction, float maxDeviation, BlockState blockState, CallbackInfoReturnable<Boolean> cir) {
        EventShouldRenderFace eventShouldRenderFace = new EventShouldRenderFace(WorldHelper.INSTANCE.getBlock(blockPos), direction, blockPos).run();
        if (eventShouldRenderFace.isCancelled())
            cir.setReturnValue(!eventShouldRenderFace.isShouldDrawSide());
    }
}
