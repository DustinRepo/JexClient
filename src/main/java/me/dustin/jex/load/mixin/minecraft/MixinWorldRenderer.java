package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.render.*;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.load.impl.IWorldRenderer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer implements IWorldRenderer {

    @Shadow private @Nullable Framebuffer entityOutlinesFramebuffer;

    @Inject(method = "render", at = @At(value = "HEAD"))
    public void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        new EventWorldRender(matrices, tickDelta, matrix4f, EventWorldRender.Mode.PRE).run();
    }

    @Inject(method = "render", at = @At(value = "RETURN"))
    public void render1(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        new EventWorldRender(matrices, tickDelta, matrix4f, EventWorldRender.Mode.POST).run();
    }

    @Inject(method = "renderEntity", at = @At("HEAD"), cancellable = true)
    public void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        EventWorldRenderEntity eventWorldRenderEntity = new EventWorldRenderEntity(entity, matrices, vertexConsumers, tickDelta).run();
        if (eventWorldRenderEntity.isCancelled())
            ci.cancel();
    }

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    public void renderWeather(LightmapTextureManager manager, float f, double d, double e, double g, CallbackInfo ci) {
        EventRenderRain eventRenderRain = new EventRenderRain().run();
        if (eventRenderRain.isCancelled())
            ci.cancel();
    }

    @Inject(method = "drawCuboidShapeOutline", at = @At("HEAD"), cancellable = true)
    private static void drawShapeOutline1(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape shape, double offsetX, double offsetY, double offsetZ, float red, float green, float blue, float alpha, CallbackInfo ci) {
        EventBlockOutlineColor eventBlockOutlineColor = new EventBlockOutlineColor().run();
        if (eventBlockOutlineColor.isCancelled()) {
            Color color = Render2DHelper.INSTANCE.hex2Rgb(Integer.toHexString(eventBlockOutlineColor.getColor()));
            net.minecraft.client.util.math.MatrixStack.Entry entry = matrices.peek();
            shape.forEachEdge((minX, minY, minZ, maxX, maxY, maxZ) -> {
                float k = (float)(maxX - minX);
                float l = (float)(maxY - minY);
                float m = (float)(maxZ - minZ);
                float n = MathHelper.sqrt(k * k + l * l + m * m);
                vertexConsumer.vertex(entry.getPositionMatrix(), (float)(minX + offsetX), (float)(minY + offsetY), (float)(minZ + offsetZ)).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).normal(entry.getNormalMatrix(), k /= n, l /= n, m /= n).next();
                vertexConsumer.vertex(entry.getPositionMatrix(), (float)(maxX + offsetX), (float)(maxY + offsetY), (float)(maxZ + offsetZ)).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).normal(entry.getNormalMatrix(), k, l, m).next();
            });
            ci.cancel();
        }
    }

    @Override
    public @Nullable Framebuffer getEntityOutlinesFramebuffer() {
        return entityOutlinesFramebuffer;
    }

    @Override
    public void setEntityOutlinesFramebuffer(Framebuffer entityOutlinesFramebuffer) {
        this.entityOutlinesFramebuffer = entityOutlinesFramebuffer;
    }
}
