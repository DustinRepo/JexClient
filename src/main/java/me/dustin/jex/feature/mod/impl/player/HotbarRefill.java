package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.misc.EventItemStackDecrement;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Feature.Manifest(category = Feature.Category.PLAYER, description = "Attempts to refill your hotbar when you use up an item with the same item")
public class HotbarRefill extends Feature {
    private int hotbarSlot = -1;
    private int swapSlot = -1;
    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (swapSlot != -1) {
            InventoryHelper.INSTANCE.swapToHotbar(swapSlot, hotbarSlot);
            swapSlot = -1;
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventItemStackDecrement> eventItemStackDecrement = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getMinecraft().screen instanceof AbstractContainerScreen<?>)
            return;
        ItemStack stack = event.getItemStack();
        int slot = InventoryHelper.INSTANCE.getInventory().findSlotMatchingItem(stack);
        if (slot != InventoryHelper.INSTANCE.getInventory().selected)
            return;
        if (stack.getCount() <= event.getAmount()) {
            swapSlot = getSlotForItemExcluding(stack.getItem(), slot);
            hotbarSlot = slot;
        }
    });

    private int getSlotForItemExcluding(Item item, int excludeSlot) {
        for (int i = 0; i < 45; i++) {
            if (i == excludeSlot)
                continue;
            if (InventoryHelper.INSTANCE.getInventory().getItem(i) != null && InventoryHelper.INSTANCE.getInventory().getItem(i).getItem() == item)
                return i;
        }
        return -1;
    }
}
