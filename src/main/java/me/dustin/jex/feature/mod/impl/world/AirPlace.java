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
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.BlockOverlay;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Gives you the ability to place blocks in the air. (Anticheats usually block this)")
public class AirPlace extends Feature {

	@Op(name = "Liquids")
	public boolean liquids = true;
	@Op(name = "Reach", min = 3, max = 6, inc = 0.1f)
	public float reach = 4.5f;

	@EventPointer
	private final EventListener<EventMouseButton> eventMouseButtonEventListener = new EventListener<>(event -> {
			HitResult hitResult = Wrapper.INSTANCE.getLocalPlayer().raycast(reach, Wrapper.INSTANCE.getMinecraft().getTickDelta(), false);
			if (hitResult instanceof BlockHitResult blockHitResult) {
				if (canReplaceBlock(WorldHelper.INSTANCE.getBlock(blockHitResult.getBlockPos())) && Wrapper.INSTANCE.getLocalPlayer().getMainHandStack().getItem() instanceof BlockItem) {
					Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, blockHitResult);
					Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
					event.cancel();
				}
			}
	}, new MousePressFilter(EventMouseButton.ClickType.IN_GAME, 1));

	@EventPointer
	private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
		HitResult hitResult = Wrapper.INSTANCE.getLocalPlayer().raycast(reach, Wrapper.INSTANCE.getMinecraft().getTickDelta(), false);
		if (hitResult instanceof BlockHitResult blockHitResult) {
			if (canReplaceBlock(WorldHelper.INSTANCE.getBlock(blockHitResult.getBlockPos()))) {
				Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(blockHitResult.getBlockPos());
				Box box = new Box(renderPos.getX(), renderPos.getY(), renderPos.getZ(), renderPos.getX() + 1, renderPos.getY() + 1, renderPos.getZ() + 1);
				Render3DHelper.INSTANCE.drawBoxOutline(event.getMatrixStack(), box, Feature.getState(BlockOverlay.class) ? ColorHelper.INSTANCE.getClientColor() : 0xff000000);
			}
		}
	});

	private boolean canReplaceBlock(Block block) {
		return block == Blocks.AIR || (liquids && block instanceof FluidBlock);
	}

}
