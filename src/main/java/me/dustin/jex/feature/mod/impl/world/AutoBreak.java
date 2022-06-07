package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClickBlockFilter;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.misc.EventSetLevel;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.event.world.EventClickBlock;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import me.dustin.jex.feature.mod.core.Feature;

import java.awt.*;

public class AutoBreak extends Feature {

	public final Property<Boolean> showPositionProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Show Position")
			.value(true)
			.build();
	public final Property<Color> emptyColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
			.name("Empty Color")
			.value(new Color(255, 0, 255))
			.parent(showPositionProperty)
			.depends(parent -> (boolean) parent.value())
			.build();
	public final Property<Color> blockinspotColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
			.name("Has Block Color")
			.value(Color.BLUE)
			.parent(showPositionProperty)
			.depends(parent -> (boolean) parent.value())
			.build();
	public final Property<Color> miningColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
			.name("Mining Color")
			.value(Color.RED)
			.parent(showPositionProperty)
			.depends(parent -> (boolean) parent.value())
			.build();
	public final Property<Float> mineDistanceProperty = new Property.PropertyBuilder<Float>(this.getClass())
			.name("Mining Distance")
			.value(3f)
			.min(2)
			.max(6)
			.inc(0.1f)
			.build();

	private BlockPos pos;

	public AutoBreak() {
		super(Category.WORLD, "Set a block to auto re-break when a new block is there.");
	}

	@EventPointer
	private final EventListener<EventClickBlock> eventClickBlockEventListener = new EventListener<>(event -> {
		pos = event.getBlockPos().add(0.5, 0, 0.5);
	}, new ClickBlockFilter(EventClickBlock.Mode.PRE));

	@EventPointer
	private final EventListener<EventSetLevel> eventJoinWorldEventListener = new EventListener<>(event -> {
		pos = null;
	});

	@EventPointer
	private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
		if (pos != null && showPositionProperty.value()) {
			Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(pos.getX(), pos.getY(), pos.getZ());
			Block block = WorldHelper.INSTANCE.getBlock(pos);

			int color = emptyColorProperty.value().getRGB();

			if (block != Blocks.AIR)
				color = blockinspotColorProperty.value().getRGB();
			if (block != Blocks.AIR && getDistance(pos, Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ()) <= mineDistanceProperty.value())
				color = miningColorProperty.value().getRGB();
			Box bb = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
			Render3DHelper.INSTANCE.drawBox(((EventRender3D) event).getPoseStack(), bb, color);
		}
	});

	@EventPointer
	private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
		if (pos != null) {
			Block block = WorldHelper.INSTANCE.getBlock(pos);
			if (block != Blocks.AIR && getDistance(pos, Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ()) <= mineDistanceProperty.value()) {
				RotationVector rot = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
				((EventPlayerPackets) event).setRotation(rot);
				rot.normalize();
				Direction facing = Direction.fromRotation(-rot.getYaw());
				Wrapper.INSTANCE.getClientPlayerInteractionManager().updateBlockBreakingProgress(pos, facing);
				Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
			}
		}
	}, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

	public double getDistance(Vec3i vec, double xIn, double yIn, double zIn) {
		double d0 = (vec.getX() - xIn);
		double d1 = (vec.getY() - yIn);
		double d2 = (vec.getZ() - zIn);
		return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
	}

}
