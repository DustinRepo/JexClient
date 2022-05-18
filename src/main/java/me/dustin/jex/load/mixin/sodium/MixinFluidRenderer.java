package me.dustin.jex.load.mixin.sodium;

import me.dustin.jex.event.render.EventRenderFluid;
import me.dustin.jex.event.render.EventShouldDrawSide;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.pipeline.FluidRenderer;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(FluidRenderer.class)
public class MixinFluidRenderer {

	@Inject(method = "isSideExposed", at = @At("HEAD"), cancellable = true, remap = false)
	public void isSideExposed1(BlockRenderView world, int x, int y, int z, Direction dir, float height, CallbackInfoReturnable<Boolean> cir) {
		BlockPos pos = new BlockPos(x, y, z);
		EventShouldDrawSide eventShouldDrawSide = new EventShouldDrawSide(world.getBlockState(pos).getBlock(), dir, pos).run();
		if (eventShouldDrawSide.isCancelled()) {
			cir.setReturnValue(eventShouldDrawSide.isShouldDrawSide());
		}
	}

	@Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
	public void render1(BlockRenderView world, FluidState fluidState, BlockPos pos, BlockPos offset, ChunkModelBuilder buffers, CallbackInfoReturnable<Boolean> cir) {
		EventRenderFluid eventRenderFluid = new EventRenderFluid(world.getBlockState(pos).getBlock()).run();
		if (eventRenderFluid.isCancelled())
			cir.setReturnValue(false);
	}

}
