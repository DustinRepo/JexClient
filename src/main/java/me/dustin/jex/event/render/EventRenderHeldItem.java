package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class EventRenderHeldItem extends Event {

    private final ItemStack itemStack;
    private final Hand hand;
    private final float partialTicks;
    private final MatrixStack poseStack;

    public EventRenderHeldItem(ItemStack itemStack, Hand hand, float partialTicks, MatrixStack poseStack) {
        this.itemStack = itemStack;
        this.hand = hand;
        this.partialTicks = partialTicks;
        this.poseStack = poseStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public Hand getHand() {
        return hand;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public MatrixStack getPoseStack() {
        return poseStack;
    }
}
