package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;

public class EventRenderNametags extends Event {

    private final LivingEntity entity;
    private final MatrixStack poseStack;
    private final VertexConsumerProvider multiBufferSource;

    public EventRenderNametags(LivingEntity entity, MatrixStack poseStack, VertexConsumerProvider multiBufferSource) {
        this.entity = entity;
        this.poseStack = poseStack;
        this.multiBufferSource = multiBufferSource;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public MatrixStack getPoseStack() {
        return poseStack;
    }

    public VertexConsumerProvider getMultiBufferSource() {
        return multiBufferSource;
    }
}
