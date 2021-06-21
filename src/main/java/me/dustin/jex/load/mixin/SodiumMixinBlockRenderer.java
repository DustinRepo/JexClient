package me.dustin.jex.load.mixin;

import me.dustin.jex.event.render.EventBufferQuadAlpha;
import me.dustin.jex.event.render.EventRenderBlock;
import me.dustin.jex.helper.math.ColorHelper;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuffers;
import me.jellysquid.mods.sodium.client.render.pipeline.BlockRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Pseudo
@Mixin(BlockRenderer.class)
public class SodiumMixinBlockRenderer {

    @Inject(method = "renderModel", at = @At("HEAD"), cancellable = true, remap = false)
    public void renderModel1(BlockRenderView world, BlockState state, BlockPos pos, BakedModel model, ChunkModelBuffers builder, boolean cull, long seed, CallbackInfoReturnable<Boolean> cir) {
        EventRenderBlock eventRenderBlock = new EventRenderBlock(state.getBlock()).run();
        if (eventRenderBlock.isCancelled())
            cir.setReturnValue(false);
    }

    @ModifyArg(method = "renderQuad", at = @At(value = "INVOKE", target = "me/jellysquid/mods/sodium/client/render/chunk/format/ModelVertexSink.writeVertex(FFFIFFI)V"), index = 3, remap=false)
    public int getBlockColor(int color) {
        Color col = ColorHelper.INSTANCE.getColor(color);
        int a = col.getAlpha();
        EventBufferQuadAlpha eventBufferQuadAlpha = new EventBufferQuadAlpha(a).run();
        col = new Color(col.getRed(), col.getGreen(), col.getBlue(), eventBufferQuadAlpha.getAlpha());
        return col.getRGB();
    }

}
