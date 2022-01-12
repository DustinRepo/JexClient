package me.dustin.jex.event.misc;

import me.dustin.events.core.Event;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class EventGetToolTipFromItem extends Event {

    private final ItemStack itemStack;
    private List<Text> textList;

    public EventGetToolTipFromItem(ItemStack itemStack, List<Text> textList) {
        this.itemStack = itemStack;
        this.textList = textList;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public List<Text> getTextList() {
        return textList;
    }

    public void setTextList(ArrayList<Text> textList) {
        this.textList = textList;
    }
}
