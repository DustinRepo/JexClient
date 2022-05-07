package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.render.EventMarkChunkClosed;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VisGraph.class)
public class MixinVisGraph {

    @Inject(method = "setOpaque", at = @At("HEAD"), cancellable = true)
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
