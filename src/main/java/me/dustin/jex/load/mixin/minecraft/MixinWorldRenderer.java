package me.dustin.jex.load.mixin.minecraft;

import com.google.gson.JsonSyntaxException;
import me.dustin.jex.event.render.EventBlockOutlineColor;
import me.dustin.jex.event.render.EventRenderRain;
import me.dustin.jex.event.render.EventWorldRender;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.esp.ESP;
import me.dustin.jex.feature.mod.impl.render.storageesp.StorageESP;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.shader.ShaderHelper;
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
public abstract class MixinWorldRenderer {

    @Shadow private @Nullable ShaderEffect entityOutlineShader;
    @Shadow @Final private MinecraftClient client;
    @Shadow private @Nullable Framebuffer entityOutlinesFramebuffer;

    private final Identifier my_outline = new Identifier("jex", "shaders/entity_outline.json");
    private final Identifier mojang_outline = new Identifier("shaders/post/entity_outline.json");

    @Inject(method = "render", at = @At(value = "RETURN"))
    public void render1(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        new EventWorldRender(matrices, tickDelta).run();
    }

    @Inject(method = "loadEntityOutlineShader", at = @At("HEAD"), cancellable = true)
    public void loadEntityOutlineShader1(CallbackInfo ci) {
        if (this.entityOutlineShader != null) {
            this.entityOutlineShader.close();
        }

        Identifier identifier = getIDForOutline();

        try {
            this.entityOutlineShader = new ShaderEffect(this.client.getTextureManager(), this.client.getResourceManager(), this.client.getFramebuffer(), identifier);
            this.entityOutlineShader.setupDimensions(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight());
            this.entityOutlinesFramebuffer = this.entityOutlineShader.getSecondaryTarget("final");
        } catch (IOException | JsonSyntaxException var3) {
            this.entityOutlineShader = null;
            this.entityOutlinesFramebuffer = null;
        }
        ShaderHelper.INSTANCE.load();
        ci.cancel();
    }

    public Identifier getIDForOutline() {
        try {
            if ((ESP.INSTANCE.getState() && ESP.INSTANCE.modeProperty.value() == ESP.Mode.SHADER) || Feature.getState(StorageESP.class) && Feature.get(StorageESP.class).modeProperty.value() == StorageESP.Mode.SHADER) {
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

    @Inject(method = "onResized", at = @At("HEAD"))
    public void onResized1(int width, int height, CallbackInfo ci) {
        ShaderHelper.INSTANCE.onResized(width, height);
    }
}
