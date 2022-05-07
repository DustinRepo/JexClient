package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.render.Render3DHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.awt.*;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Draw player skeletons")
public class Skeletons extends Feature {//it looks cool as fuck but seriously fuck this was a massive pain in the ass

    @Op(name = "Color", isColor = true)
    public int skeletonColor = 0xffffffff;

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        PoseStack matrixStack = event.getPoseStack();
        float g = event.getPartialTicks();
        Render3DHelper.INSTANCE.setup3DRender(true);
        Wrapper.INSTANCE.getWorld().entitiesForRendering().forEach(entity -> {
            if (entity instanceof Player playerEntity && (entity != Wrapper.INSTANCE.getLocalPlayer() || Wrapper.INSTANCE.getOptions().getCameraType() != CameraType.FIRST_PERSON)) {
                Color color = ColorHelper.INSTANCE.getColor(skeletonColor);
                Vec3 footPos = Render3DHelper.INSTANCE.getEntityRenderPosition(playerEntity, g);
                PlayerRenderer livingEntityRenderer = (PlayerRenderer)(LivingEntityRenderer<?, ?>) Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().getRenderer(playerEntity);
                PlayerModel<Player> playerEntityModel = (PlayerModel)livingEntityRenderer.getModel();

                float h = Mth.rotLerp(g, playerEntity.yBodyRotO, playerEntity.yBodyRot);
                float j = Mth.rotLerp(g, playerEntity.yHeadRotO, playerEntity.yHeadRot);

                float q = playerEntity.animationPosition - playerEntity.animationSpeed * (1.0F - g);
                float p = Mth.lerp(g, playerEntity.animationSpeedOld, playerEntity.animationSpeed);
                float o = (float)playerEntity.tickCount + g;
                float k = j - h;
                float m = EntityHelper.INSTANCE.getPitch(playerEntity);

                playerEntityModel.prepareMobModel(playerEntity, q, p, g);
                playerEntityModel.setupAnim(playerEntity, q, p, o, k, m);

                boolean swimming = playerEntity.isVisuallySwimming();
                boolean sneaking = playerEntity.isShiftKeyDown() && !swimming;
                boolean flying = playerEntity.isFallFlying();

                ModelPart head = playerEntityModel.head;
                ModelPart leftArm = playerEntityModel.leftArm;
                ModelPart rightArm = playerEntityModel.rightArm;
                ModelPart leftLeg = playerEntityModel.leftLeg;
                ModelPart rightLeg = playerEntityModel.rightLeg;

                matrixStack.translate(footPos.x, footPos.y, footPos.z);
                if (swimming) matrixStack.translate(0, 0.35f, 0);

                matrixStack.mulPose(new Quaternion(new Vector3f(0, -1, 0), playerEntity.yBodyRot + 180, true));
                if (swimming || flying) matrixStack.mulPose(new Quaternion(new Vector3f(-1, 0, 0), 90 + m, true));

                if (swimming) matrixStack.translate(0, -0.95f, 0);

                BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
                bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

                Matrix4f matrix4f = matrixStack.last().pose();
                bufferBuilder.vertex(matrix4f, 0, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                bufferBuilder.vertex(matrix4f, 0, sneaking ? 1.05f : 1.4f, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();//spine

                bufferBuilder.vertex(matrix4f, -0.37f, sneaking ? 1.05f : 1.35f, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();//shoulders
                bufferBuilder.vertex(matrix4f, 0.37f, sneaking ? 1.05f : 1.35f, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

                bufferBuilder.vertex(matrix4f, -0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();//pelvis
                bufferBuilder.vertex(matrix4f, 0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

                matrixStack.pushPose();//head
                matrixStack.translate(0, sneaking ? 1.05f : 1.4f, 0);
                rotate(matrixStack, head);
                matrix4f = matrixStack.last().pose();
                bufferBuilder.vertex(matrix4f, 0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                bufferBuilder.vertex(matrix4f, 0, 0.15f, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                matrixStack.popPose();

                matrixStack.pushPose();//right leg
                matrixStack.translate(0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0);
                rotate(matrixStack, rightLeg);
                matrix4f = matrixStack.last().pose();
                bufferBuilder.vertex(matrix4f, 0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                bufferBuilder.vertex(matrix4f, 0, -0.6f, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                matrixStack.popPose();

                matrixStack.pushPose();//left leg
                matrixStack.translate(-0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0);
                rotate(matrixStack, leftLeg);
                matrix4f = matrixStack.last().pose();
                bufferBuilder.vertex(matrix4f, 0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                bufferBuilder.vertex(matrix4f, 0, -0.6f, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                matrixStack.popPose();

                matrixStack.pushPose();//right arm
                matrixStack.translate(0.37f, sneaking ? 1.05f : 1.35f, 0);
                rotate(matrixStack, rightArm);
                matrix4f = matrixStack.last().pose();
                bufferBuilder.vertex(matrix4f, 0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                bufferBuilder.vertex(matrix4f, 0, -0.55f, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                matrixStack.popPose();

                matrixStack.pushPose();//left arm
                matrixStack.translate(-0.37f, sneaking ? 1.05f : 1.35f, 0);
                rotate(matrixStack, leftArm);
                matrix4f = matrixStack.last().pose();
                bufferBuilder.vertex(matrix4f, 0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                bufferBuilder.vertex(matrix4f, 0, -0.55f, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                matrixStack.popPose();

                bufferBuilder.clear();
                BufferUploader.drawWithShader(bufferBuilder.end());

                if (swimming) matrixStack.translate(0, 0.95f, 0);
                if (swimming || flying) matrixStack.mulPose(new Quaternion(new Vector3f(1, 0, 0), 90 + m, true));
                if (swimming) matrixStack.translate(0, -0.35f, 0);

                matrixStack.mulPose(new Quaternion(new Vector3f(0, 1, 0), playerEntity.yBodyRot + 180, true));
                matrixStack.translate(-footPos.x, -footPos.y, -footPos.z);
            }
        });
        Render3DHelper.INSTANCE.end3DRender();
    });

    private void rotate(PoseStack matrix, ModelPart modelPart) {
        if (modelPart.zRot != 0.0F) {
            matrix.mulPose(Vector3f.ZP.rotation(modelPart.zRot));
        }

        if (modelPart.yRot != 0.0F) {
            matrix.mulPose(Vector3f.YN.rotation(modelPart.yRot));
        }

        if (modelPart.xRot != 0.0F) {
            matrix.mulPose(Vector3f.XN.rotation(modelPart.xRot));
        }
    }
}
