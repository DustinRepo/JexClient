package me.dustin.jex.load.mixin;

import com.google.gson.JsonSyntaxException;
import me.dustin.jex.event.render.EventBlockOutlineColor;
import me.dustin.jex.event.render.EventRenderRain;
import me.dustin.jex.event.render.EventWorldRender;
import me.dustin.jex.feature.impl.render.esp.ESP;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.load.impl.IWorldRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.io.IOException;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer implements IWorldRenderer {

    @Shadow
    @Final
    private BufferBuilderStorage bufferBuilders;

    @Shadow private @Nullable ShaderEffect entityOutlineShader;
    @Shadow @Final private MinecraftClient client;
    @Shadow private @Nullable Framebuffer entityOutlinesFramebuffer;
    private Identifier my_outline = new Identifier("jex", "shaders/entity_outline.json");
    private Identifier mojang_outline = new Identifier("shaders/post/entity_outline.json");

    @Inject(method = "render", at = @At(value = "RETURN"))
    public void render1(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        new EventWorldRender(matrices, tickDelta).run();
    }

    @Inject(method = "loadEntityOutlineShader", at = @At("HEAD"), cancellable = true)
    public void loadEntityOutlineShader1(CallbackInfo ci) {
        if (this.entityOutlineShader != null) {
            this.entityOutlineShader.close();
        }

        Identifier identifier = getIDForOutline("shaders/post/entity_outline.json");

        try {
            this.entityOutlineShader = new ShaderEffect(this.client.getTextureManager(), this.client.getResourceManager(), this.client.getFramebuffer(), identifier);
            this.entityOutlineShader.setupDimensions(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight());
            this.entityOutlinesFramebuffer = this.entityOutlineShader.getSecondaryTarget("final");
        } catch (IOException | JsonSyntaxException var3) {
            this.entityOutlineShader = null;
            this.entityOutlinesFramebuffer = null;
        }
        ci.cancel();
    }

    public Identifier getIDForOutline(String id) {
        try {
            if (ESP.INSTANCE.getState() && ESP.INSTANCE.mode.equalsIgnoreCase("Shader")) {
                return my_outline;
            }
        } catch (Exception e) {
            return mojang_outline;
        }
        return mojang_outline;
    }

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    public void renderWeather(LightmapTextureManager manager, float f, double d, double e, double g, CallbackInfo ci) {
        EventRenderRain eventRenderRain = new EventRenderRain().run();
        if (eventRenderRain.isCancelled())
            ci.cancel();
    }

    @Inject(method = "drawShapeOutline", at = @At("HEAD"), cancellable = true)
    private static void drawShapeOutline1(MatrixStack matrixStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j, CallbackInfo ci) {
        EventBlockOutlineColor eventBlockOutlineColor = new EventBlockOutlineColor().run();
        if (eventBlockOutlineColor.isCancelled()) {
            Color color = Render2DHelper.INSTANCE.hex2Rgb(Integer.toHexString(eventBlockOutlineColor.getColor()));
            net.minecraft.client.util.math.MatrixStack.Entry entry = matrixStack.peek();
            voxelShape.forEachEdge((k, l, m, n, o, p) -> {
                float q = (float)(n - k);
                float r = (float)(o - l);
                float s = (float)(p - m);
                float t = MathHelper.sqrt(q * q + r * r + s * s);
                q /= t;
                r /= t;
                s /= t;
                vertexConsumer.vertex(entry.getModel(), (float)(k + d), (float)(l + e), (float)(m + f)).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).normal(entry.getNormal(), q, r, s).next();
                vertexConsumer.vertex(entry.getModel(), (float)(n + d), (float)(o + e), (float)(p + f)).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).normal(entry.getNormal(), q, r, s).next();
            });
            ci.cancel();
        }
    }

    @Override
    public BufferBuilderStorage getBufferBuilders() {
        return this.bufferBuilders;
    }
}
