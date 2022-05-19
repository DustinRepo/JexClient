package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClickBlockFilter;
import me.dustin.jex.event.render.EventBlockOutlineColor;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.event.world.EventClickBlock;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.AirBlock;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import java.awt.*;

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

    public BlockOverlay() {
        super(Category.VISUAL, "Change the block outline and have an overlay show your break progress");
    }

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        if (clickedBlock == null || WorldHelper.INSTANCE.getBlock(clickedBlock.getBlockPos()) instanceof AirBlock)
            return;
        Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(clickedBlock.getBlockPos());
        if (progressOverlay && Wrapper.INSTANCE.getIMultiPlayerGameMode().getBlockBreakProgress() > 0 && Wrapper.INSTANCE.getMultiPlayerGameMode().isBreakingBlock()) {
            float breakProgress = Wrapper.INSTANCE.getIMultiPlayerGameMode().getBlockBreakProgress() / 2;
            Box box = new Box(renderPos.x + 0.5 - breakProgress, renderPos.y + 0.5 - breakProgress, renderPos.z + 0.5 - breakProgress, renderPos.x + 0.5 + breakProgress, renderPos.y + 0.5 + breakProgress, renderPos.z + 0.5 + breakProgress);
            Render3DHelper.INSTANCE.drawBoxInside(event.getPoseStack(), box, progressColor ? getColor(1 - (breakProgress * 2)).getRGB() : overlayColor);
        }
    });

    @EventPointer
    private final EventListener<EventBlockOutlineColor> eventBlockOutlineColorEventListener = new EventListener<>(event -> {
        event.setColor(outlineColor);
        event.cancel();
    });

    @EventPointer
    private final EventListener<EventClickBlock> eventClickBlockEventListener = new EventListener<>(event -> {
        this.clickedBlock = new BlockHitResult(Vec3d.of(event.getBlockPos()), event.getFace(), event.getBlockPos(), false);
    }, new ClickBlockFilter(EventClickBlock.Mode.PRE));

    public Color getColor(double power) {
        double H = power * 0.35; // Hue (note 0.4 = Green, see huge chart below)
        double S = 0.9; // Saturation
        double B = 0.9; // Brightness

        return Color.getHSBColor((float) H, (float) S, (float) B);
    }
}
