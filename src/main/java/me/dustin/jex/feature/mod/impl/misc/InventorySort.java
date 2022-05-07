package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Feature.Manifest(category = Feature.Category.MISC, description = "Sort your inventory with a middle click while it's open.")
public class InventorySort extends Feature {

    @Op(name = "Sort Key", isKeybind = true)
    public int sortKey = KeyboardHelper.INSTANCE.MIDDLE_CLICK;

    private StopWatch timeOutStopWatch = new StopWatch();

    @EventPointer
    private final EventListener<EventKeyPressed> eventKeyPressedEventListener = new EventListener<>(event -> {
        if (event.getKey() != sortKey)
            return;
        if (Wrapper.INSTANCE.getLocalPlayer() != null && Wrapper.INSTANCE.getMinecraft().screen instanceof AbstractContainerScreen<?> handledScreen) {
            AbstractContainerMenu screenHandler = handledScreen.getMenu();
            int emptySlot = getFirstEmptySlot(screenHandler);
            int nonEmptySlot = getLastNonEmptySlot(screenHandler);
            timeOutStopWatch.reset();
            while (emptySlot != -1 && nonEmptySlot != -1 && emptySlot < nonEmptySlot) {
                InventoryHelper.INSTANCE.windowClick(screenHandler, screenHandler instanceof InventoryMenu ? (nonEmptySlot < 9 ? nonEmptySlot + 36 : nonEmptySlot) : nonEmptySlot, ClickType.PICKUP);
                InventoryHelper.INSTANCE.windowClick(screenHandler, screenHandler instanceof InventoryMenu ? (emptySlot < 9 ? emptySlot + 36 : emptySlot) :  emptySlot, ClickType.PICKUP);

                emptySlot = getFirstEmptySlot(screenHandler);
                nonEmptySlot = getLastNonEmptySlot(screenHandler);

                if (timeOutStopWatch.hasPassed(1000)) {//if this takes longer than a second it borked up
                    ChatHelper.INSTANCE.addClientMessage("MiddleClickSort timeout - something went wrong.");
                    ChatHelper.INSTANCE.addClientMessage("If you would like to report this, please take a screenshot of the inventory and send it in the Discord while also saying what screen you are in.");
                    break;
                }
            }
        }
    });

    int getLastNonEmptySlot(AbstractContainerMenu screenHandler) {
        int s = -1;
        if (screenHandler instanceof InventoryMenu) {
            for (int i = 9; i < 36; i++) {
                if (InventoryHelper.INSTANCE.getInventory().getItem(i) != null && InventoryHelper.INSTANCE.getInventory().getItem(i).getItem() != Items.AIR)
                    s = i;
            }
            return s;
        }
        int most = Wrapper.INSTANCE.getLocalPlayer().containerMenu.slots.size() - 36;

        for (int i = 0; i < most; i++) {
            ItemStack stack = screenHandler.getSlot(i).getItem();
            if (stack != null && stack.getItem() != Items.AIR) {
                s = i;
            }
        }
        return s;
    }

    int getFirstEmptySlot(AbstractContainerMenu screenHandler) {
        if (screenHandler instanceof InventoryMenu) {
            for (int i = 9; i < 36; i++) {
                if (InventoryHelper.INSTANCE.getInventory().getItem(i) == null || InventoryHelper.INSTANCE.getInventory().getItem(i).getItem() == Items.AIR)
                    return i;
            }
            return -1;
        }
        int most = Wrapper.INSTANCE.getLocalPlayer().containerMenu.slots.size() - 36;
        for (int i = 0; i < most; i++) {
            ItemStack stack = screenHandler.getSlot(i).getItem();
            if (stack == null || stack.getItem() == Items.AIR) {
                return i;
            }
        }
        return -1;
    }

    int getInvSlot(Slot slot) {
        for (int i = 0; i < Wrapper.INSTANCE.getLocalPlayer().containerMenu.slots.size(); i++) {
            Slot testSlot = Wrapper.INSTANCE.getLocalPlayer().containerMenu.getSlot(i);
            if (slot == testSlot)
                return i;
        }
        return -1;
    }
}
