package me.dustin.jex.load.mixin.sodium;

import me.dustin.jex.event.render.EventRenderFluid;
import me.dustin.jex.event.render.EventShouldRenderFace;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.pipeline.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(FluidRenderer.class)
public class MixinFluidRenderer {

	@Inject(method = "isSideExposed", at = @At("HEAD"), cancellable = true, remap = false)
	public void isSideExposed1(BlockAndTintGetter world, int x, int y, int z, Direction dir, float height, CallbackInfoReturnable<Boolean> cir) {
		BlockPos pos = new BlockPos(x, y, z);
		EventShouldRenderFace eventShouldRenderFace = new EventShouldRenderFace(world.getBlockState(pos).getBlock(), dir, pos).run();
		if (eventShouldRenderFace.isCancelled()) {
			cir.setReturnValue(eventShouldRenderFace.isShouldDrawSide());
		}
	}

	@Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
	public void render1(BlockAndTintGetter world, FluidState fluidState, BlockPos pos, BlockPos offset, ChunkModelBuilder buffers, CallbackInfoReturnable<Boolean> cir) {
		EventRenderFluid eventRenderFluid = new EventRenderFluid(world.getBlockState(pos).getBlock()).run();
		if (eventRenderFluid.isCancelled())
			cir.setReturnValue(false);
	}

}
