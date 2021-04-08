package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class EventRenderItem extends Event {

    private MatrixStack matrixStack;
    private ItemStack itemStack;
    private ModelTransformation.Mode type;
    private RenderTime renderTime;

    public EventRenderItem(MatrixStack matrixStack, ItemStack itemStack, ModelTransformation.Mode type, RenderTime renderTime)
    {
        this.matrixStack = matrixStack;
        this.itemStack = itemStack;
        this.type = type;
        this.renderTime = renderTime;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ModelTransformation.Mode getType() {
        return type;
    }

    public RenderTime getRenderTime() {
        return renderTime;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public enum RenderTime {
        PRE, POST
    }
}
