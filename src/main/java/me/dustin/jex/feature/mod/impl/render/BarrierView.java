package me.dustin.jex.feature.mod.impl.render;

import java.awt.*;
import java.util.ArrayList;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.BufferHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.BarrierBlock;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

public class BarrierView extends Feature {

	public final Property<Color> colorProperty = new Property.PropertyBuilder<Color>(this.getClass())
			.name("Color")
			.value(Color.RED)
			.build();

	private final ArrayList<BlockPos> renderPositions = new ArrayList<>();
	private final StopWatch stopWatch = new StopWatch();

	public BarrierView() {
		super(Category.VISUAL, "See barriers as if you are in creative");
	}

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
		ArrayList<Render3DHelper.BoxStorage> list = new ArrayList<>();
		renderPositions.forEach(blockPos -> {
			Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(blockPos);
			Box box = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
			list.add(new Render3DHelper.BoxStorage(box, colorProperty.value().getRGB()));
		});
		//Render3DHelper.INSTANCE.drawList(eventRender3D.getMatrixStack(), list);
		Render3DHelper.INSTANCE.setup3DRender(true);
		BufferBuilder bufferBuilder = BufferHelper.INSTANCE.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
		list.forEach(blockStorage -> {
			Box box = blockStorage.box();
			int color = blockStorage.color();
			Render3DHelper.INSTANCE.drawOutlineBox(event.getPoseStack(), box, color, false);
			Color color1 = ColorHelper.INSTANCE.getColor(color);
			Matrix4f matrix4f = event.getPoseStack().peek().getPositionMatrix();
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
        BufferHelper.INSTANCE.drawWithShader(bufferBuilder, ShaderHelper.INSTANCE.getPosColorShader());
		BufferHelper.INSTANCE.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		list.forEach(blockStorage -> {
			Box box = blockStorage.box();
			int color = blockStorage.color();
			Render3DHelper.INSTANCE.drawFilledBox(event.getPoseStack(), box, color & 0x45ffffff, false);
		});
		BufferHelper.INSTANCE.drawWithShader(bufferBuilder, ShaderHelper.INSTANCE.getPosColorShader());
		Render3DHelper.INSTANCE.end3DRender();
	});
}
