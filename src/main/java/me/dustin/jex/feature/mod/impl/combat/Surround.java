package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;

public class Surround extends Feature {

	public Property<Boolean> autoTurnOffProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Auto Turn Off")
			.value(true)
			.build();
	public Property<Integer> placeDelayProperty = new Property.PropertyBuilder<Integer>(this.getClass())
			.name("Place Delay (MS)")
			.value(0)
			.min(0)
			.max(250)
			.build();
	public Property<Boolean> rotateProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Rotate")
			.value(true)
			.build();
	public Property<Color> placeColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
			.name("Place Color")
			.value(Color.RED)
			.build();

	private int stage = 0;
	private final StopWatch stopWatch = new StopWatch();
	private BlockPos placingPos;

	public Surround() {
		super(Category.COMBAT);
	}

	@EventPointer
	private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
		if (Wrapper.INSTANCE.getLocalPlayer() == null || Wrapper.INSTANCE.getWorld().isOutOfHeightLimit((int) Wrapper.INSTANCE.getLocalPlayer().getY()))
			return;
		if (placingPos != null) {
			RotationVector rotationVector = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), PlayerHelper.INSTANCE.getPlacingLookPos(placingPos));
			if (rotateProperty.value())
				((EventPlayerPackets) event).setRotation(rotationVector);
			PlayerHelper.INSTANCE.placeBlockInPos(placingPos, Hand.MAIN_HAND, true);
			placingPos = null;
		}
		if (!stopWatch.hasPassed(placeDelayProperty.value()))
			return;
		int savedSlot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
		int obby = InventoryHelper.INSTANCE.getFromHotbar(Items.OBSIDIAN);
		if (obby == -1) {
			this.stage = 0;
			if (autoTurnOffProperty.value())
				this.setState(false);
			return;
		}
		PlayerHelper.INSTANCE.centerOnBlock();
		InventoryHelper.INSTANCE.setSlot(obby, true, true);
		ArrayList<BlockPos> placePos = new ArrayList<>();
		BlockPos playerPos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos();
		placePos.add(playerPos.north());
		placePos.add(playerPos.east());
		placePos.add(playerPos.south());
		placePos.add(playerPos.west());
		if (placeDelayProperty.value() != 0) {
			if (stage == placePos.size()) {
				InventoryHelper.INSTANCE.setSlot(savedSlot, true, true);
				if (autoTurnOffProperty.value())
					this.setState(false);
				return;
			}
			BlockPos pos = placePos.get(stage);
			if (Wrapper.INSTANCE.getWorld().getBlockState(pos).getMaterial().isReplaceable()) {
				RotationVector rotationVector = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), PlayerHelper.INSTANCE.getPlacingLookPos(pos));
				if (rotateProperty.value())
					event.setRotation(rotationVector);
				placingPos = pos;
				stopWatch.reset();
			}
			stage++;
		} else {
			for (BlockPos pos : placePos) {
				if (Wrapper.INSTANCE.getWorld().getBlockState(pos).getMaterial().isReplaceable()) {
					PlayerHelper.INSTANCE.placeBlockInPos(pos, Hand.MAIN_HAND, true);
				}
			}
			InventoryHelper.INSTANCE.setSlot(savedSlot, true, true);
			if (autoTurnOffProperty.value())
				this.setState(false);
			stopWatch.reset();
			this.stage = 0;
		}
	}, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

	@EventPointer
	private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
		ArrayList<BlockPos> placePos = new ArrayList<>();
		BlockPos playerPos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos();
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
		Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(blockPos);
		Box bb = new Box(renderPos.getX(), renderPos.getY(), renderPos.getZ(), renderPos.getX() + 1, renderPos.getY() + 1, renderPos.getZ() + 1);
		Render3DHelper.INSTANCE.drawBox(event.getPoseStack(), bb, placeColorProperty.value().getRGB());
	});

	@Override
	public void onDisable() {
		super.onDisable();
		this.stage = 0;
	}
}
