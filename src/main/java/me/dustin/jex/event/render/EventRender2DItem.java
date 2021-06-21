package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;

public class EventRender2DItem extends Event {
    private final ItemRenderer itemRenderer;
    private final TextRenderer fontRenderer;
    private final ItemStack stack;
    private final int x, y;

    public EventRender2DItem(ItemRenderer itemRenderer, TextRenderer fontRenderer, ItemStack stack, int x, int y) {
        this.itemRenderer = itemRenderer;
        this.fontRenderer = fontRenderer;
        this.stack = stack;
        this.x = x;
        this.y = y;
    }

    public ItemRenderer getItemRenderer() {
        return this.itemRenderer;
    }

    public final TextRenderer getFontRenderer() {
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
