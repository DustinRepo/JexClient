package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.misc.EventJoinWorld;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.event.world.EventClickBlock;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Set a block to auto re-break when a new block is there.")
public class AutoBreak extends Feature {

	@Op(name = "Show Position")
	public boolean showPosition = true;

	@Op(name = "Mining Distance", min = 2, max = 6, inc = 0.1f)
	public float mineDistance = 3;

	@OpChild(name = "Empty Color", isColor = true, parent = "Show Position")
	public int emptyColor = 0xffff00ff;

	@OpChild(name = "Has Block Color", isColor = true, parent = "Show Position")
	public int blockinspotColor = 0xff0000ff;

	@OpChild(name = "Mining Color", isColor = true, parent = "Show Position")
	public int miningColor = 0xffff0000;

	private BlockPos pos;

	@EventPointer
	private final EventListener<EventClickBlock> eventClickBlockEventListener = new EventListener<>(event -> {
		pos = event.getBlockPos().add(0.5, 0, 0.5);
	});

	@EventPointer
	private final EventListener<EventJoinWorld> eventJoinWorldEventListener = new EventListener<>(event -> {
		pos = null;
	});

	@EventPointer
	private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
		if (pos != null && showPosition) {
			Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(pos.getX(), pos.getY(), pos.getZ());
			Block block = WorldHelper.INSTANCE.getBlock(pos);

			int color = emptyColor;

			if (block != Blocks.AIR)
				color = blockinspotColor;
			if (block != Blocks.AIR && getDistance(pos, Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ()) <= mineDistance)
				color = miningColor;
			Box bb = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
			Render3DHelper.INSTANCE.drawBox(((EventRender3D) event).getMatrixStack(), bb, color);
		}
	});

	@EventPointer
	private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
		if (pos != null) {
			Block block = WorldHelper.INSTANCE.getBlock(pos);
			if (block != Blocks.AIR && getDistance(pos, Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ()) <= mineDistance) {
				RotationVector rot = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
				((EventPlayerPackets) event).setRotation(rot);
				rot.normalize();
				Direction facing = Direction.fromRotation(-rot.getYaw());
				Wrapper.INSTANCE.getInteractionManager().updateBlockBreakingProgress(pos, facing);
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
