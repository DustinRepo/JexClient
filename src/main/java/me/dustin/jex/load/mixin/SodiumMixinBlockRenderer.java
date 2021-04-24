package me.dustin.jex.load.mixin;

import me.dustin.jex.event.render.EventRenderBlock;
import me.jellysquid.mods.sodium.client.model.quad.sink.ModelQuadSinkDelegate;
import me.jellysquid.mods.sodium.client.render.pipeline.BlockRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(BlockRenderer.class)
public class SodiumMixinBlockRenderer {

    @Inject(method = "renderModel", at = @At("HEAD"), cancellable = true, remap = false)
    public void renderModel1(BlockRenderView world, BlockState state, BlockPos pos, BakedModel model, ModelQuadSinkDelegate builder, boolean cull, long seed, CallbackInfoReturnable<Boolean> cir) {
        EventRenderBlock eventRenderBlock = new EventRenderBlock(state.getBlock()).run();
        if (eventRenderBlock.isCancelled())
            cir.setReturnValue(false);
    }

}
