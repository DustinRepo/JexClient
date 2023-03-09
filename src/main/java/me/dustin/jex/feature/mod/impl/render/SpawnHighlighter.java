package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.impl.world.SpawnSphere;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.BufferHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import me.dustin.jex.feature.mod.core.Feature;

import java.awt.*;
import java.util.ArrayList;

public class SpawnHighlighter extends Feature {

	public final Property<Long> checkDelayProperty = new Property.PropertyBuilder<Long>(this.getClass())
			.name("Check Delay")
			.value(250L)
			.max(1000)
			.inc(10)
			.build();
	public final Property<Integer> radiusProperty = new Property.PropertyBuilder<Integer>(this.getClass())
			.name("Radius")
			.value(25)
			.min(10)
			.max(50)
			.inc(1)
			.build();
	public final Property<Integer> yradiusProperty = new Property.PropertyBuilder<Integer>(this.getClass())
			.name("Y Radius")
			.value(15)
			.min(10)
			.max(50)
			.inc(1)
			.parent(radiusProperty)
			.build();
	public final Property<Boolean> disableDepthProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Z Clipping")
			.value(false)
			.build();
	public final Property<Boolean> checkHeightProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Check Height")
			.value(true)
			.build();
	public final Property<Boolean> checkLightProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Check Light")
			.value(true)
			.build();
	public final Property<Integer> lightValueProperty = new Property.PropertyBuilder<Integer>(this.getClass())
			.name("Light Value")
			.value(0)
			.min(0)
			.max(15)
			.parent(radiusProperty)
			.build();
	public final Property<Boolean> checkWaterProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Check Water")
			.value(true)
			.build();
	public final Property<Boolean> checkIsSpawnableProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Check IsSpawnable")
			.value(true)
			.build();
	public final Property<Color> colorProperty = new Property.PropertyBuilder<Color>(this.getClass())
			.name("Color")
			.value(Color.RED)
			.build();
	public final Property<Color> spawnSphereColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
			.name("SpawnSphere Color")
			.value(new Color(0, 161, 255))
			.build();

	private final ArrayList<BlockPos> posList = new ArrayList<>();
	private final StopWatch stopWatch = new StopWatch();

	public SpawnHighlighter() {
		super(Category.VISUAL);
	}

	@EventPointer
	private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
		ArrayList<Render3DHelper.BoxStorage> boxes = new ArrayList<>();
		posList.forEach(blockPos -> {
			int color = this.colorProperty.value().getRGB();
			if (Feature.getState(SpawnSphere.class)) {
				Vec3d pos = Feature.get(SpawnSphere.class).pos;
				if (ClientMathHelper.INSTANCE.getDistance(pos, Vec3d.of(blockPos)) <= 128)
					color = spawnSphereColorProperty.value().getRGB();
			}
			Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
			Box box = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 0.05f, renderPos.z + 1);
			boxes.add(new Render3DHelper.BoxStorage(box, color));
		});
		Render3DHelper.INSTANCE.setup3DRender(disableDepthProperty.value());
		BufferBuilder bufferBuilder = BufferHelper.INSTANCE.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		boxes.forEach(blockStorage -> {
			Box box = blockStorage.box();
			int color = blockStorage.color();
			Render3DHelper.INSTANCE.drawFilledBox(event.getPoseStack(), box, color & 0x70ffffff, false);
		});
		BufferHelper.INSTANCE.drawWithShader(bufferBuilder, ShaderHelper.INSTANCE.getPosColorShader());
		Render3DHelper.INSTANCE.end3DRender();
	});

	@EventPointer
	private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
		if (stopWatch.hasPassed(checkDelayProperty.value())) {
			posList.clear();
			for (int x = -radiusProperty.value(); x < radiusProperty.value(); x++) {
				for (int y = -yradiusProperty.value(); y < 5; y++) {
					for (int z = -radiusProperty.value(); z < radiusProperty.value(); z++) {
						BlockPos blockPos = new BlockPos(Wrapper.INSTANCE.getLocalPlayer().getBlockPos().add(x, y, z));
						if (isValidBlock(blockPos)) {
							BlockPos abovePos = blockPos.add(0, 1, 0);
							posList.add(abovePos);
						}
					}
				}
			}
			stopWatch.reset();
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
		if (checkIsSpawnableProperty.value())
			if (!WorldHelper.INSTANCE.canMobSpawnOntop(blockPos))
				return false;
		if (checkWaterProperty.value())
			if (WorldHelper.INSTANCE.isWaterlogged(above))
				return false;
		assert aboveBlock != null;
		if (!WorldHelper.INSTANCE.canMobSpawnInside(aboveState) || (checkHeightProperty.value() && !WorldHelper.INSTANCE.canMobSpawnInside(twoAboveState)))
			return false;
		if (checkLightProperty.value()) {
			int light = Wrapper.INSTANCE.getWorld().getLightLevel(LightType.BLOCK, above);
			return light <= lightValueProperty.value();
		}
		return true;
	}
}
