package me.dustin.jex.helper.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.helper.addon.AddonHelper;
import me.dustin.jex.helper.addon.cape.CapeHelper;
import me.dustin.jex.helper.addon.ears.EarsHelper;
import me.dustin.jex.helper.addon.hat.HatHelper;
import me.dustin.jex.helper.addon.pegleg.PeglegHelper;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.math.Matrix4x4;
import me.dustin.jex.helper.math.vector.Vector3D;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import me.dustin.jex.helper.render.shader.ShaderProgram;
import me.dustin.jex.helper.render.shader.post.impl.PostProcessBlur;
import me.dustin.jex.load.impl.IItemRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vector4f;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public enum Render2DHelper {
    INSTANCE;
    private final static Identifier MAP_BACKGROUND = new Identifier("textures/map/map_background_checkerboard.png");
    private final PostProcessBlur postProcessBlur = new PostProcessBlur();
    private EntityRendererFactory.Context context;

    public void setup2DRender(boolean disableDepth) {
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        if (disableDepth)
            RenderSystem.disableDepthTest();
    }

    public void end2DRender() {
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
    }

    public double getScaleFactor() {
        return Wrapper.INSTANCE.getWindow().getScaleFactor();
    }

    public int getScaledWidth() {
        return Wrapper.INSTANCE.getWindow().getScaledWidth();
    }

    public int getScaledHeight() {
        return Wrapper.INSTANCE.getWindow().getScaledHeight();
    }

    public void drawTexture(MatrixStack matrices, float x, float y, float u, float v, float width, float height, int textureWidth, int textureHeight) {
        drawTexture(matrices, x, y, width, height, u, v, width, height, textureWidth, textureHeight);
    }

    private void drawTexture(MatrixStack matrices, float x, float y, float width, float height, float u, float v, float regionWidth, float regionHeight, int textureWidth, int textureHeight) {
        drawTexture(matrices, x, x + width, y, y + height, 0, regionWidth, regionHeight, u, v, textureWidth, textureHeight);
    }

    private void renderTexture(MatrixStack matrices, float x, float y, float z, float width, float height, float u, float v, float regionWidth, float regionHeight, int textureWidth, int textureHeight) {
        drawTexture(matrices, x, x + width, y, y + height, z, regionWidth, regionHeight, u, v, textureWidth, textureHeight);
    }

    private void drawTexture(MatrixStack matrices, float x0, float y0, float x1, float y1, float z, float regionWidth, float regionHeight, float u, float v, int textureWidth, int textureHeight) {
        drawTexturedQuad(matrices.peek().getPositionMatrix(), x0, y0, x1, y1, z, (u + 0.0F) / (float)textureWidth, (u + (float)regionWidth) / (float)textureWidth, (v + 0.0F) / (float)textureHeight, (v + (float)regionHeight) / (float)textureHeight);
    }

    public void drawTexturedQuad(Matrix4f matrices, float x0, float x1, float y0, float y1, float z, float u0, float u1, float v0, float v1) {
        BufferBuilder bufferBuilder = BufferHelper.INSTANCE.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrices, x0, y1, z).texture(u0, v1).next();
        bufferBuilder.vertex(matrices, x1, y1, z).texture(u1, v1).next();
        bufferBuilder.vertex(matrices, x1, y0, z).texture(u1, v0).next();
        bufferBuilder.vertex(matrices, x0, y0, z).texture(u0, v0).next();
        BufferHelper.INSTANCE.drawWithShader(bufferBuilder, GameRenderer::getPositionTexShader);
    }

    public void drawTexturedQuadNoDraw(Matrix4f matrices, float x0, float x1, float y0, float y1, float z, float u0, float u1, float v0, float v1) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.vertex(matrices, x0, y1, z).texture(u0, v1).next();
        bufferBuilder.vertex(matrices, x1, y1, z).texture(u1, v1).next();
        bufferBuilder.vertex(matrices, x1, y0, z).texture(u1, v0).next();
        bufferBuilder.vertex(matrices, x0, y0, z).texture(u0, v0).next();
    }

    public void blur(float radius, Framebuffer in) {
        Matrix4f proj = RenderSystem.getProjectionMatrix();
        ShaderProgram shader = postProcessBlur.getShader();
        postProcessBlur.setFirst(in);
        shader.setUpdateUniforms(() -> {
            shader.getUniform("BlurDir").setVec(new Vec2f(0, 1));
            shader.getUniform("Radius").setFloat(radius);
        });
        postProcessBlur.render();
        postProcessBlur.getSecond().draw(Wrapper.INSTANCE.getWindow().getFramebufferWidth(), Wrapper.INSTANCE.getWindow().getFramebufferHeight(), false);
        RenderSystem.setProjectionMatrix(proj);
    }

    public void fill(MatrixStack poseStack, float x1, float y1, float x2, float y2, int color) {
        Matrix4f matrix = poseStack.peek().getPositionMatrix();
        float j;
        if (x1 < x2) {
            j = x1;
            x1 = x2;
            x2 = j;
        }

        if (y1 < y2) {
            j = y1;
            y1 = y2;
            y2 = j;
        }

        float f = (float)(color >> 24 & 255) / 255.0F;
        float g = (float)(color >> 16 & 255) / 255.0F;
        float h = (float)(color >> 8 & 255) / 255.0F;
        float k = (float)(color & 255) / 255.0F;
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        BufferBuilder bufferBuilder = BufferHelper.INSTANCE.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x1, y2, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x2, y2, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x2, y1, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x1, y1, 0.0F).color(g, h, k, f).next();
        BufferHelper.INSTANCE.drawWithShader(bufferBuilder, ShaderHelper.INSTANCE.getPosColorShader());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public void fillNoDraw(MatrixStack poseStack, float x1, float y1, float x2, float y2, int color) {
        Matrix4f matrix = poseStack.peek().getPositionMatrix();
        float j;
        if (x1 < x2) {
            j = x1;
            x1 = x2;
            x2 = j;
        }

        if (y1 < y2) {
            j = y1;
            y1 = y2;
            y2 = j;
        }

        float f = (float)(color >> 24 & 255) / 255.0F;
        float g = (float)(color >> 16 & 255) / 255.0F;
        float h = (float)(color >> 8 & 255) / 255.0F;
        float k = (float)(color & 255) / 255.0F;
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.vertex(matrix, x1, y2, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x2, y2, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x2, y1, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x1, y1, 0.0F).color(g, h, k, f).next();
    }

    public void drawFace(MatrixStack poseStack, float x, float y, int renderScale, Identifier id) {
        try {
            bindTexture(id);
            RenderSystem.enableBlend();
            drawTexture(poseStack, x, y, 8 * renderScale, 8 * renderScale, 8 * renderScale, 8 * renderScale, 8 * renderScale, 8 * renderScale, 64 * renderScale, 64 * renderScale);
            drawTexture(poseStack, x, y, 8 * renderScale, 8 * renderScale, 40 * renderScale, 8 * renderScale, 8 * renderScale, 8 * renderScale, 64 * renderScale, 64 * renderScale);
        }catch (Exception e){}
    }



    public void renderPlayerIn3D(Identifier skin, String uuid, float x, float y, float yaw, float scale) {
        AddonHelper.AddonResponse addonResponse = AddonHelper.INSTANCE.getResponse(uuid);
        if (context == null) {
            context = new EntityRendererFactory.Context(Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher(), Wrapper.INSTANCE.getMinecraft().getItemRenderer(), Wrapper.INSTANCE.getMinecraft().getBlockRenderManager(), Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().getHeldItemRenderer(), Wrapper.INSTANCE.getMinecraft().getResourceManager(), Wrapper.INSTANCE.getMinecraft().getEntityModelLoader(), Wrapper.INSTANCE.getTextRenderer());
        }
        PlayerEntityModel<PlayerEntity> playerEntityPlayerEntityModel = new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER), false);
        playerEntityPlayerEntityModel.getHead().scale(new Vec3f(-0.3f, -0.3f, -0.3f));//??? no fucking clue why it's needed

        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.translate(x, y - scale / 2.f, 1050.0);
        matrixStack.scale(1.0f, 1.0f, -1.0f);
        RenderSystem.applyModelViewMatrix();
        MatrixStack matrixStack2 = new MatrixStack();
        matrixStack2.translate(0.0, 0.0, 1000.0);
        matrixStack2.scale(scale, scale, scale);
        Quaternion quaternion = Vec3f.POSITIVE_Z.getDegreesQuaternion(0);
        Quaternion quaternion2 = Vec3f.POSITIVE_Y.getDegreesQuaternion(yaw);
        quaternion.hamiltonProduct(quaternion2);
        matrixStack2.multiply(quaternion);
        DiffuseLighting.method_34742();
        int overlayTexture = OverlayTexture.DEFAULT_UV;
        VertexConsumerProvider.Immediate vertexConsumerProvider = Wrapper.INSTANCE.getMinecraft().getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(skin));
        playerEntityPlayerEntityModel.render(matrixStack2, vertexConsumer, 0xF000F0, overlayTexture, 1, 1, 1, 1);
        if (EarsHelper.INSTANCE.hasEars(uuid)) {
            vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumerProvider, RenderLayer.getArmorCutoutNoCull(EarsHelper.INSTANCE.getEars(uuid)), false, addonResponse != null && addonResponse.enchantedears());
            matrixStack2.translate(0, 0.8, 0);
            playerEntityPlayerEntityModel.renderEars(matrixStack2, vertexConsumer, 0xF000F0, overlayTexture);
            matrixStack2.translate(0, -0.8, 0);
        }
        if (CapeHelper.INSTANCE.hasCape(uuid)) {
            vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumerProvider, RenderLayer.getArmorCutoutNoCull(CapeHelper.INSTANCE.getCape(uuid)), false, addonResponse != null && addonResponse.enchantedcape());
            matrixStack2.translate(0, 0.75, 0.05);
            matrixStack2.scale(0.5f, 0.5f, 0.5f);
            matrixStack2.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0f));
            matrixStack2.multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion(10));
            playerEntityPlayerEntityModel.renderCape(matrixStack2, vertexConsumer, 0xF000F0, overlayTexture);
            matrixStack2.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(10));
            matrixStack2.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(180.0f));
            matrixStack2.scale(2, 2, 2);
            matrixStack2.translate(0, -0.75, 0.1);
        }
        if (PeglegHelper.INSTANCE.hasPegleg(uuid)) {
            vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumerProvider, RenderLayer.getArmorCutoutNoCull(PeglegHelper.INSTANCE.getPeglegTexture(uuid)), false, addonResponse != null && addonResponse.enchantedleg());
            matrixStack2.translate(0, 1.125, -0.15);
            matrixStack2.scale(0.5f, 0.5f, 0.5f);
            PeglegHelper.INSTANCE.renderPegleg(matrixStack2, vertexConsumer, 0xF000F0, overlayTexture, PeglegHelper.INSTANCE.getType(uuid));
            matrixStack2.translate(0, -1.125, 0.15);
            matrixStack2.scale(2, 2, 2);
        }
        if (HatHelper.INSTANCE.hasHat(uuid)) {
            vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntitySolid(HatHelper.INSTANCE.getHatTexture(uuid)));
            matrixStack2.translate(0, -0.075, -0.075);
            matrixStack2.scale(0.5f, 0.5f, 0.5f);
            HatHelper.INSTANCE.renderHat(matrixStack2, vertexConsumer, 0xF000F0, overlayTexture, HatHelper.INSTANCE.getType(uuid));
            matrixStack2.translate(0, 0.075, 0.075);
            matrixStack2.scale(2, 2, 2);
        }
        vertexConsumerProvider.draw();
        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();
        DiffuseLighting.enableGuiDepthLighting();
    }

    public void draw3DHead(MatrixStack matrixStack, Identifier skin, float x, float y, float yaw, float pitch) {
        bindTexture(skin);
        RenderSystem.enableBlend();
        matrixStack.translate(x - 4, y - 4, 0);
        matrixStack.multiply(new Quaternion(new Vec3f(0, 1, 0), yaw, true));
        matrixStack.multiply(new Quaternion(new Vec3f(1, 0, 0), pitch, true));

        //face
        renderTexture(matrixStack, -8, -8, -8, 32, 32, 8, 8, 8, 8, 64, 64);
        renderTexture(matrixStack, -8, -8, -8, 32, 32, 40, 8, 8, 8, 64, 64);

        //top of head
        matrixStack.multiply(new Quaternion(new Vec3f(-1, 0, 0), 90, true));
        renderTexture(matrixStack, -8, -24, -8, 32, 32, 8, 0, 8, 8, 64, 64);
        renderTexture(matrixStack, -8, -24, -8, 32, 32, 40, 0, 8, 8, 64, 64);
        matrixStack.multiply(new Quaternion(new Vec3f(1, 0, 0), 90, true));

        //back of head
        matrixStack.multiply(new Quaternion(new Vec3f(0, 1, 0), 180, true));
        renderTexture(matrixStack, -24, -8, -24, 32, 32, 24, 8, 8, 8, 64, 64);
        renderTexture(matrixStack, -24, -8, -24, 32, 32, 48, 8, 8, 8, 64, 64);
        matrixStack.multiply(new Quaternion(new Vec3f(0, -1, 0), 180, true));

        //bottom of head
        matrixStack.multiply(new Quaternion(new Vec3f(1, 0, 0), 90, true));
        matrixStack.multiply(new Quaternion(new Vec3f(0, 0, 1), 180, true));
        renderTexture(matrixStack, -24, -24, -24, 32, 32, 16, 0, 8, 8, 64, 64);
        renderTexture(matrixStack, -24, -24, -24, 32, 32, 48, 0, 8, 8, 64, 64);
        matrixStack.multiply(new Quaternion(new Vec3f(0, 0, -1), 180, true));
        matrixStack.multiply(new Quaternion(new Vec3f(-1, 0, 0), 90, true));

        //right side head
        matrixStack.multiply(new Quaternion(new Vec3f(0, 1, 0), 90, true));
        renderTexture(matrixStack, -24, -8, -8, 32, 32, 0, 8, 8, 8, 64, 64);
        renderTexture(matrixStack, -24, -8, -8, 32, 32, 32, 8, 8, 8, 64, 64);
        matrixStack.multiply(new Quaternion(new Vec3f(0, -1, 0), 90, true));

        //left side head
        matrixStack.multiply(new Quaternion(new Vec3f(0, 1, 0), 270, true));
        renderTexture(matrixStack, -8, -8, -24, 32, 32, 16, 8, 8, 8, 64, 64);
        renderTexture(matrixStack, -8, -8, -24, 32, 32, 48, 8, 8, 8, 64, 64);
        matrixStack.multiply(new Quaternion(new Vec3f(0, -1, 0), 270, true));
    }

    public void draw3DCape(MatrixStack poseStack, float x, float y, Identifier identifier, float yaw, float pitch) {
        poseStack.push();
        poseStack.translate(x + 16, y + 30, 64);
        poseStack.multiply(new Quaternion(new Vec3f(0, 1, 0), yaw, true));
        poseStack.multiply(new Quaternion(new Vec3f(1, 0, 0), pitch, true));
        //
        bindTexture(identifier);
        //front of cape
        DrawableHelper.drawTexture(poseStack, -16, -30, 2.5f, 4, 32, 60, 198, 124);
        //back of cape
        poseStack.multiply(new Quaternion(new Vec3f(0.0F, 1.0F, 0.0F), 180, true));
        DrawableHelper.drawTexture(poseStack, -16, -30, 34.5f, 4, 32, 60, 198, 124);
        poseStack.multiply(new Quaternion(new Vec3f(0.0F, 1.0F, 0.0F), -180, true));
        //
        poseStack.pop();
    }

    public void fillAndBorder(MatrixStack poseStack, float left, float top, float right, float bottom, int bcolor, int icolor, float f) {
        fill(poseStack, left + f, top + f, right - f, bottom - f, icolor);
        fill(poseStack, left, top, left + f, bottom, bcolor);
        fill(poseStack, left + f, top, right, top + f, bcolor);
        fill(poseStack, left + f, bottom - f, right, bottom, bcolor);
        fill(poseStack, right - f, top + f, right, bottom - f, bcolor);
    }

    public void drawGradientRect(double x, double y, double x2, double y2, int col1, int col2) {
        float f = (float) (col1 >> 24 & 0xFF) / 255F;
        float f1 = (float) (col1 >> 16 & 0xFF) / 255F;
        float f2 = (float) (col1 >> 8 & 0xFF) / 255F;
        float f3 = (float) (col1 & 0xFF) / 255F;

        float f4 = (float) (col2 >> 24 & 0xFF) / 255F;
        float f5 = (float) (col2 >> 16 & 0xFF) / 255F;
        float f6 = (float) (col2 >> 8 & 0xFF) / 255F;
        float f7 = (float) (col2 & 0xFF) / 255F;

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        BufferBuilder bufferBuilder = BufferHelper.INSTANCE.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(x2, y, 0).color(f1, f2, f3, f).next();
        bufferBuilder.vertex(x, y, 0).color(f1, f2, f3, f).next();

        bufferBuilder.vertex(x, y2, 0).color(f5, f6, f7, f4).next();
        bufferBuilder.vertex(x2, y2, 0).color(f5, f6, f7, f4).next();

        BufferHelper.INSTANCE.drawWithShader(bufferBuilder, ShaderHelper.INSTANCE.getPosColorShader());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public void gradientFill(MatrixStack poseStack, float x, float y, float x2, float y2, int col1, int col2) {
        Matrix4f matrix4f = poseStack.peek().getPositionMatrix();
        float f = (float) (col1 >> 24 & 0xFF) / 255F;
        float f1 = (float) (col1 >> 16 & 0xFF) / 255F;
        float f2 = (float) (col1 >> 8 & 0xFF) / 255F;
        float f3 = (float) (col1 & 0xFF) / 255F;

        float f4 = (float) (col2 >> 24 & 0xFF) / 255F;
        float f5 = (float) (col2 >> 16 & 0xFF) / 255F;
        float f6 = (float) (col2 >> 8 & 0xFF) / 255F;
        float f7 = (float) (col2 & 0xFF) / 255F;

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        BufferBuilder bufferBuilder = BufferHelper.INSTANCE.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix4f, x2, y, 0).color(f1, f2, f3, f).next();
        bufferBuilder.vertex(matrix4f, x, y, 0).color(f1, f2, f3, f).next();

        bufferBuilder.vertex(matrix4f, x, y2, 0).color(f5, f6, f7, f4).next();
        bufferBuilder.vertex(matrix4f, x2, y2, 0).color(f5, f6, f7, f4).next();

        BufferHelper.INSTANCE.drawWithShader(bufferBuilder, ShaderHelper.INSTANCE.getPosColorShader());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public void gradientSidewaysFill(MatrixStack poseStack, float x, float y, float x2, float y2, int col1, int col2) {
        Matrix4f matrix4f = poseStack.peek().getPositionMatrix();
        float f = (float) (col1 >> 24 & 0xFF) / 255F;
        float f1 = (float) (col1 >> 16 & 0xFF) / 255F;
        float f2 = (float) (col1 >> 8 & 0xFF) / 255F;
        float f3 = (float) (col1 & 0xFF) / 255F;

        float f4 = (float) (col2 >> 24 & 0xFF) / 255F;
        float f5 = (float) (col2 >> 16 & 0xFF) / 255F;
        float f6 = (float) (col2 >> 8 & 0xFF) / 255F;
        float f7 = (float) (col2 & 0xFF) / 255F;

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        BufferBuilder bufferBuilder = BufferHelper.INSTANCE.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix4f, x2, y, 0).color(f5, f6, f7, f4).next();
        bufferBuilder.vertex(matrix4f, x, y, 0).color(f1, f2, f3, f).next();

        bufferBuilder.vertex(matrix4f, x, y2, 0).color(f1, f2, f3, f).next();
        bufferBuilder.vertex(matrix4f, x2, y2, 0).color(f5, f6, f7, f4).next();

        BufferHelper.INSTANCE.drawWithShader(bufferBuilder, ShaderHelper.INSTANCE.getPosColorShader());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    //credits to 0x150 for this
    private void renderRoundedQuadInternal(Matrix4f matrix, float cr, float cg, float cb, float ca, double fromX, double fromY, double toX, double toY, double rad, double samples) {
        BufferBuilder buffer = BufferHelper.INSTANCE.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        double toX1 = toX - rad;
        double toY1 = toY - rad;
        double fromX1 = fromX + rad;
        double fromY1 = fromY + rad;
        double[][] map = new double[][] { new double[] { toX1, toY1 }, new double[] { toX1, fromY1 },
                new double[] { fromX1, fromY1 }, new double[] { fromX1, toY1 } };
        for (int i = 0; i < 4; i++) {
            double[] current = map[i];
            double max = (360 / 4d + i * 90d);
            for (double r = i * 90d; r < max; r += (90 / samples)) {
                float rad1 = (float) Math.toRadians(r);
                float sin = (float) (Math.sin(rad1) * rad);
                float cos = (float) (Math.cos(rad1) * rad);
                buffer.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F)
                        .color(cr, cg, cb, ca)
                        .next();
            }
            // make sure we render the corner properly by adding one final vertex at the end
            float rad1 = (float) Math.toRadians(max);
            float sin = (float) (Math.sin(rad1) * rad);
            float cos = (float) (Math.cos(rad1) * rad);
            buffer.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F)
                    .color(cr, cg, cb, ca)
                    .next();
        }
        BufferHelper.INSTANCE.drawWithShader(buffer, ShaderHelper.INSTANCE.getPosColorShader());
    }

    public void renderRoundedQuad(MatrixStack matrices, double fromX, double fromY, double toX, double toY, int c, double rad, double samples) {
        double height = toY - fromY;
        double width = toX - fromX;
        double smallestC = Math.min(height, width) / 2d;
        rad = Math.min(rad, smallestC);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Color color = ColorHelper.INSTANCE.getColor(c);
        setup2DRender(false);
        renderRoundedQuadInternal(matrix,
                color.getRed() / 255.f,
                color.getGreen() / 255.f,
                color.getBlue() / 255.f,
                color.getAlpha() / 255.f,
                fromX,
                fromY,
                toX,
                toY,
                rad,
                samples);
        //end2DRender();
    }

    public void outlineAndFill(MatrixStack poseStack, float x, float y, float x2, float y2, int bcolor, int icolor) {
        Matrix4f matrix = poseStack.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = BufferHelper.INSTANCE.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        float f = (float)(icolor >> 24 & 255) / 255.0F;
        float g = (float)(icolor >> 16 & 255) / 255.0F;
        float h = (float)(icolor >> 8 & 255) / 255.0F;
        float k = (float)(icolor & 255) / 255.0F;
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferBuilder.vertex(matrix, x, y2, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x2, y2, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x2, y, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(g, h, k, f).next();
        BufferHelper.INSTANCE.drawWithShader(bufferBuilder, ShaderHelper.INSTANCE.getPosColorShader());
        BufferHelper.INSTANCE.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        f = (float)(bcolor >> 24 & 255) / 255.0F;
        g = (float)(bcolor >> 16 & 255) / 255.0F;
        h = (float)(bcolor >> 8 & 255) / 255.0F;
        k = (float)(bcolor & 255) / 255.0F;
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x, y2, 0.0F).color(g, h, k, f).next();

        bufferBuilder.vertex(matrix, x, y2, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x2, y2, 0.0F).color(g, h, k, f).next();

        bufferBuilder.vertex(matrix, x2, y2, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x2, y, 0.0F).color(g, h, k, f).next();

        bufferBuilder.vertex(matrix, x2, y, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(g, h, k, f).next();
        BufferHelper.INSTANCE.drawWithShader(bufferBuilder, ShaderHelper.INSTANCE.getPosColorShader());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public void drawMap(MatrixStack poseStack, int x, int y, ItemStack stack) {
        MapState mapState = FilledMapItem.getOrCreateMapState(stack, Wrapper.INSTANCE.getWorld());
        if (mapState != null) {
            Render2DHelper.INSTANCE.bindTexture(MAP_BACKGROUND);
            DrawableHelper.drawTexture(poseStack, x, y, 0, 0, 150, 150, 150, 150);

            poseStack.push();
            poseStack.translate(x + 11, y + 11, 1000);
            VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
            Wrapper.INSTANCE.getMinecraft().gameRenderer.getMapRenderer().draw(poseStack, immediate, FilledMapItem.getMapId(stack), mapState, false, 15728880);
            immediate.draw();
            poseStack.pop();
        }
    }

    public void drawFullCircle(int cx, int cy, double r, int c, MatrixStack poseStack) {
        float f = (c >> 24 & 0xFF) / 255.0F;
        float f1 = (c >> 16 & 0xFF) / 255.0F;
        float f2 = (c >> 8 & 0xFF) / 255.0F;
        float f3 = (c & 0xFF) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        BufferBuilder bufferBuilder = BufferHelper.INSTANCE.begin(DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 360; i++) {
            double x = Math.sin(i * 3.141592653589793D / 180.0D) * r;
            double y = Math.cos(i * 3.141592653589793D / 180.0D) * r;
            bufferBuilder.vertex(cx + x, cy + y, -64).color(f1, f2, f3, f).next();
        }
        BufferHelper.INSTANCE.drawWithShader(bufferBuilder, ShaderHelper.INSTANCE.getPosColorShader());
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.defaultBlendFunc();
    }

    public void drawArc(float cx, float cy, double r, int c, int startpoint, double arc, int linewidth, MatrixStack poseStack) {
        float f = (c >> 24 & 0xFF) / 255.0F;
        float f1 = (c >> 16 & 0xFF) / 255.0F;
        float f2 = (c >> 8 & 0xFF) / 255.0F;
        float f3 = (c & 0xFF) / 255.0F;
        RenderSystem.lineWidth(linewidth);

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();

        BufferBuilder bufferBuilder = BufferHelper.INSTANCE.begin(DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);//TRIANGLE_STRIP is fucked too I guess

        for (int i = (int) startpoint; i <= arc; i += 1) {
            double x = Math.sin(i * 3.141592653589793D / 180.0D) * r;
            double y = Math.cos(i * 3.141592653589793D / 180.0D) * r;
            bufferBuilder.vertex(cx + x, cy + y, 0).color(f1, f2, f3, f).next();
        }
        BufferHelper.INSTANCE.drawWithShader(bufferBuilder, ShaderHelper.INSTANCE.getPosColorShader());
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.defaultBlendFunc();
    }

    public void drawHLine(MatrixStack poseStack, float par1, float par2, float par3, int par4) {
        if (par2 < par1) {
            float var5 = par1;
            par1 = par2;
            par2 = var5;
        }

        fill(poseStack, par1, par3, par2 + 1, par3 + 1, par4);
    }

    public void drawThinHLine(MatrixStack poseStack, float x, float y, float endX, int color) {
        Matrix4f matrix4f = poseStack.peek().getPositionMatrix();
        Color color1 = ColorHelper.INSTANCE.getColor(color);
        Render2DHelper.INSTANCE.setup2DRender(false);
        BufferBuilder bufferBuilder = BufferHelper.INSTANCE.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, x, y, 0).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, endX, y, 0).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        BufferHelper.INSTANCE.drawWithShader(bufferBuilder, ShaderHelper.INSTANCE.getPosColorShader());
        Render2DHelper.INSTANCE.end2DRender();
    }

    public void drawVLine(MatrixStack poseStack, float par1, float par2, float par3, int par4) {
        if (par3 < par2) {
            float var5 = par2;
            par2 = par3;
            par3 = var5;
        }

        fill(poseStack, par1, par2 + 1, par1 + 1, par3, par4);
    }

    public Color hex2Rgb(String colorStr) {
        try {
            return new Color(Integer.valueOf(colorStr.substring(2, 4), 16), Integer.valueOf(colorStr.substring(4, 6), 16), Integer.valueOf(colorStr.substring(6, 8), 16));
        } catch (Exception e) {
            return Color.WHITE;
        }
    }

    public boolean isHovered(float x, float y, float width, float height) {
        return x < MouseHelper.INSTANCE.getMouseX() && x + width > MouseHelper.INSTANCE.getMouseX() && y < MouseHelper.INSTANCE.getMouseY() && y + height > MouseHelper.INSTANCE.getMouseY();
    }

    public boolean hoversCircle(float centerX, float centerY, float radius) {
        Vec2f vec2f = new Vec2f(MouseHelper.INSTANCE.getMouseX(), MouseHelper.INSTANCE.getMouseY());
        float distance = ClientMathHelper.INSTANCE.getDistance2D(vec2f, new Vec2f(centerX, centerY));
        return distance <= radius;
    }

    public boolean isOnScreen(Vec3d pos) {
        if (pos.getZ() > -1 && pos.getZ() < 1) {
            return true;
        }
        return false;
    }

    public void drawItem(ItemStack stack, float xPosition, float yPosition) {
        drawItem(stack, xPosition, yPosition, 1);
    }
    public void drawItem(ItemStack stack, float xPosition, float yPosition, float scale) {
        String amountText = stack.getCount() != 1 ? stack.getCount() + "" : "";
        IItemRenderer iItemRenderer = (IItemRenderer) Wrapper.INSTANCE.getMinecraft().getItemRenderer();
        iItemRenderer.renderItemIntoGUI(stack, xPosition, yPosition);
        renderGuiItemOverlay(Wrapper.INSTANCE.getMinecraft().textRenderer, stack, xPosition - 0.5f, yPosition + 1, scale, amountText);
    }

    public void renderGuiItemOverlay(TextRenderer renderer, ItemStack stack, float x, float y, float scale, @Nullable String countLabel) {
        if (!stack.isEmpty()) {
            MatrixStack poseStack = new MatrixStack();
            if (stack.getCount() != 1 || countLabel != null) {
                String string = countLabel == null ? String.valueOf(stack.getCount()) : countLabel;
                poseStack.translate(0.0D, 0.0D, (double)(Wrapper.INSTANCE.getMinecraft().getItemRenderer().zOffset + 200.0F));
                VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
                renderer.draw(string, (float)(x + 19 - 2 - renderer.getWidth(string)), (float)(y + 6 + 3), 16777215, true, poseStack.peek().getPositionMatrix(), immediate, false, 0, 15728880);
                immediate.draw();
            }

            if (stack.isItemBarVisible()) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.disableBlend();
                int i = stack.getItemBarStep();
                int j = stack.getItemBarColor();
                this.fill(poseStack, x + 2, y + 13, x + 2 + 13, y + 13 + 2, 0xff000000);
                this.fill(poseStack, x + 2, y + 13, x + 2 + i, y + 13 + 1, new Color(j >> 16 & 255, j >> 8 & 255, j & 255, 255).getRGB());
                RenderSystem.enableBlend();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

            ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().player;
            float f = clientPlayerEntity == null ? 0.0F : clientPlayerEntity.getItemCooldownManager().getCooldownProgress(stack.getItem(), MinecraftClient.getInstance().getTickDelta());
            if (f > 0.0F) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                Tessellator tessellator2 = Tessellator.getInstance();
                BufferBuilder bufferBuilder2 = tessellator2.getBuffer();
                this.renderGuiQuad(bufferBuilder2, x, y + MathHelper.floor(16.0F * (1.0F - f)), 16, MathHelper.ceil(16.0F * f), 255, 255, 255, 127);
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

        }
    }

    private void renderGuiQuad(BufferBuilder buffer, float x, float y, float width, float height, int red, int green, int blue, int alpha) {
        BufferHelper.INSTANCE.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(x + 0, y + 0, 0.0D).color(red, green, blue, alpha).next();
        buffer.vertex(x + 0, y + height, 0.0D).color(red, green, blue, alpha).next();
        buffer.vertex(x + width, y + height, 0.0D).color(red, green, blue, alpha).next();
        buffer.vertex( x + width, y + 0, 0.0D).color(red, green, blue, alpha).next();
        BufferHelper.INSTANCE.drawWithShader(buffer, ShaderHelper.INSTANCE.getPosColorShader());
    }

    public Formatting getPercentFormatting(float percent) {
        if (percent <= 15)
            return Formatting.DARK_RED;
        else if (percent <= 25)
            return Formatting.RED;
        else if (percent <= 50)
            return Formatting.GOLD;
        else if (percent <= 75)
            return Formatting.YELLOW;
        return Formatting.GREEN;
    }

    public Vec3d to2D(Vec3d worldPos, MatrixStack poseStack) {
        Vec3d bound = Render3DHelper.INSTANCE.getRenderPosition(worldPos, poseStack);
        Vec3d twoD = to2D(bound.x, bound.y, bound.z);
        return new Vec3d(twoD.x, twoD.y, twoD.z);
    }

    private Vec3d to2D(double x, double y, double z) {
        int displayHeight = Wrapper.INSTANCE.getWindow().getHeight();
        Vector3D screenCoords = new Vector3D();
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        Matrix4x4 matrix4x4Proj = Matrix4x4.copyFromColumnMajor(RenderSystem.getProjectionMatrix());//no more joml :)
        Matrix4x4 matrix4x4Model = Matrix4x4.copyFromColumnMajor(RenderSystem.getModelViewMatrix());//but I do the math myself now :( (heck math)
        matrix4x4Proj.mul(matrix4x4Model).project((float) x, (float) y, (float) z, viewport, screenCoords);

        return new Vec3d(screenCoords.x / Render2DHelper.INSTANCE.getScaleFactor(), (displayHeight - screenCoords.y) / Render2DHelper.INSTANCE.getScaleFactor(), screenCoords.z);
    }

    public Vec3d getHeadPos(Entity entity, float partialTicks, MatrixStack poseStack) {
        Vec3d bound = Render3DHelper.INSTANCE.getEntityRenderPosition(entity, partialTicks).add(0, entity.getHeight() + 0.2, 0);
        Vector4f vector4f = new Vector4f((float)bound.x, (float)bound.y, (float)bound.z, 1.f);
        vector4f.transform(poseStack.peek().getPositionMatrix());
        Vec3d twoD = to2D(vector4f.getX(), vector4f.getY(), vector4f.getZ());
        return new Vec3d(twoD.x, twoD.y, twoD.z);
    }

    public Vec3d getFootPos(Entity entity, float partialTicks, MatrixStack poseStack) {
        Vec3d bound = Render3DHelper.INSTANCE.getEntityRenderPosition(entity, partialTicks, poseStack);
        Vec3d twoD = to2D(bound.x, bound.y, bound.z);
        return new Vec3d(twoD.x, twoD.y, twoD.z);
    }

    public Vec3d getPos(Entity entity, float yOffset, float partialTicks, MatrixStack poseStack) {
        Vec3d bound = Render3DHelper.INSTANCE.getEntityRenderPosition(entity, partialTicks).add(0, yOffset, 0);
        Vector4f vector4f = new Vector4f((float)bound.x, (float)bound.y, (float)bound.z, 1.f);
        vector4f.transform(poseStack.peek().getPositionMatrix());
        Vec3d twoD = to2D(vector4f.getX(), vector4f.getY(), vector4f.getZ());
        return new Vec3d(twoD.x, twoD.y, twoD.z);
    }

    public void bindTexture(Identifier identifier) {
        RenderSystem.setShaderTexture(0, identifier);
    }

    public void shaderColor(int hex) {
        float alpha = (hex >> 24 & 0xFF) / 255.0F;
        float red = (hex >> 16 & 0xFF) / 255.0F;
        float green = (hex >> 8 & 0xFF) / 255.0F;
        float blue = (hex & 0xFF) / 255.0F;
        RenderSystem.setShaderColor(red, green, blue, alpha);
    }
}
