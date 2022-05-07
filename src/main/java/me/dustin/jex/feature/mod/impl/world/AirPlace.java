package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.MousePressFilter;
import me.dustin.jex.event.misc.EventMouseButton;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.BlockOverlay;
import me.dustin.jex.feature.option.annotate.Op;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Gives you the ability to place blocks in the air. (Anticheats usually block this)")
public class AirPlace extends Feature {

	@Op(name = "Liquids")
	public boolean liquids = true;
	@Op(name = "Reach", min = 3, max = 6, inc = 0.1f)
	public float reach = 4.5f;

	@EventPointer
	private final EventListener<EventMouseButton> eventMouseButtonEventListener = new EventListener<>(event -> {
			HitResult hitResult = Wrapper.INSTANCE.getLocalPlayer().pick(reach, Wrapper.INSTANCE.getMinecraft().getFrameTime(), false);
			if (hitResult instanceof BlockHitResult blockHitResult) {
				if (canReplaceBlock(WorldHelper.INSTANCE.getBlock(blockHitResult.getBlockPos())) && Wrapper.INSTANCE.getLocalPlayer().getMainHandItem().getItem() instanceof BlockItem) {
					Wrapper.INSTANCE.getMultiPlayerGameMode().useItemOn(Wrapper.INSTANCE.getLocalPlayer(), InteractionHand.MAIN_HAND, blockHitResult);
					Wrapper.INSTANCE.getLocalPlayer().swing(InteractionHand.MAIN_HAND);
					event.cancel();
				}
			}
	}, new MousePressFilter(EventMouseButton.ClickType.IN_GAME, 1));

	@EventPointer
	private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
		HitResult hitResult = Wrapper.INSTANCE.getLocalPlayer().pick(reach, Wrapper.INSTANCE.getMinecraft().getFrameTime(), false);
		if (hitResult instanceof BlockHitResult blockHitResult) {
			if (canReplaceBlock(WorldHelper.INSTANCE.getBlock(blockHitResult.getBlockPos()))) {
				Vec3 renderPos = Render3DHelper.INSTANCE.getRenderPosition(blockHitResult.getBlockPos());
				AABB box = new AABB(renderPos.x(), renderPos.y(), renderPos.z(), renderPos.x() + 1, renderPos.y() + 1, renderPos.z() + 1);
				Render3DHelper.INSTANCE.drawBoxOutline(event.getPoseStack(), box, Feature.getState(BlockOverlay.class) ? ColorHelper.INSTANCE.getClientColor() : 0xff000000);
			}
		}
	});

	private boolean canReplaceBlock(Block block) {
		return block == Blocks.AIR || (liquids && block instanceof LiquidBlock);
	}

}
