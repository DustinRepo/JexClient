package me.dustin.jex.load.mixin.minecraft;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import me.dustin.jex.event.render.EventBlockOutlineColor;
import me.dustin.jex.event.render.EventRenderRain;
import me.dustin.jex.event.render.EventWorldRender;
import me.dustin.jex.feature.mod.impl.render.esp.ESP;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.io.IOException;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {

    @Shadow @Nullable private PostChain entityEffect;
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Nullable private RenderTarget entityTarget;
    private final ResourceLocation my_outline = new ResourceLocation("jex", "shaders/entity_outline.json");
    private final ResourceLocation mojang_outline = new ResourceLocation("shaders/post/entity_outline.json");

    @Inject(method = "renderLevel", at = @At(value = "RETURN"))
    public void render1(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        new EventWorldRender(matrices, tickDelta).run();
    }

    @Inject(method = "initOutline", at = @At("HEAD"), cancellable = true)
    public void loadEntityOutlineShader1(CallbackInfo ci) {
        if (this.entityEffect != null) {
            this.entityEffect.close();
        }

        ResourceLocation identifier = getIDForOutline("shaders/post/entity_outline.json");

        try {
            this.entityEffect = new PostChain(this.minecraft.getTextureManager(), this.minecraft.getResourceManager(), this.minecraft.getMainRenderTarget(), identifier);
            this.entityEffect.resize(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
            this.entityTarget = this.entityEffect.getTempTarget("final");
        } catch (IOException | JsonSyntaxException var3) {
            this.entityEffect = null;
            this.entityTarget = null;
        }
        ShaderHelper.INSTANCE.load();
        ci.cancel();
    }

    public ResourceLocation getIDForOutline(String id) {
        try {
            if (ESP.INSTANCE.getState() && ESP.INSTANCE.mode.equalsIgnoreCase("Shader")) {
                return my_outline;
            }
        } catch (Exception e) {
            return mojang_outline;
        }
        return mojang_outline;
    }

    @Inject(method = "renderSnowAndRain", at = @At("HEAD"), cancellable = true)
    public void renderWeather(LightTexture manager, float f, double d, double e, double g, CallbackInfo ci) {
        EventRenderRain eventRenderRain = new EventRenderRain().run();
        if (eventRenderRain.isCancelled())
            ci.cancel();
    }

    @Inject(method = "renderShape", at = @At("HEAD"), cancellable = true)
    private static void drawShapeOutline1(PoseStack matrixStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j, CallbackInfo ci) {
        EventBlockOutlineColor eventBlockOutlineColor = new EventBlockOutlineColor().run();
        if (eventBlockOutlineColor.isCancelled()) {
            Color color = Render2DHelper.INSTANCE.hex2Rgb(Integer.toHexString(eventBlockOutlineColor.getColor()));
            PoseStack.Pose entry = matrixStack.last();
            voxelShape.forAllEdges((k, l, m, n, o, p) -> {
                float q = (float)(n - k);
                float r = (float)(o - l);
                float s = (float)(p - m);
                float t = Mth.sqrt(q * q + r * r + s * s);
                q /= t;
                r /= t;
                s /= t;
                vertexConsumer.vertex(entry.pose(), (float)(k + d), (float)(l + e), (float)(m + f)).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).normal(entry.normal(), q, r, s).endVertex();
                vertexConsumer.vertex(entry.pose(), (float)(n + d), (float)(o + e), (float)(p + f)).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).normal(entry.normal(), q, r, s).endVertex();
            });
            ci.cancel();
        }
    }

    @Inject(method = "resize", at = @At("HEAD"))
    public void onResized1(int width, int height, CallbackInfo ci) {
        ShaderHelper.INSTANCE.onResized(width, height);
    }
}
