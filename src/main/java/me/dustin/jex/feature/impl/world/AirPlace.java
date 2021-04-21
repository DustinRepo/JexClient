package me.dustin.jex.feature.impl.world;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventMouseButton;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.feature.impl.render.BlockOverlay;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.block.Blocks;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

@Feat(name = "AirPlace", category = FeatureCategory.WORLD, description = "Gives you the ability to place blocks in the air. (Anticheats usually block this)")
public class AirPlace extends Feature {

    @Op(name = "Reach", min = 3, max = 6, inc = 0.1f)
    public float reach = 4.5f;

    @EventListener(events = {EventMouseButton.class, EventRender3D.class})
    private void runMethod(Event event) {
        if (event instanceof EventMouseButton) {
            EventMouseButton eventMouseButton = (EventMouseButton)event;
            if (eventMouseButton.getButton() == 1 && eventMouseButton.getClickType() == EventMouseButton.ClickType.IN_GAME && Wrapper.INSTANCE.getMinecraft().crosshairTarget == null) {
                HitResult hitResult = Wrapper.INSTANCE.getLocalPlayer().raycast(reach, Wrapper.INSTANCE.getMinecraft().getTickDelta(), false);
                if (hitResult instanceof BlockHitResult) {
                    BlockHitResult blockHitResult = (BlockHitResult)hitResult;
                    if (WorldHelper.INSTANCE.getBlock(blockHitResult.getBlockPos()) == Blocks.AIR) {
                        Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, blockHitResult);
                        Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                        event.cancel();
                    }
                }
            }
        } else if (event instanceof EventRender3D) {
            HitResult hitResult = Wrapper.INSTANCE.getLocalPlayer().raycast(reach, Wrapper.INSTANCE.getMinecraft().getTickDelta(), false);
            if (hitResult instanceof BlockHitResult && Wrapper.INSTANCE.getMinecraft().crosshairTarget == null) {
                BlockHitResult blockHitResult = (BlockHitResult)hitResult;
                if (WorldHelper.INSTANCE.getBlock(blockHitResult.getBlockPos()) == Blocks.AIR) {
                    Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(blockHitResult.getBlockPos());
                    Box box = new Box(renderPos.getX(), renderPos.getY(), renderPos.getZ(), renderPos.getX() + 1, renderPos.getY() + 1, renderPos.getZ() + 1);
                    Render3DHelper.INSTANCE.drawBoxOutline(((EventRender3D) event).getMatrixStack(), box, Feature.get(BlockOverlay.class).getState() ? ColorHelper.INSTANCE.getClientColor() : 0xff000000);
                }
            }
        }
    }

}
