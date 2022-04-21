package me.dustin.jex.feature.mod.impl.render;

import java.awt.*;
import java.util.ArrayList;

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
import me.dustin.jex.helper.render.Render3DHelper.BoxStorage;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.BarrierBlock;
import net.minecraft.client.render.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

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
						BlockPos pos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().add(x, y, z);
						if (WorldHelper.INSTANCE.getBlock(pos) instanceof BarrierBlock) {
							renderPositions.add(pos);
						}
					}
				}
			}
			stopWatch.reset();
		}
		ArrayList<BoxStorage> list = new ArrayList<>();
		renderPositions.forEach(blockPos -> {
			Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(blockPos);
			Box box = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
			list.add(new BoxStorage(box, color));
		});
		//Render3DHelper.INSTANCE.drawList(eventRender3D.getMatrixStack(), list);
		Render3DHelper.INSTANCE.setup3DRender(true);
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
		list.forEach(blockStorage -> {
			Box box = blockStorage.box();
			int color = blockStorage.color();
			Render3DHelper.INSTANCE.drawOutlineBox(event.getMatrixStack(), box, color, false);
			Color color1 = ColorHelper.INSTANCE.getColor(color);
			Matrix4f matrix4f = event.getMatrixStack().peek().getPositionMatrix();
			bufferBuilder.vertex(matrix4f, (float)box.minX, (float)box.maxY, (float)box.minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
			bufferBuilder.vertex(matrix4f, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
			bufferBuilder.vertex(matrix4f, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
			bufferBuilder.vertex(matrix4f, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

			bufferBuilder.vertex(matrix4f, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
			bufferBuilder.vertex(matrix4f, (float)box.minX, (float)box.minY, (float)box.minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
			bufferBuilder.vertex(matrix4f, (float)box.minX, (float)box.minY, (float)box.maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
			bufferBuilder.vertex(matrix4f, (float)box.maxX, (float)box.minY, (float)box.minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

			bufferBuilder.vertex(matrix4f, (float)box.minX, (float)box.minY, (float)box.minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
			bufferBuilder.vertex(matrix4f, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
			bufferBuilder.vertex(matrix4f, (float)box.minX, (float)box.maxY, (float)box.minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
			bufferBuilder.vertex(matrix4f, (float)box.minX, (float)box.minY, (float)box.maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

			bufferBuilder.vertex(matrix4f, (float)box.maxX, (float)box.minY, (float)box.minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
			bufferBuilder.vertex(matrix4f, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
			bufferBuilder.vertex(matrix4f, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
			bufferBuilder.vertex(matrix4f, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

			bufferBuilder.vertex(matrix4f, (float)box.minX, (float)box.minY, (float)box.minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
			bufferBuilder.vertex(matrix4f, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
			bufferBuilder.vertex(matrix4f, (float)box.minX, (float)box.maxY, (float)box.minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
			bufferBuilder.vertex(matrix4f, (float)box.maxX, (float)box.minY, (float)box.minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

			bufferBuilder.vertex(matrix4f, (float)box.minX, (float)box.minY, (float)box.maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
			bufferBuilder.vertex(matrix4f, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
			bufferBuilder.vertex(matrix4f, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
			bufferBuilder.vertex(matrix4f, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
		});
		bufferBuilder.end();
		BufferRenderer.method_43433(bufferBuilder);

		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		list.forEach(blockStorage -> {
			Box box = blockStorage.box();
			int color = blockStorage.color();
			Render3DHelper.INSTANCE.drawFilledBox(event.getMatrixStack(), box, color & 0x45ffffff, false);
		});
		bufferBuilder.end();
		BufferRenderer.method_43433(bufferBuilder);
		Render3DHelper.INSTANCE.end3DRender();
	});
}
