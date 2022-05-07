package me.dustin.jex.event.render;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.events.core.Event;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class EventRenderHeldItem extends Event {

    private final ItemStack itemStack;
    private final InteractionHand hand;
    private final float partialTicks;
    private final PoseStack poseStack;

    public EventRenderHeldItem(ItemStack itemStack, InteractionHand hand, float partialTicks, PoseStack poseStack) {
        this.itemStack = itemStack;
        this.hand = hand;
        this.partialTicks = partialTicks;
        this.poseStack = poseStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }
}
