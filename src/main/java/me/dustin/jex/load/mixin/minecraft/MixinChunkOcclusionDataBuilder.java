package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.render.EventMarkChunkClosed;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkOcclusionDataBuilder.class)
public class MixinChunkOcclusionDataBuilder {

    @Inject(method = "markClosed", at = @At("HEAD"), cancellable = true)
    public void markClosed(BlockPos blockPos, CallbackInfo ci) {
        try {
            EventMarkChunkClosed eventMarkChunkClosed = new EventMarkChunkClosed().run();
            if (eventMarkChunkClosed.isCancelled())
                ci.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
