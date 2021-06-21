package me.dustin.jex.load.mixin;

import me.dustin.jex.event.render.EventBufferQuadAlpha;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AbstractQuadRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(value = AbstractQuadRenderer.class, remap = false)
public abstract class MixinAbstractQuadRenderer {

    @ModifyArg(method = "bufferQuad(Lnet/minecraft/client/render/VertexConsumer;Lnet/fabricmc/fabric/impl/client/indigo/renderer/mesh/MutableQuadViewImpl;Lnet/minecraft/util/math/Matrix4f;ILnet/minecraft/util/math/Matrix3f;Lnet/minecraft/util/math/Vec3f;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumer;color(IIII)Lnet/minecraft/client/render/VertexConsumer;"), index = 3)
    private static int colorAlpha(int alpha) {
        EventBufferQuadAlpha eventBufferQuadAlpha = new EventBufferQuadAlpha(alpha).run();
        return eventBufferQuadAlpha.getAlpha();
    }

}
