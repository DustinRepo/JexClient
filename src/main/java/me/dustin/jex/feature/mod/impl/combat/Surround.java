package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import me.dustin.jex.feature.option.annotate.Op;
import java.util.ArrayList;

@Feature.Manifest(category = Feature.Category.COMBAT, description = "Automatically place obsidian around your feet to defend from crystals")
public class Surround extends Feature {

	@Op(name = "Auto Turn Off")
	public boolean autoTurnOff = true;
	@Op(name = "Place Delay (MS)", min = 0, max = 250)
	public int placeDelay = 0;
	@Op(name = "Rotate")
	public boolean rotate = true;
	@Op(name = "Place Color", isColor = true)
	public int placeColor = 0xffff0000;

	private int stage = 0;
	private StopWatch stopWatch = new StopWatch();
	private BlockPos placingPos;

	@EventPointer
	private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
		if (Wrapper.INSTANCE.getLocalPlayer() == null || Wrapper.INSTANCE.getWorld().isOutsideBuildHeight((int) Wrapper.INSTANCE.getLocalPlayer().getY()))
			return;
		if (placingPos != null) {
			RotationVector rotationVector = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), PlayerHelper.INSTANCE.getPlacingLookPos(placingPos));
			if (rotate)
				((EventPlayerPackets) event).setRotation(rotationVector);
			PlayerHelper.INSTANCE.placeBlockInPos(placingPos, InteractionHand.MAIN_HAND, true);
			placingPos = null;
		}
		if (!stopWatch.hasPassed(placeDelay))
			return;
		int savedSlot = InventoryHelper.INSTANCE.getInventory().selected;
		int obby = InventoryHelper.INSTANCE.getFromHotbar(Items.OBSIDIAN);
		if (obby == -1) {
			this.stage = 0;
			if (autoTurnOff)
				this.setState(false);
			return;
		}
		PlayerHelper.INSTANCE.centerOnBlock();
		InventoryHelper.INSTANCE.setSlot(obby, true, true);
		ArrayList<BlockPos> placePos = new ArrayList<>();
		BlockPos playerPos = Wrapper.INSTANCE.getLocalPlayer().blockPosition();
		placePos.add(playerPos.north());
		placePos.add(playerPos.east());
		placePos.add(playerPos.south());
		placePos.add(playerPos.west());
		if (placeDelay != 0) {
			if (stage == placePos.size()) {
				InventoryHelper.INSTANCE.setSlot(savedSlot, true, true);
				if (autoTurnOff)
					this.setState(false);
				return;
			}
			BlockPos pos = placePos.get(stage);
			if (Wrapper.INSTANCE.getWorld().getBlockState(pos).getMaterial().isReplaceable()) {
				RotationVector rotationVector = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), PlayerHelper.INSTANCE.getPlacingLookPos(pos));
				if (rotate)
					((EventPlayerPackets) event).setRotation(rotationVector);
				placingPos = pos;
				stopWatch.reset();
			}
			stage++;
		} else {
			for (BlockPos pos : placePos) {
				if (Wrapper.INSTANCE.getWorld().getBlockState(pos).getMaterial().isReplaceable()) {
					PlayerHelper.INSTANCE.placeBlockInPos(pos, InteractionHand.MAIN_HAND, true);
				}
			}
			InventoryHelper.INSTANCE.setSlot(savedSlot, true, true);
			if (autoTurnOff)
				this.setState(false);
			stopWatch.reset();
			this.stage = 0;
		}
	}, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

	@EventPointer
	private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
		ArrayList<BlockPos> placePos = new ArrayList<>();
		BlockPos playerPos = Wrapper.INSTANCE.getLocalPlayer().blockPosition();
		placePos.add(playerPos.north());
		placePos.add(playerPos.east());
		placePos.add(playerPos.south());
		placePos.add(playerPos.west());
		BlockPos blockPos = null;
		for (BlockPos pos : placePos) {
			if (Wrapper.INSTANCE.getWorld().getBlockState(pos).getMaterial().isReplaceable()) {
				blockPos = pos;
				break;
			}
		}
		if (blockPos == null)
			return;
		Vec3 renderPos = Render3DHelper.INSTANCE.getRenderPosition(blockPos);
		AABB bb = new AABB(renderPos.x(), renderPos.y(), renderPos.z(), renderPos.x() + 1, renderPos.y() + 1, renderPos.z() + 1);
		Render3DHelper.INSTANCE.drawBox(event.getPoseStack(), bb, placeColor);
	});

	@Override
	public void onDisable() {
		super.onDisable();
		this.stage = 0;
	}
}
