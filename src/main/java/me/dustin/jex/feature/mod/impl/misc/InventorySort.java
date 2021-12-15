package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

@Feature.Manifest(category = Feature.Category.MISC, description = "Sort your inventory with a middle click while it's open.")
public class InventorySort extends Feature {

    @Op(name = "Sort Key", isKeybind = true)
    public int sortKey = KeyboardHelper.INSTANCE.MIDDLE_CLICK;

    private Timer timeOutTimer = new Timer();

    @EventPointer
    private final EventListener<EventKeyPressed> eventKeyPressedEventListener = new EventListener<>(event -> {
        if (event.getKey() != sortKey)
            return;
        if (Wrapper.INSTANCE.getLocalPlayer() != null && Wrapper.INSTANCE.getMinecraft().currentScreen instanceof HandledScreen<?> handledScreen) {
            ScreenHandler screenHandler = handledScreen.getScreenHandler();
            int emptySlot = getFirstEmptySlot(screenHandler);
            int nonEmptySlot = getLastNonEmptySlot(screenHandler);
            timeOutTimer.reset();
            while (emptySlot != -1 && nonEmptySlot != -1 && emptySlot < nonEmptySlot) {
                InventoryHelper.INSTANCE.windowClick(screenHandler, screenHandler instanceof PlayerScreenHandler ? (nonEmptySlot < 9 ? nonEmptySlot + 36 : nonEmptySlot) : nonEmptySlot, SlotActionType.PICKUP);
                InventoryHelper.INSTANCE.windowClick(screenHandler, screenHandler instanceof PlayerScreenHandler ? (emptySlot < 9 ? emptySlot + 36 : emptySlot) :  emptySlot, SlotActionType.PICKUP);

                emptySlot = getFirstEmptySlot(screenHandler);
                nonEmptySlot = getLastNonEmptySlot(screenHandler);

                if (timeOutTimer.hasPassed(1000)) {//if this takes longer than a second it borked up
                    ChatHelper.INSTANCE.addClientMessage("MiddleClickSort timeout - something went wrong.");
                    ChatHelper.INSTANCE.addClientMessage("If you would like to report this, please take a screenshot of the inventory and send it in the Discord while also saying what screen you are in.");
                    break;
                }
            }
        }
    });

    int getLastNonEmptySlot(ScreenHandler screenHandler) {
        int s = -1;
        if (screenHandler instanceof PlayerScreenHandler) {
            for (int i = 9; i < 36; i++) {
                if (InventoryHelper.INSTANCE.getInventory().getStack(i) != null && InventoryHelper.INSTANCE.getInventory().getStack(i).getItem() != Items.AIR)
                    s = i;
            }
            return s;
        }
        int most = Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler.slots.size() - 36;

        for (int i = 0; i < most; i++) {
            ItemStack stack = screenHandler.getSlot(i).getStack();
            if (stack != null && stack.getItem() != Items.AIR) {
                s = i;
            }
        }
        return s;
    }

    int getFirstEmptySlot(ScreenHandler screenHandler) {
        if (screenHandler instanceof PlayerScreenHandler) {
            for (int i = 9; i < 36; i++) {
                if (InventoryHelper.INSTANCE.getInventory().getStack(i) == null || InventoryHelper.INSTANCE.getInventory().getStack(i).getItem() == Items.AIR)
                    return i;
            }
            return -1;
        }
        int most = Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler.slots.size() - 36;
        for (int i = 0; i < most; i++) {
            ItemStack stack = screenHandler.getSlot(i).getStack();
            if (stack == null || stack.getItem() == Items.AIR) {
                return i;
            }
        }
        return -1;
    }

    int getInvSlot(Slot slot) {
        for (int i = 0; i < Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler.slots.size(); i++) {
            Slot testSlot = Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler.getSlot(i);
            if (slot == testSlot)
                return i;
        }
        return -1;
    }
}
