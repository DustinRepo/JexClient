package me.dustin.jex.feature.mod.impl.player;

import me.dustin.jex.JexClient;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

@Feature.Manifest(category = Feature.Category.PLAYER, description = "Automatically swap your Elytra and your Chestplate on toggle")
public class ElytraSwap extends Feature {

    @Override
    public void onEnable() {
        ItemStack equippedStack = InventoryHelper.INSTANCE.getInventory().getItem(38);
        if (equippedStack.getItem() instanceof ArmorItem) {
            //wearing armor, look for elytra
            int bestElytraSlot = -1;
            ItemStack bestSelected = null;

            for (int i = 0; i < 36; i++) {
                ItemStack selected = InventoryHelper.INSTANCE.getInventory().getItem(i);
                if (selected.getItem() == Items.ELYTRA) {
                    if (ElytraItem.isFlyEnabled(selected)) {
                        if (bestElytraSlot == -1) {
                            bestElytraSlot = i;
                            bestSelected = selected;
                        } else {
                            if (InventoryHelper.INSTANCE.compareEnchants(selected, bestSelected, Enchantments.MENDING)) {
                                bestElytraSlot = i;
                                bestSelected = selected;
                            } else if (InventoryHelper.INSTANCE.compareEnchants(selected, bestSelected, Enchantments.UNBREAKING)) {
                                bestElytraSlot = i;
                                bestSelected = selected;
                            }
                        }
                    }
                }
            }
            if (bestElytraSlot != -1) {
                if (bestElytraSlot > 8) {
                    InventoryHelper.INSTANCE.swapToHotbar(bestElytraSlot, 8);
                    bestElytraSlot = 8;
                }
                //swap on slot 6 as that's the chest slot
                InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().containerMenu, 6, ClickType.SWAP, bestElytraSlot);
                ChatHelper.INSTANCE.addRawMessage("\2478[\247aElytraSwap\2478]\247f: \2477Equipped " + bestSelected.getHoverName().getString());
            } else {
                ChatHelper.INSTANCE.addRawMessage("\2478[\247aElytraSwap\2478]\247f: \2477No elytra available for swap!");
            }
        } else if (equippedStack.getItem() == Items.ELYTRA){
            //wearing elytra, look for armor
            int bestChestSlot = -1;
            ItemStack bestSelectedStack = null;
            for (int i = 0; i < 36; i++) {
                ItemStack selected = InventoryHelper.INSTANCE.getInventory().getItem(i);
                if (selected.getItem() instanceof ArmorItem armorItem && armorItem.getSlot() == EquipmentSlot.CHEST) {
                    if (bestSelectedStack == null) {
                        bestChestSlot = i;
                        bestSelectedStack = selected;
                        continue;
                    }
                    ArmorItem bestSelected = (ArmorItem) bestSelectedStack.getItem();
                    if (bestSelected.getMaterial().getDefenseForSlot(bestSelected.getSlot()) < armorItem.getMaterial().getDefenseForSlot(armorItem.getSlot())) {
                        bestChestSlot = i;
                        bestSelectedStack = selected;
                    } else if (bestSelected.getMaterial().getDefenseForSlot(bestSelected.getSlot()) == armorItem.getMaterial().getDefenseForSlot(armorItem.getSlot()) && bestSelected.getMaterial().getToughness() < armorItem.getMaterial().getToughness()) {
                        bestChestSlot = i;
                        bestSelectedStack = selected;
                    } else if (bestSelected.getMaterial().getToughness() == armorItem.getMaterial().getToughness() && InventoryHelper.INSTANCE.compareEnchants(equippedStack, selected, Enchantments.ALL_DAMAGE_PROTECTION)) {
                        bestChestSlot = i;
                        bestSelectedStack = selected;
                    }
                }
            }
            if (bestChestSlot != -1) {
                if (bestChestSlot > 8) {
                    InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().containerMenu, bestChestSlot, ClickType.SWAP, 8);
                    bestChestSlot = 8;
                }
                //swap on slot 6 as that's the chest slot
                InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().containerMenu, 6, ClickType.SWAP, bestChestSlot);
                ChatHelper.INSTANCE.addRawMessage("\2478[\247aElytraSwap\2478]\247f: \2477Equipped " + bestSelectedStack.getHoverName().getString());
            } else {
                ChatHelper.INSTANCE.addRawMessage("\2478[\247aElytraSwap\2478]\247f: \2477No chestplate available for swap!");
            }
        }
        setState(false);
    }

    @Override
    public void onDisable() {
    }
}
