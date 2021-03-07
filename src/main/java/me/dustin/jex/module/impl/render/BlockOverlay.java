package me.dustin.jex.module.impl.render;

import me.dustin.events.core.annotate.EventListener;
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

    @EventListener(events = {EventRender3D.class})
    private void runMethod(EventRender3D eventRender3D) {
        HitResult result = Wrapper.INSTANCE.getLocalPlayer().raycast(Wrapper.INSTANCE.getInteractionManager().getReachDistance(), eventRender3D.getPartialTicks(), false);
        if (result instanceof BlockHitResult) {
            BlockHitResult blockHitResult = (BlockHitResult)result;
            if (WorldHelper.INSTANCE.getBlock(((BlockHitResult) result).getBlockPos()) instanceof AirBlock)
                return;
            Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(blockHitResult.getBlockPos());
            Box box = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
            Render3DHelper.INSTANCE.drawBoxOutline(box, outlineColor);

            if (Wrapper.INSTANCE.getInteractionManager().isBreakingBlock() && progressOverlay) {
                float breakProgress = Wrapper.INSTANCE.getIInteractionManager().getBlockBreakProgress() / 2;
                box = new Box(renderPos.x + 0.5 - breakProgress, renderPos.y + 0.5 - breakProgress, renderPos.z + 0.5 - breakProgress, renderPos.x + 0.5 + breakProgress, renderPos.y + 0.5 + breakProgress, renderPos.z + 0.5 + breakProgress);
                Render3DHelper.INSTANCE.drawBoxInside(box, progressColor ? getColor(breakProgress * 2).getRGB() : overlayColor);
            }
        }
    }
    public Color getColor(double power) {
        double H = power * 0.4; // Hue (note 0.4 = Green, see huge chart below)
        double S = 0.9; // Saturation
        double B = 0.9; // Brightness

        return Color.getHSBColor((float) H, (float) S, (float) B);
    }
}
