package me.dustin.jex.feature.mod.impl.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.awt.*;
import java.util.ArrayList;

import com.mojang.math.Matrix4f;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.core.Feature.Category;
import me.dustin.jex.feature.mod.core.Feature.Manifest;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BarrierBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Manifest(category = Category.VISUAL, description = "See barriers as if you are in creative")
public class BarrierView extends Feature {
	
	@Op(name = "Color", isColor = true)
	public int color = 0xffff0000;
	
	private final ArrayList<BlockPos> renderPositions = new ArrayList<>();
	private final StopWatch stopWatch = new StopWatch();

	@EventPointer
	private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
		if (stopWatch.hasPassed(250)) {
			renderPositions.clear();
			for (int x = -4; x < 4; x++) {
				for (int y = -4; y < 4; y++) {
					for (int z = -4; z < 4; z++) {
						BlockPos pos = Wrapper.INSTANCE.getLocalPlayer().blockPosition().offset(x, y, z);
						if (WorldHelper.INSTANCE.getBlock(pos) instanceof BarrierBlock) {
							renderPositions.add(pos);
						}
					}
				}
			}
			stopWatch.reset();
		}
		ArrayList<Render3DHelper.BoxStorage> list = new ArrayList<>();
		renderPositions.forEach(blockPos -> {
			Vec3 renderPos = Render3DHelper.INSTANCE.getRenderPosition(blockPos);
			AABB box = new AABB(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
			list.add(new Render3DHelper.BoxStorage(box, color));
		});
		//Render3DHelper.INSTANCE.drawList(eventRender3D.getMatrixStack(), list);
		Render3DHelper.INSTANCE.setup3DRender(true);
		BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
		bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
		list.forEach(blockStorage -> {
			AABB box = blockStorage.box();
			int color = blockStorage.color();
			Render3DHelper.INSTANCE.drawOutlineBox(event.getPoseStack(), box, color, false);
			Color color1 = ColorHelper.INSTANCE.getColor(color);
			Matrix4f matrix4f = event.getPoseStack().last().pose();
			bufferBuilder.vertex(matrix4f, (float)box.minX, (float)box.maxY, (float)box.minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
			bufferBuilder.vertex(matrix4f, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
			bufferBuilder.vertex(matrix4f, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
			bufferBuilder.vertex(matrix4f, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();

			bufferBuilder.vertex(matrix4f, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
			bufferBuilder.vertex(matrix4f, (float)box.minX, (float)box.minY, (float)box.minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
			bufferBuilder.vertex(matrix4f, (float)box.minX, (float)box.minY, (float)box.maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
			bufferBuilder.vertex(matrix4f, (float)box.maxX, (float)box.minY, (float)box.minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();

			bufferBuilder.vertex(matrix4f, (float)box.minX, (float)box.minY, (float)box.minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
			bufferBuilder.vertex(matrix4f, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
			bufferBuilder.vertex(matrix4f, (float)box.minX, (float)box.maxY, (float)box.minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
			bufferBuilder.vertex(matrix4f, (float)box.minX, (float)box.minY, (float)box.maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();

			bufferBuilder.vertex(matrix4f, (float)box.maxX, (float)box.minY, (float)box.minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
			bufferBuilder.vertex(matrix4f, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
			bufferBuilder.vertex(matrix4f, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
			bufferBuilder.vertex(matrix4f, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();

			bufferBuilder.vertex(matrix4f, (float)box.minX, (float)box.minY, (float)box.minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
			bufferBuilder.vertex(matrix4f, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
			bufferBuilder.vertex(matrix4f, (float)box.minX, (float)box.maxY, (float)box.minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
			bufferBuilder.vertex(matrix4f, (float)box.maxX, (float)box.minY, (float)box.minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();

			bufferBuilder.vertex(matrix4f, (float)box.minX, (float)box.minY, (float)box.maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
			bufferBuilder.vertex(matrix4f, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
			bufferBuilder.vertex(matrix4f, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
			bufferBuilder.vertex(matrix4f, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
		});
		bufferBuilder.clear();
        BufferUploader.drawWithShader(bufferBuilder.end());
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		list.forEach(blockStorage -> {
			AABB box = blockStorage.box();
			int color = blockStorage.color();
			Render3DHelper.INSTANCE.drawFilledBox(event.getPoseStack(), box, color & 0x45ffffff, false);
		});
		bufferBuilder.clear();
        BufferUploader.drawWithShader(bufferBuilder.end());
		Render3DHelper.INSTANCE.end3DRender();
	});
}
