package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

@Feature.Manifest(name = "MendingSaver", category = Feature.Category.MISC, description = "Save your mending tools from breaking by putting them away automatically.")
public class MendingSaver extends Feature {

    @Op(name = "Notify")
    public boolean notify;
    @Op(name = "Item %", max = 30)
    public int itemPercent = 10;

    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            ItemStack currentStack = Wrapper.INSTANCE.getLocalPlayer().getMainHandStack();
            if (currentStack != null && InventoryHelper.INSTANCE.hasEnchantment(currentStack, Enchantments.MENDING)) {
                float percent = (((float) currentStack.getMaxDamage() - (float) currentStack.getDamage()) / (float) currentStack.getMaxDamage()) * 100;
                if (percent < itemPercent) {
                    if (notify)
                        ChatHelper.INSTANCE.addClientMessage("MendingSaver just saved your \247b" + currentStack.getName().getString());

                    if (!InventoryHelper.INSTANCE.isInventoryFullIgnoreHotbar())
                        InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, InventoryHelper.INSTANCE.getInventory().selectedSlot + 36, SlotActionType.QUICK_MOVE);
                    else {
                        InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, InventoryHelper.INSTANCE.getInventory().selectedSlot + 36, SlotActionType.SWAP, getFirstNonMendingSlot() == -1 ? 8 : getFirstNonMendingSlot());
                    }
                }
            }
        }
    }

    private int getFirstNonMendingSlot() {
        for (int i = 9; i < 36; i++) {
            ItemStack itemStack = InventoryHelper.INSTANCE.getInventory().getStack(i);
            if (!InventoryHelper.INSTANCE.hasEnchantment(itemStack, Enchantments.MENDING)) {
                return i;
            }
        }
        return -1;
    }

}
