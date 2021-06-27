package me.dustin.jex.feature.impl.misc;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

@Feat(name = "AutoMend", category = FeatureCategory.MISC, description = "Automatically hold a mending item in your offhand until it has full durability")
public class AutoMend extends Feature {

    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            ItemStack offhand = Wrapper.INSTANCE.getLocalPlayer().getOffHandStack();
            if (InventoryHelper.INSTANCE.hasEnchantment(offhand, Enchantments.MENDING) && offhand.isDamaged())
                return;
            int mendingItem = getMendingItem();
            if (mendingItem == -1)
                return;
            InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, mendingItem < 9 ? mendingItem + 36 : mendingItem, SlotActionType.PICKUP);
            InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, 45, SlotActionType.PICKUP);
            if (!(offhand.getItem() instanceof AirBlockItem))
                InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, mendingItem < 9 ? mendingItem + 36 : mendingItem, SlotActionType.PICKUP);
        }
    }

    private int getMendingItem() {
        for (int i = 0; i < 36; i++) {
            ItemStack itemStack = InventoryHelper.INSTANCE.getInventory().getStack(i);
            if (InventoryHelper.INSTANCE.hasEnchantment(itemStack, Enchantments.MENDING) && itemStack.isDamaged() && itemStack != Wrapper.INSTANCE.getLocalPlayer().getMainHandStack())
                return i;
        }
        return -1;
    }

}
