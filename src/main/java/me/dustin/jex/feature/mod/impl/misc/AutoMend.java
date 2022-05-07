package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

@Feature.Manifest(category = Feature.Category.MISC, description = "Automatically hold a mending item in your offhand until it has full durability")
public class AutoMend extends Feature {

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        ItemStack offhand = Wrapper.INSTANCE.getLocalPlayer().getOffhandItem();
        if (InventoryHelper.INSTANCE.hasEnchantment(offhand, Enchantments.MENDING) && offhand.isDamaged())
            return;
        int mendingItem = getMendingItem();
        if (mendingItem == -1)
            return;
        InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().containerMenu, mendingItem < 9 ? mendingItem + 36 : mendingItem, ClickType.PICKUP);
        InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().containerMenu, 45, ClickType.PICKUP);
        if (!(offhand.getItem() instanceof AirItem))
            InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().containerMenu, mendingItem < 9 ? mendingItem + 36 : mendingItem, ClickType.PICKUP);
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    private int getMendingItem() {
        for (int i = 0; i < 36; i++) {
            ItemStack itemStack = InventoryHelper.INSTANCE.getInventory().getItem(i);
            if (InventoryHelper.INSTANCE.hasEnchantment(itemStack, Enchantments.MENDING) && itemStack.isDamaged() && itemStack != Wrapper.INSTANCE.getLocalPlayer().getMainHandItem())
                return i;
        }
        return -1;
    }

}
