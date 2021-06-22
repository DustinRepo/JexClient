package me.dustin.jex.load.mixin;

import me.dustin.jex.event.render.EventBufferQuadAlpha;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AbstractQuadRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = AbstractQuadRenderer.class, remap = false)
public abstract class MixinAbstractQuadRenderer {
    @SuppressWarnings("All")
    @ModifyArg(method = "bufferQuad*", /*Hack. This is a hack made by https://github.com/slcoolj to help un-bloat this class and actually make this work*/ at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumer;color(IIII)Lnet/minecraft/client/render/VertexConsumer;"), index = 3, remap = true)
    private static int colorAlpha(int alpha) {
        EventBufferQuadAlpha eventBufferQuadAlpha = new EventBufferQuadAlpha(alpha).run();
        return eventBufferQuadAlpha.getAlpha();
    }
}
