package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;

public class EventRender2DItem extends Event {
    private final ItemRenderer itemRenderer;
    private final Font fontRenderer;
    private final ItemStack stack;
    private final int x, y;

    public EventRender2DItem(ItemRenderer itemRenderer, Font fontRenderer, ItemStack stack, int x, int y) {
        this.itemRenderer = itemRenderer;
        this.fontRenderer = fontRenderer;
        this.stack = stack;
        this.x = x;
        this.y = y;
    }

    public ItemRenderer getItemRenderer() {
        return this.itemRenderer;
    }

    public final Font getFontRenderer() {
        return this.fontRenderer;
    }

    public final ItemStack getStack() {
        return this.stack;
    }

    public final int getX() {
        return this.x;
    }

    public final int getY() {
        return  this.y;
    }
}
