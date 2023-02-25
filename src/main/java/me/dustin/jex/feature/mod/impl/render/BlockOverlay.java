package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClickBlockFilter;
import me.dustin.jex.event.render.EventBlockOutlineColor;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.event.world.EventClickBlock;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.AirBlock;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import me.dustin.jex.feature.mod.core.Feature;
import java.awt.*;

public class BlockOverlay extends Feature {

    public final Property<Color> outlineColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Outline Color")
            .value(new Color(0, 245, 255))
            .build();
    public final Property<Float> sizeProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Outline size")
            .value(0.5f)
            .min(0.1f)
            .max(0.5f)
            .inc(0.01f)
            .build();
    public final Property<Boolean> progressOverlayProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Progress Overlay")
            .description("Show block breaking progress.")
            .value(true)
            .build();
    public final Property<Color> overlayColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Overlay Color")
            .value(new Color(0, 245, 255))
            .parent(progressOverlayProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> progressColorProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Progress Based Color")
            .description("Sets the color from green to red based on break progress")
            .value(true)
            .parent(overlayColorProperty)
            .build();

    public BlockHitResult clickedBlock;

    public BlockOverlay() {
        super(Category.VISUAL, "Change the block outline and have an overlay show your break progress");
    }

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        if (clickedBlock == null || WorldHelper.INSTANCE.getBlock(clickedBlock.getBlockPos()) instanceof AirBlock)
            return;
        Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(clickedBlock.getBlockPos());
        if (progressOverlayProperty.value() && Wrapper.INSTANCE.getIClientPlayerInteractionManager().getBlockBreakProgress() > 0 && Wrapper.INSTANCE.getClientPlayerInteractionManager().isBreakingBlock()) {
            float breakProgress = Wrapper.INSTANCE.getIClientPlayerInteractionManager().getBlockBreakProgress() / 2;
            Box box = new Box(renderPos.x + sizeProperty.value() - breakProgress, renderPos.y + sizeProperty.value() - breakProgress, renderPos.z + sizeProperty.value() - breakProgress, renderPos.x + sizeProperty.value() + breakProgress, renderPos.y + sizeProperty.value() + breakProgress, renderPos.z + sizeProperty.value() + breakProgress);
            Render3DHelper.INSTANCE.drawBoxInside(event.getPoseStack(), box, progressColorProperty.value() ? ColorHelper.INSTANCE.redGreenShift(1 - (breakProgress * 2)) : overlayColorProperty.value().getRGB());
        }
    });

    @EventPointer
    private final EventListener<EventBlockOutlineColor> eventBlockOutlineColorEventListener = new EventListener<>(event -> {
        event.setColor(outlineColorProperty.value().getRGB());
        event.cancel();
    });

    @EventPointer
    private final EventListener<EventClickBlock> eventClickBlockEventListener = new EventListener<>(event -> {
        this.clickedBlock = new BlockHitResult(Vec3d.of(event.getBlockPos()), event.getFace(), event.getBlockPos(), false);
    }, new ClickBlockFilter(EventClickBlock.Mode.PRE));

}
