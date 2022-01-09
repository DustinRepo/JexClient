package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ToolTipFilter;
import me.dustin.jex.event.render.EventRenderToolTip;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.render.Render2DHelper;
import net.minecraft.item.Items;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "View maps just by hovering over them in your inventory")
public class MapToolTip extends Feature {
    @EventPointer
    private final EventListener<EventRenderToolTip> eventRenderToolTipEventListener = new EventListener<>(event -> {
        Render2DHelper.INSTANCE.drawMap(event.getMatrixStack(), event.getX() + 8, event.getY() - 165, event.getItemStack());
    }, new ToolTipFilter(EventRenderToolTip.Mode.POST, Items.FILLED_MAP));
}
