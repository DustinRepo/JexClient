package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.events.core.annotate.EventPointer;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

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
            if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof HandledScreen || Wrapper.INSTANCE.getMinecraft().currentScreen instanceof InventoryScreen || Wrapper.INSTANCE.getMinecraft().currentScreen instanceof MerchantScreen)
                return;
            int armorSlot = 0;
            for (; armorSlot < 4; armorSlot++) {
                int bestItem = -1;
                ItemStack equippedStack = InventoryHelper.INSTANCE.getInventory().getStack(36 + armorSlot);
                if (equippedStack.getItem() == Items.ELYTRA)
                    continue;
                for (int i = 0; i < 36; i++) {
                    if (bestItem != -1)
                        equippedStack = InventoryHelper.INSTANCE.getInventory().getStack(bestItem);
                    ItemStack itemStack = InventoryHelper.INSTANCE.getInventory().getStack(i);
                    if (itemStack != null && itemStack.getItem() instanceof ArmorItem armorItem) {
                        if (equippedStack.getItem() instanceof AirBlockItem) {
                            if (armorItem.getSlotType().getType() != EquipmentSlot.Type.HAND && armorItem.getSlotType().getEntitySlotId() == armorSlot)
                                bestItem = i;
                            continue;
                        }
                        if (equippedStack.getItem() instanceof ArmorItem) {
                            equipped = (ArmorItem) equippedStack.getItem();
                            if (armorItem.getSlotType() != equipped.getSlotType())
                                continue;
                            if (equipped.getMaterial().getProtectionAmount(equipped.getSlotType()) < armorItem.getMaterial().getProtectionAmount(armorItem.getSlotType())) {
                                bestItem = i;
                            } else if (equipped.getMaterial().getProtectionAmount(equipped.getSlotType()) == armorItem.getMaterial().getProtectionAmount(armorItem.getSlotType()) && equipped.getMaterial().getToughness() < armorItem.getMaterial().getToughness()) {
                                bestItem = i;
                            } else if (equipped.getMaterial().getToughness() == armorItem.getMaterial().getToughness() && InventoryHelper.INSTANCE.compareEnchants(equippedStack, itemStack, Enchantments.PROTECTION)) {
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
                        Wrapper.INSTANCE.getInteractionManager().clickSlot(0, 8 - armorSlot, 0, SlotActionType.THROW, Wrapper.INSTANCE.getLocalPlayer());
                    else
                        Wrapper.INSTANCE.getInteractionManager().clickSlot(0, 8 - armorSlot, 0, SlotActionType.QUICK_MOVE, Wrapper.INSTANCE.getLocalPlayer());
                }

                InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, stackToMove < 9 ? stackToMove + 36 : stackToMove, SlotActionType.QUICK_MOVE);
                stopWatch.reset();
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
