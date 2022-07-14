package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class EventWorldRenderEntity extends Event {
    private final Entity entity;
    private final MatrixStack matrixStack;
    private final VertexConsumerProvider vertexConsumerProvider;
    private final float tickDelta;

    public EventWorldRenderEntity(Entity entity, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, float tickDelta) {
        this.entity = entity;
        this.matrixStack = matrixStack;
        this.vertexConsumerProvider = vertexConsumerProvider;
        this.tickDelta = tickDelta;
    }

    public Entity getEntity() {
        return entity;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public VertexConsumerProvider getVertexConsumerProvider() {
        return vertexConsumerProvider;
    }

    public float getTickDelta() {
        return tickDelta;
    }
}
