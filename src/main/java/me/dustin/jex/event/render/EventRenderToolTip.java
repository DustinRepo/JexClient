package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class EventRenderToolTip extends Event {

    private ItemStack itemStack;
    private final MatrixStack poseStack;
    private final Mode mode;
    private int x, y;
    private ToolTipData other;

    public EventRenderToolTip(MatrixStack poseStack, Mode mode, int x, int y, ItemStack itemStack) {
        this.itemStack = itemStack;
        this.poseStack = poseStack;
        this.mode = mode;
        this.x = x;
        this.y = y;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public Mode getMode() {
        return mode;
    }

    public MatrixStack getPoseStack() {
        return poseStack;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public ToolTipData getOther() {
        return other;
    }

    public void setOther(ToolTipData toolTipData) {
        this.other = toolTipData;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public enum Mode {
        PRE, POST
    }

    public static record ToolTipData(ItemStack itemStack, int x, int y){}
}
