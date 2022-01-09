package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ToolTipFilter;
import me.dustin.jex.event.render.EventRenderToolTip;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.util.Identifier;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "View maps just by hovering over them in your inventory")
public class MapToolTip extends Feature {

    private final static Identifier MAP_BACKGROUND = new Identifier("textures/map/map_background_checkerboard.png");

    @EventPointer
    private final EventListener<EventRenderToolTip> eventRenderToolTipEventListener = new EventListener<>(event -> {
        drawMap(event.getMatrixStack(), event.getX() + 8, event.getY() - 165, event.getItemStack());
    }, new ToolTipFilter(EventRenderToolTip.Mode.POST, Items.FILLED_MAP));

    public void drawMap(MatrixStack matrixStack, int x, int y, ItemStack stack) {
        MapState mapState = FilledMapItem.getOrCreateMapState(stack, Wrapper.INSTANCE.getWorld());
        if (mapState != null) {
            Render2DHelper.INSTANCE.bindTexture(MAP_BACKGROUND);
            DrawableHelper.drawTexture(matrixStack, x, y, 0, 0, 150, 150, 150, 150);

            matrixStack.push();
            matrixStack.translate(x + 11, y + 11, 1000);
            VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
            Wrapper.INSTANCE.getMinecraft().gameRenderer.getMapRenderer().draw(matrixStack, immediate, FilledMapItem.getMapId(stack), mapState, false, 15728880);
            immediate.draw();
            matrixStack.pop();
        }
    }

}
