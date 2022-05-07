package me.dustin.jex.event.misc;

import me.dustin.events.core.Event;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class EventGetToolTipFromItem extends Event {

    private final ItemStack itemStack;
    private List<Component> textList;

    public EventGetToolTipFromItem(ItemStack itemStack, List<Component> textList) {
        this.itemStack = itemStack;
        this.textList = textList;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public List<Component> getTextList() {
        return textList;
    }

    public void setTextList(ArrayList<Component> textList) {
        this.textList = textList;
    }
}
