package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;

public class EventRotateItemEntity extends Event {

    private final ItemEntity itemEntity;
    private final MatrixStack poseStack;
    private final float g;

    public EventRotateItemEntity(ItemEntity itemEntity, MatrixStack poseStack, float g) {
        this.itemEntity = itemEntity;
        this.poseStack = poseStack;
        this.g = g;
    }

    public ItemEntity getItemEntity() {
        return itemEntity;
    }

    public MatrixStack getPoseStack() {
        return poseStack;
    }

    public float getG() {
        return g;
    }
}
