package me.dustin.jex.event.render;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.events.core.Event;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;

public class EventRenderNametags extends Event {

    private final LivingEntity entity;
    private final PoseStack poseStack;
    private final MultiBufferSource multiBufferSource;

    public EventRenderNametags(LivingEntity entity, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        this.entity = entity;
        this.poseStack = poseStack;
        this.multiBufferSource = multiBufferSource;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public MultiBufferSource getMultiBufferSource() {
        return multiBufferSource;
    }
}
