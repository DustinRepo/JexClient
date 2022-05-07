package me.dustin.jex.load.mixin.indigo;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.event.render.EventRenderBlock;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.TerrainRenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(TerrainRenderContext.class)
public class MixinTerrainRenderContext {

	@Inject(at = { @At("HEAD") }, method = "tessellateBlock", cancellable = true, remap = false)
	private void tesselateBlock(BlockState blockState, BlockPos blockPos, BakedModel model, PoseStack matrixStack, CallbackInfoReturnable<Boolean> cir) {
		EventRenderBlock eventRenderBlock = new EventRenderBlock(blockState.getBlock()).run();
		if (eventRenderBlock.isCancelled())
			cir.setReturnValue(false);
	}

}
