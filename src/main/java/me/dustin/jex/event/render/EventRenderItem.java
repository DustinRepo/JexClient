package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class EventRenderItem extends Event {

    private final MatrixStack poseStack;
    private final ItemStack itemStack;
    private final ModelTransformation.Mode type;
    private final RenderTime renderTime;
    private final boolean leftHanded;

    public EventRenderItem(MatrixStack poseStack, ItemStack itemStack, ModelTransformation.Mode type, RenderTime renderTime, boolean leftHanded)
    {
        this.poseStack = poseStack;
        this.itemStack = itemStack;
        this.type = type;
        this.renderTime = renderTime;
        this.leftHanded = leftHanded;
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

    public MatrixStack getPoseStack() {
        return poseStack;
    }

    public boolean isLeftHanded() {
        return leftHanded;
    }

    public enum RenderTime {
        PRE, POST
    }
}
