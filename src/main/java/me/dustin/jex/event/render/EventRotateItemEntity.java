package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;

public class EventRotateItemEntity extends Event {

    private ItemEntity itemEntity;
    private MatrixStack matrixStack;
    private float g;

    public EventRotateItemEntity(ItemEntity itemEntity, MatrixStack matrixStack, float g) {
        this.itemEntity = itemEntity;
        this.matrixStack = matrixStack;
        this.g = g;
    }

    public ItemEntity getItemEntity() {
        return itemEntity;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public float getG() {
        return g;
    }
}
