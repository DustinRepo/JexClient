package me.dustin.jex.load.mixin.sodium;

import me.dustin.jex.event.render.EventRenderBlock;
import me.dustin.jex.event.render.EventSodiumQuadAlpha;
import me.dustin.jex.helper.math.ColorHelper;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
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
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Pseudo
@Mixin(value = BlockRenderer.class, remap = false)
public class MixinBlockRenderer {
    @Inject(method = "renderModel", at = @At("HEAD"), cancellable = true)
    public void renderModel1(BlockRenderView world, BlockState state, BlockPos pos, BlockPos origin, BakedModel model, ChunkModelBuilder buffers, boolean cull, long seed, CallbackInfoReturnable<Boolean> cir) {
        EventRenderBlock eventRenderBlock = new EventRenderBlock(state.getBlock()).run();
        if (eventRenderBlock.isCancelled())
            cir.setReturnValue(false);
    }

    @Inject(method = "renderQuad", at = @At(value = "HEAD"), cancellable = true)
    public int getBlockColor(int color) {
        Color col = ColorHelper.INSTANCE.getColor(color);
        int a = col.getAlpha();
        EventSodiumQuadAlpha eventBufferQuadAlpha = new EventSodiumQuadAlpha(a).run();
        col = new Color(col.getRed(), col.getGreen(), col.getBlue(), eventBufferQuadAlpha.getAlpha());
        return col.getRGB();
    }
}
