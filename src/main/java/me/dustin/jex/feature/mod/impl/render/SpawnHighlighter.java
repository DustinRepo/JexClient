package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;

import java.util.ArrayList;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Show all blocks near you that mobs can spawn on.")
public class SpawnHighlighter extends Feature {

	@Op(name = "Check Delay", max = 1000, inc = 10)
	public int checkDelay = 250;
	@Op(name = "Radius", min = 10, max = 50, inc = 1)
	public int radius = 25;
	@OpChild(name = "Y Radius", min = 10, max = 50, inc = 1, parent = "Radius")
	public int yradius = 15;
	@Op(name = "Z Clipping")
	public boolean disableDepth = false;
	@Op(name = "Check Height")
	public boolean checkHeight = true;
	@Op(name = "Check Light")
	public boolean checkLight = true;
	@OpChild(name = "Light Value", min = 0, max = 15, inc = 1, parent = "Check Light")
	public int lightValue = 0;
	@Op(name = "Check Water")
	public boolean checkWater = true;
	@Op(name = "Check IsSpawnable")
	public boolean checkIsSpawnable = true;
	@Op(name = "Color", isColor = true)
	public int color = 0xffff0000;

	private final ArrayList<BlockPos> posList = new ArrayList<>();
	private final Timer timer = new Timer();

	@EventPointer
	private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
		ArrayList<Render3DHelper.BoxStorage> boxes = new ArrayList<>();
		posList.forEach(blockPos -> {
			Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
			Box box = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 0.05f, renderPos.z + 1);
			boxes.add(new Render3DHelper.BoxStorage(box, color));
		});
		Render3DHelper.INSTANCE.setup3DRender(disableDepth);
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		boxes.forEach(blockStorage -> {
			Box box = blockStorage.box();
			int color = blockStorage.color();
			Render3DHelper.INSTANCE.drawFilledBox(event.getMatrixStack(), box, color & 0x70ffffff, false);
		});
		bufferBuilder.end();
		BufferRenderer.draw(bufferBuilder);
		Render3DHelper.INSTANCE.end3DRender();
	});

	@EventPointer
	private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
		if (timer.hasPassed(checkDelay)) {
			posList.clear();
			for (int x = -radius; x < radius; x++) {
				for (int y = -yradius; y < 5; y++) {
					for (int z = -radius; z < radius; z++) {
						BlockPos blockPos = new BlockPos(Wrapper.INSTANCE.getLocalPlayer().getBlockPos().add(x, y, z));
						if (isValidBlock(blockPos)) {
							BlockPos abovePos = blockPos.add(0, 1, 0);
							posList.add(abovePos);
						}
					}
				}
			}
			timer.reset();
		}
	}, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

	private boolean isValidBlock(BlockPos blockPos) {
		BlockPos above = blockPos.add(0, 1, 0);
		BlockState thisState = Wrapper.INSTANCE.getWorld().getBlockState(blockPos);
		BlockState aboveState = Wrapper.INSTANCE.getWorld().getBlockState(above);
		BlockState twoAboveState = Wrapper.INSTANCE.getWorld().getBlockState(above.up());
		Block thisBlock = thisState.getBlock();
		Block aboveBlock = aboveState.getBlock();
		if (thisBlock == Blocks.AIR)
			return false;
		if (checkIsSpawnable)
			if (!WorldHelper.INSTANCE.canMobSpawnOntop(blockPos))
				return false;
		if (checkWater)
			if (WorldHelper.INSTANCE.isWaterlogged(above))
				return false;
		assert aboveBlock != null;
		if (!WorldHelper.INSTANCE.canMobSpawnInside(aboveState) || (checkHeight && !WorldHelper.INSTANCE.canMobSpawnInside(twoAboveState)))
			return false;
		if (checkLight) {
			int light = Wrapper.INSTANCE.getWorld().getLightLevel(LightType.BLOCK, above);
			return light <= lightValue;
		}
		return true;
	}
}
