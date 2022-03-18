package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class EventRenderHeldItem extends Event {

    private ItemStack itemStack;
    private Hand hand;
    private float partialTicks;
    private MatrixStack matrixStack;

    public EventRenderHeldItem(ItemStack itemStack, Hand hand, float partialTicks, MatrixStack matrixStack) {
        this.itemStack = itemStack;
        this.hand = hand;
        this.partialTicks = partialTicks;
        this.matrixStack = matrixStack;
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

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }
}
