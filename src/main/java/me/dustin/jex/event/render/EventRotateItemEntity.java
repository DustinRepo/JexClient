package me.dustin.jex.event.render;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.events.core.Event;
import net.minecraft.world.entity.item.ItemEntity;

public class EventRotateItemEntity extends Event {

    private final ItemEntity itemEntity;
    private final PoseStack poseStack;
    private final float g;

    public EventRotateItemEntity(ItemEntity itemEntity, PoseStack poseStack, float g) {
        this.itemEntity = itemEntity;
        this.poseStack = poseStack;
        this.g = g;
    }

    public ItemEntity getItemEntity() {
        return itemEntity;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public float getG() {
        return g;
    }
}
