package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import me.dustin.jex.feature.option.annotate.Op;

@Feature.Manifest(category = Feature.Category.MISC, description = "Save your mending tools from breaking by putting them away automatically.")
public class MendingSaver extends Feature {

    @Op(name = "Notify")
    public boolean notify;
    @Op(name = "Item %", max = 30)
    public int itemPercent = 10;

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        for (int i = 0; i < 9; i++) {
            ItemStack currentStack = InventoryHelper.INSTANCE.getInventory().getStack(i);
            if (currentStack != null && InventoryHelper.INSTANCE.hasEnchantment(currentStack, Enchantments.MENDING)) {
                float percent = (((float) currentStack.getMaxDamage() - (float) currentStack.getDamage()) / (float) currentStack.getMaxDamage()) * 100;
                if (percent < itemPercent) {
                    if (notify)
                        ChatHelper.INSTANCE.addClientMessage("MendingSaver just saved your \247b" + currentStack.getName().getString());

                    if (!InventoryHelper.INSTANCE.isInventoryFullIgnoreHotbar())
                        InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, i + 36, SlotActionType.QUICK_MOVE);
                    else {
                        InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, i + 36, SlotActionType.SWAP, getFirstNonMendingSlot() == -1 ? 8 : getFirstNonMendingSlot());
                    }
                }
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

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
