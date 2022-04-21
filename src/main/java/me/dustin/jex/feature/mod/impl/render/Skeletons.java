package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;

import java.awt.*;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Draw player skeletons")
public class Skeletons extends Feature {//it looks cool as fuck but seriously fuck this was a massive pain in the ass

    @Op(name = "Color", isColor = true)
    public int skeletonColor = 0xffffffff;

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        MatrixStack matrixStack = event.getMatrixStack();
        float g = event.getPartialTicks();
        Render3DHelper.INSTANCE.setup3DRender(true);
        Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
            if (entity instanceof PlayerEntity playerEntity && (entity != Wrapper.INSTANCE.getLocalPlayer() || Wrapper.INSTANCE.getOptions().getPerspective() != Perspective.FIRST_PERSON)) {
                Color color = ColorHelper.INSTANCE.getColor(skeletonColor);
                Vec3d footPos = Render3DHelper.INSTANCE.getEntityRenderPosition(playerEntity, g);
                PlayerEntityRenderer livingEntityRenderer = (PlayerEntityRenderer)(LivingEntityRenderer<?, ?>) Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().getRenderer(playerEntity);
                PlayerEntityModel<PlayerEntity> playerEntityModel = (PlayerEntityModel)livingEntityRenderer.getModel();

                float h = MathHelper.lerpAngleDegrees(g, playerEntity.prevBodyYaw, playerEntity.bodyYaw);
                float j = MathHelper.lerpAngleDegrees(g, playerEntity.prevHeadYaw, playerEntity.headYaw);

                float q = playerEntity.limbAngle - playerEntity.limbDistance * (1.0F - g);
                float p = MathHelper.lerp(g, playerEntity.lastLimbDistance, playerEntity.limbDistance);
                float o = (float)playerEntity.age + g;
                float k = j - h;
                float m = EntityHelper.INSTANCE.getPitch(playerEntity);

                playerEntityModel.animateModel(playerEntity, q, p, g);
                playerEntityModel.setAngles(playerEntity, q, p, o, k, m);

                boolean swimming = playerEntity.isInSwimmingPose();
                boolean sneaking = playerEntity.isSneaking() && !swimming;
                boolean flying = playerEntity.isFallFlying();

                ModelPart head = playerEntityModel.head;
                ModelPart leftArm = playerEntityModel.leftArm;
                ModelPart rightArm = playerEntityModel.rightArm;
                ModelPart leftLeg = playerEntityModel.leftLeg;
                ModelPart rightLeg = playerEntityModel.rightLeg;

                matrixStack.translate(footPos.x, footPos.y, footPos.z);
                if (swimming) matrixStack.translate(0, 0.35f, 0);

                matrixStack.multiply(new Quaternion(new Vec3f(0, -1, 0), playerEntity.bodyYaw + 180, true));
                if (swimming || flying) matrixStack.multiply(new Quaternion(new Vec3f(-1, 0, 0), 90 + m, true));

                if (swimming) matrixStack.translate(0, -0.95f, 0);

                BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
                bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

                Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
                bufferBuilder.vertex(matrix4f, 0, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                bufferBuilder.vertex(matrix4f, 0, sneaking ? 1.05f : 1.4f, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();//spine

                bufferBuilder.vertex(matrix4f, -0.37f, sneaking ? 1.05f : 1.35f, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();//shoulders
                bufferBuilder.vertex(matrix4f, 0.37f, sneaking ? 1.05f : 1.35f, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();

                bufferBuilder.vertex(matrix4f, -0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();//pelvis
                bufferBuilder.vertex(matrix4f, 0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();

                matrixStack.push();//head
                matrixStack.translate(0, sneaking ? 1.05f : 1.4f, 0);
                rotate(matrixStack, head);
                matrix4f = matrixStack.peek().getPositionMatrix();
                bufferBuilder.vertex(matrix4f, 0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                bufferBuilder.vertex(matrix4f, 0, 0.15f, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                matrixStack.pop();

                matrixStack.push();//right leg
                matrixStack.translate(0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0);
                rotate(matrixStack, rightLeg);
                matrix4f = matrixStack.peek().getPositionMatrix();
                bufferBuilder.vertex(matrix4f, 0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                bufferBuilder.vertex(matrix4f, 0, -0.6f, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                matrixStack.pop();

                matrixStack.push();//left leg
                matrixStack.translate(-0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0);
                rotate(matrixStack, leftLeg);
                matrix4f = matrixStack.peek().getPositionMatrix();
                bufferBuilder.vertex(matrix4f, 0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                bufferBuilder.vertex(matrix4f, 0, -0.6f, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                matrixStack.pop();

                matrixStack.push();//right arm
                matrixStack.translate(0.37f, sneaking ? 1.05f : 1.35f, 0);
                rotate(matrixStack, rightArm);
                matrix4f = matrixStack.peek().getPositionMatrix();
                bufferBuilder.vertex(matrix4f, 0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                bufferBuilder.vertex(matrix4f, 0, -0.55f, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                matrixStack.pop();

                matrixStack.push();//left arm
                matrixStack.translate(-0.37f, sneaking ? 1.05f : 1.35f, 0);
                rotate(matrixStack, leftArm);
                matrix4f = matrixStack.peek().getPositionMatrix();
                bufferBuilder.vertex(matrix4f, 0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                bufferBuilder.vertex(matrix4f, 0, -0.55f, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                matrixStack.pop();

                bufferBuilder.end();
                BufferRenderer.method_43433(bufferBuilder);

                if (swimming) matrixStack.translate(0, 0.95f, 0);
                if (swimming || flying) matrixStack.multiply(new Quaternion(new Vec3f(1, 0, 0), 90 + m, true));
                if (swimming) matrixStack.translate(0, -0.35f, 0);

                matrixStack.multiply(new Quaternion(new Vec3f(0, 1, 0), playerEntity.bodyYaw + 180, true));
                matrixStack.translate(-footPos.x, -footPos.y, -footPos.z);
            }
        });
        Render3DHelper.INSTANCE.end3DRender();
    });

    private void rotate(MatrixStack matrix, ModelPart modelPart) {
        if (modelPart.roll != 0.0F) {
            matrix.multiply(Vec3f.POSITIVE_Z.getRadialQuaternion(modelPart.roll));
        }

        if (modelPart.yaw != 0.0F) {
            matrix.multiply(Vec3f.NEGATIVE_Y.getRadialQuaternion(modelPart.yaw));
        }

        if (modelPart.pitch != 0.0F) {
            matrix.multiply(Vec3f.NEGATIVE_X.getRadialQuaternion(modelPart.pitch));
        }
    }
}
