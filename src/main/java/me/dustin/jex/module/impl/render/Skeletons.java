package me.dustin.jex.module.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import java.awt.*;

@ModClass(name = "Skeletons", category = ModCategory.VISUAL, description = "Draw player skeletons")
public class Skeletons /*extends Module*/ {

    @Op(name = "Color", isColor = true)
    public int skeletonColor = 0xffffffff;

    @EventListener(events = {EventRender3D.class})
    private void runMethod(EventRender3D eventRender3D) {
        Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
            if (entity instanceof PlayerEntity) {
                MatrixStack matrixStack = eventRender3D.getMatrixStack();
                PlayerEntity playerEntity = (PlayerEntity)entity;
                Color color = ColorHelper.INSTANCE.getColor(skeletonColor);
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                RenderSystem.disableTexture();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
                RenderSystem.enableCull();
                matrixStack.push();

                Vec3d footPos = Render3DHelper.INSTANCE.getEntityRenderPosition(playerEntity, eventRender3D.getPartialTicks());

                matrixStack.multiply(new Quaternion(new Vec3f(0, 0, 1f), playerEntity.bodyYaw, true));
                BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
                bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

                bufferBuilder.vertex(footPos.x, footPos.y + 0.7, footPos.z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                bufferBuilder.vertex(footPos.x, footPos.y + 1.4, footPos.z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();//spine

                bufferBuilder.vertex(footPos.x, footPos.y + 1.4, footPos.z - 0.35).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();//shoulders
                bufferBuilder.vertex(footPos.x, footPos.y + 1.4, footPos.z + 0.35).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();

                bufferBuilder.vertex(footPos.x, footPos.y + 0.7, footPos.z - 0.15).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();//pelvis
                bufferBuilder.vertex(footPos.x, footPos.y + 0.7, footPos.z + 0.15).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();

                bufferBuilder.vertex(footPos.x, footPos.y + 0.7, footPos.z - 0.15).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();//right leg
                bufferBuilder.vertex(footPos.x, footPos.y + 0.1, footPos.z - 0.15).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();

                bufferBuilder.vertex(footPos.x, footPos.y + 0.7, footPos.z + 0.15).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();//left leg
                bufferBuilder.vertex(footPos.x, footPos.y + 0.1, footPos.z + 0.15).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();

                bufferBuilder.vertex(footPos.x, footPos.y + 1.4, footPos.z + 0.35).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();//left arm
                bufferBuilder.vertex(footPos.x, footPos.y + 0.8, footPos.z + 0.35).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();

                bufferBuilder.vertex(footPos.x, footPos.y + 1.4, footPos.z - 0.35).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();//right arm
                bufferBuilder.vertex(footPos.x, footPos.y + 0.8, footPos.z - 0.35).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();

                bufferBuilder.end();
                BufferRenderer.draw(bufferBuilder);

                matrixStack.pop();
                RenderSystem.enableTexture();
                RenderSystem.disableCull();
                RenderSystem.disableBlend();
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(true);
            }
        });
    }

}
