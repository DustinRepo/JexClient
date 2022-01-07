package me.dustin.jex.event.render;
/*
 * @Author Dustin
 * 9/29/2019
 */

import me.dustin.events.core.Event;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class EventRenderToolTip extends Event {

    private ItemStack itemStack;
    private final MatrixStack matrixStack;
    private final Mode mode;

    public EventRenderToolTip(MatrixStack matrixStack, Mode mode, ItemStack itemStack) {
        this.itemStack = itemStack;
        this.matrixStack = matrixStack;
        this.mode = mode;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public Mode getMode() {
        return mode;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public enum Mode {
        PRE, POST
    }
}
