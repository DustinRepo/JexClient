package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.render.EventRenderBlock;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.TerrainBlockRenderInfo;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.TerrainRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TerrainRenderContext.class)
public class MixinTerrainRenderContext {

    @Shadow
    @Final
    private TerrainBlockRenderInfo blockInfo;

    @Inject(at = {@At("HEAD")}, method = "tesselateBlock", cancellable = true, remap = false)
    private void tesselateBlock(BlockState blockState, BlockPos blockPos, BakedModel model, MatrixStack matrixStack, CallbackInfoReturnable<Boolean> cir) {
        EventRenderBlock eventRenderBlock = new EventRenderBlock(blockState.getBlock()).run();
        if (eventRenderBlock.isCancelled())
            cir.setReturnValue(false);
    }

}
