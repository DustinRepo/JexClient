package me.dustin.jex.module.impl.render;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventBlockOutlineColor;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.minecraft.block.AirBlock;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

@ModClass(name = "BlockOverlay", category = ModCategory.VISUAL, description = "Change the block outline and have an overlay show your break progress")
public class BlockOverlay extends Module {

    @Op(name = "Outline Color", isColor = true)
    public int outlineColor = new Color(0, 245, 255).getRGB();

    @Op(name = "Progress Overlay")
    public boolean progressOverlay = true;
    @OpChild(name = "Overlay Color", isColor = true, parent = "Progress Overlay")
    public int overlayColor = new Color(0, 245, 255).getRGB();
    @OpChild(name = "Progress Based Color", parent = "Overlay Color")
    public boolean progressColor = false;

    @EventListener(events = {EventRender3D.class, EventBlockOutlineColor.class})
    private void runMethod(Event event) {
        if (event instanceof EventRender3D) {
            EventRender3D eventRender3D = (EventRender3D)event;
            HitResult result = Wrapper.INSTANCE.getLocalPlayer().raycast(Wrapper.INSTANCE.getInteractionManager().getReachDistance(), eventRender3D.getPartialTicks(), false);
            if (result instanceof BlockHitResult) {
                BlockHitResult blockHitResult = (BlockHitResult) result;
                if (WorldHelper.INSTANCE.getBlock(((BlockHitResult) result).getBlockPos()) instanceof AirBlock)
                    return;
                Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(blockHitResult.getBlockPos());
                if (Wrapper.INSTANCE.getInteractionManager().isBreakingBlock() && progressOverlay) {
                    float breakProgress = Wrapper.INSTANCE.getIInteractionManager().getBlockBreakProgress() / 2;
                    Box box = new Box(renderPos.x + 0.5 - breakProgress, renderPos.y + 0.5 - breakProgress, renderPos.z + 0.5 - breakProgress, renderPos.x + 0.5 + breakProgress, renderPos.y + 0.5 + breakProgress, renderPos.z + 0.5 + breakProgress);
                    Render3DHelper.INSTANCE.drawBoxInside(((EventRender3D) event).getMatrixStack(), box, progressColor ? getColor(1 - (breakProgress * 2)).getRGB() : overlayColor);
                }
            }
        } else if (event instanceof EventBlockOutlineColor) {
            EventBlockOutlineColor eventBlockOutlineColor = (EventBlockOutlineColor)event;
            eventBlockOutlineColor.setColor(outlineColor);
            eventBlockOutlineColor.cancel();
        }
    }
    public Color getColor(double power) {
        double H = power * 0.35; // Hue (note 0.4 = Green, see huge chart below)
        double S = 0.9; // Saturation
        double B = 0.9; // Brightness

        return Color.getHSBColor((float) H, (float) S, (float) B);
    }
}
