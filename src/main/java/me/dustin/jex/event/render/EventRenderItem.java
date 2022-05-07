package me.dustin.jex.event.render;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.events.core.Event;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;

public class EventRenderItem extends Event {

    private final PoseStack poseStack;
    private final ItemStack itemStack;
    private final ItemTransforms.TransformType type;
    private final RenderTime renderTime;
    private final boolean leftHanded;

    public EventRenderItem(PoseStack poseStack, ItemStack itemStack, ItemTransforms.TransformType type, RenderTime renderTime, boolean leftHanded)
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

    public ItemTransforms.TransformType getType() {
        return type;
    }

    public RenderTime getRenderTime() {
        return renderTime;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public boolean isLeftHanded() {
        return leftHanded;
    }

    public enum RenderTime {
        PRE, POST
    }
}
