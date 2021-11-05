package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventBlockOutlineColor;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.event.world.EventClickBlock;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.block.AirBlock;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Change the block outline and have an overlay show your break progress")
public class BlockOverlay extends Feature {

    @Op(name = "Outline Color", isColor = true)
    public int outlineColor = new Color(0, 245, 255).getRGB();

    @Op(name = "Progress Overlay")
    public boolean progressOverlay = true;
    @OpChild(name = "Overlay Color", isColor = true, parent = "Progress Overlay")
    public int overlayColor = new Color(0, 245, 255).getRGB();
    @OpChild(name = "Progress Based Color", parent = "Overlay Color")
    public boolean progressColor = false;

    public BlockHitResult clickedBlock;

    @EventListener(events = {EventRender3D.class, EventBlockOutlineColor.class, EventClickBlock.class})
    private void runMethod(Event event) {
        if (event instanceof EventRender3D eventRender3D) {
            if (clickedBlock == null || WorldHelper.INSTANCE.getBlock(clickedBlock.getBlockPos()) instanceof AirBlock)
                return;
            Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(clickedBlock.getBlockPos());
            if (Wrapper.INSTANCE.getIInteractionManager().getBlockBreakProgress() > 0 && Wrapper.INSTANCE.getInteractionManager().isBreakingBlock()) {
                float breakProgress = Wrapper.INSTANCE.getIInteractionManager().getBlockBreakProgress() / 2;
                Box box = new Box(renderPos.x + 0.5 - breakProgress, renderPos.y + 0.5 - breakProgress, renderPos.z + 0.5 - breakProgress, renderPos.x + 0.5 + breakProgress, renderPos.y + 0.5 + breakProgress, renderPos.z + 0.5 + breakProgress);
                Render3DHelper.INSTANCE.drawBoxInside(eventRender3D.getMatrixStack(), box, progressColor ? getColor(1 - (breakProgress * 2)).getRGB() : overlayColor);
            }
        } else if (event instanceof EventBlockOutlineColor eventBlockOutlineColor) {
            eventBlockOutlineColor.setColor(outlineColor);
            eventBlockOutlineColor.cancel();
        } else if (event instanceof EventClickBlock eventClickBlock) {
            this.clickedBlock = new BlockHitResult(Vec3d.of(eventClickBlock.getBlockPos()), eventClickBlock.getFace(), eventClickBlock.getBlockPos(), false);
        }
    }
    public Color getColor(double power) {
        double H = power * 0.35; // Hue (note 0.4 = Green, see huge chart below)
        double S = 0.9; // Saturation
        double B = 0.9; // Brightness

        return Color.getHSBColor((float) H, (float) S, (float) B);
    }
}
