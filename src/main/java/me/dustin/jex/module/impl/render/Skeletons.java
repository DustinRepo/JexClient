package me.dustin.jex.module.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import java.awt.*;

@ModClass(name = "Skeletons", category = ModCategory.VISUAL, description = "Draw player skeletons")
public class Skeletons extends Module {//it looks cool as fuck but seriously fuck this was a massive pain in the ass

    @Op(name = "Color", isColor = true)
    public int skeletonColor = 0xffffffff;

    @EventListener(events = {EventRender3D.class})
    private void runMethod(EventRender3D eventRender3D) {
        Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
            if (entity instanceof PlayerEntity && entity != Wrapper.INSTANCE.getLocalPlayer()) {
                MatrixStack matrixStack = eventRender3D.getOrigStack();
                Render3DHelper.INSTANCE.fixCameraRots(matrixStack);
                PlayerEntity playerEntity = (PlayerEntity)entity;
                Color color = ColorHelper.INSTANCE.getColor(skeletonColor);
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                RenderSystem.disableTexture();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
                RenderSystem.enableCull();

                Vec3d footPos = Render3DHelper.INSTANCE.getEntityRenderPosition(playerEntity, eventRender3D.getPartialTicks());
                PlayerEntityModel playerEntityModel = (PlayerEntityModel)((LivingEntityRenderer)Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().getRenderer(playerEntity)).getModel();

                boolean sneaking = playerEntity.isSneaking();

                ModelPart head = playerEntityModel.head;
                ModelPart leftArm = playerEntityModel.leftArm;
                ModelPart rightArm = playerEntityModel.rightArm;
                ModelPart leftLeg = playerEntityModel.leftLeg;
                ModelPart rightLeg = playerEntityModel.rightLeg;

                float[] headValues = {head.roll, head.yaw, head.pitch};
                float[] leftArmValues = {leftArm.roll, leftArm.yaw, leftArm.pitch};
                float[] rightArmValues = {rightArm.roll, rightArm.yaw, rightArm.pitch};
                float[] leftLegValues = {leftLeg.roll, leftLeg.yaw, leftLeg.pitch};
                float[] rightLegValues = {rightLeg.roll, rightLeg.yaw, rightLeg.pitch};

                matrixStack.translate(footPos.x, footPos.y, footPos.z);
                matrixStack.multiply(new Quaternion(new Vec3f(0, -1, 0), playerEntity.bodyYaw + 180, true));
                BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
                bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

                Matrix4f matrix4f = matrixStack.peek().getModel();
                bufferBuilder.vertex(matrix4f, 0, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                bufferBuilder.vertex(matrix4f, 0, sneaking ? 1.05f : 1.4f, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();//spine

                bufferBuilder.vertex(matrix4f, -0.37f, sneaking ? 1.05f : 1.35f, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();//shoulders
                bufferBuilder.vertex(matrix4f, 0.37f, sneaking ? 1.05f : 1.35f, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();

                bufferBuilder.vertex(matrix4f, -0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();//pelvis
                bufferBuilder.vertex(matrix4f, 0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();

                matrixStack.push();//head
                matrixStack.translate(0, sneaking ? 1.05f : 1.4f, 0);
                rotate(matrixStack, headValues);
                matrix4f = matrixStack.peek().getModel();
                bufferBuilder.vertex(matrix4f, 0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                bufferBuilder.vertex(matrix4f, 0, 0.15f, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                matrixStack.pop();

                matrixStack.push();//right leg
                matrixStack.translate(0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0);
                rotate(matrixStack, rightLegValues);
                matrix4f = matrixStack.peek().getModel();
                bufferBuilder.vertex(matrix4f, 0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                bufferBuilder.vertex(matrix4f, 0, -0.6f, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                matrixStack.pop();

                matrixStack.push();//left leg
                matrixStack.translate(-0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0);
                rotate(matrixStack, leftLegValues);
                matrix4f = matrixStack.peek().getModel();
                bufferBuilder.vertex(matrix4f, 0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                bufferBuilder.vertex(matrix4f, 0, -0.6f, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                matrixStack.pop();

                matrixStack.push();//right arm
                matrixStack.translate(0.37f, sneaking ? 1.05f : 1.35f, 0);
                rotate(matrixStack, rightArmValues);
                matrix4f = matrixStack.peek().getModel();
                bufferBuilder.vertex(matrix4f, 0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                bufferBuilder.vertex(matrix4f, 0, -0.55f, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                matrixStack.pop();

                matrixStack.push();//left arm
                matrixStack.translate(-0.37f, sneaking ? 1.05f : 1.35f, 0);
                rotate(matrixStack, leftArmValues);
                matrix4f = matrixStack.peek().getModel();
                bufferBuilder.vertex(matrix4f, 0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                bufferBuilder.vertex(matrix4f, 0, -0.55f, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                matrixStack.pop();

                bufferBuilder.end();
                BufferRenderer.draw(bufferBuilder);

                RenderSystem.enableTexture();
                RenderSystem.disableCull();
                RenderSystem.disableBlend();
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(true);
                Render3DHelper.INSTANCE.applyCameraRots(matrixStack);
                matrixStack.multiply(new Quaternion(new Vec3f(0, 1, 0), playerEntity.bodyYaw + 180, true));
                matrixStack.translate(-footPos.x, -footPos.y, -footPos.z);
            }
        });
    }

    private void rotate(MatrixStack matrix, float[] rotations) {
        if (rotations[0] != 0.0F) {
            matrix.multiply(Vec3f.POSITIVE_Z.getRadialQuaternion(rotations[0]));//roll
        }

        if (rotations[1] != 0.0F) {
            matrix.multiply(Vec3f.NEGATIVE_Y.getRadialQuaternion(rotations[1]));//yaw
        }

        if (rotations[2] != 0.0F) {
            matrix.multiply(Vec3f.NEGATIVE_X.getRadialQuaternion(rotations[2]));//pitch
        }

    }
}
