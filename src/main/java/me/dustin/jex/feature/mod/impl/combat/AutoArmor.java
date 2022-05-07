package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.events.core.annotate.EventPointer;

@Feature.Manifest(category = Feature.Category.COMBAT, description = "Puts on the best armor in your inventory automatically.")
public class AutoArmor extends Feature {

    @Op(name = "Delay (MS)", max = 1000)
    public int delay = 65;

    private StopWatch stopWatch = new StopWatch();

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (stopWatch.hasPassed(delay)) {
            int stackToMove = -1;
            ArmorItem equipped = null;
            if (Wrapper.INSTANCE.getMinecraft().screen instanceof AbstractContainerScreen || Wrapper.INSTANCE.getMinecraft().screen instanceof InventoryScreen || Wrapper.INSTANCE.getMinecraft().screen instanceof MerchantScreen)
                return;
            int armorSlot = 0;
            for (; armorSlot < 4; armorSlot++) {
                int bestItem = -1;
                ItemStack equippedStack = InventoryHelper.INSTANCE.getInventory().getItem(36 + armorSlot);
                if (equippedStack.getItem() == Items.ELYTRA)
                    continue;
                for (int i = 0; i < 36; i++) {
                    if (bestItem != -1)
                        equippedStack = InventoryHelper.INSTANCE.getInventory().getItem(bestItem);
                    ItemStack itemStack = InventoryHelper.INSTANCE.getInventory().getItem(i);
                    if (itemStack != null && itemStack.getItem() instanceof ArmorItem armorItem) {
                        if (equippedStack.getItem() instanceof AirItem) {
                            if (armorItem.getSlot().getType() != EquipmentSlot.Type.HAND && armorItem.getSlot().getIndex() == armorSlot)
                                bestItem = i;
                            continue;
                        }
                        if (equippedStack.getItem() instanceof ArmorItem) {
                            equipped = (ArmorItem) equippedStack.getItem();
                            if (armorItem.getSlot() != equipped.getSlot())
                                continue;
                            if (equipped.getMaterial().getDefenseForSlot(equipped.getSlot()) < armorItem.getMaterial().getDefenseForSlot(armorItem.getSlot())) {
                                bestItem = i;
                            } else if (equipped.getMaterial().getDefenseForSlot(equipped.getSlot()) == armorItem.getMaterial().getDefenseForSlot(armorItem.getSlot()) && equipped.getMaterial().getToughness() < armorItem.getMaterial().getToughness()) {
                                bestItem = i;
                            } else if (equipped.getMaterial().getToughness() == armorItem.getMaterial().getToughness() && InventoryHelper.INSTANCE.compareEnchants(equippedStack, itemStack, Enchantments.ALL_DAMAGE_PROTECTION)) {
                                bestItem = i;
                            }
                        }
                    }
                }
                if (bestItem != -1) {
                    stackToMove = bestItem;
                    break;
                }
            }
            if (stackToMove != -1) {
                if (equipped != null) {
                    if (InventoryHelper.INSTANCE.isInventoryFull())
                        Wrapper.INSTANCE.getMultiPlayerGameMode().handleInventoryMouseClick(0, 8 - armorSlot, 0, ClickType.THROW, Wrapper.INSTANCE.getLocalPlayer());
                    else
                        Wrapper.INSTANCE.getMultiPlayerGameMode().handleInventoryMouseClick(0, 8 - armorSlot, 0, ClickType.QUICK_MOVE, Wrapper.INSTANCE.getLocalPlayer());
                }

                InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().containerMenu, stackToMove < 9 ? stackToMove + 36 : stackToMove, ClickType.QUICK_MOVE);
                stopWatch.reset();
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
