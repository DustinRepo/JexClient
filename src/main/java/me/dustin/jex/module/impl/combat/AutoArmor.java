package me.dustin.jex.module.impl.combat;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

@ModClass(name = "AutoArmor", category = ModCategory.COMBAT, description = "Puts on the best armor in your inventory automatically.")
public class AutoArmor extends Module {

    @Op(name = "Delay (MS)", max = 1000)
    public int delay = 65;

    private Timer timer = new Timer();

    @EventListener(events = {EventPlayerPackets.class})
    public void runEvent(EventPlayerPackets event) {
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            if (timer.hasPassed(delay)) {
                int stackToMove = -1;
                ArmorItem equipped = null;
                ArmorItem armorItem = null;
                ItemStack itemStack = null;
                if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof HandledScreen || Wrapper.INSTANCE.getMinecraft().currentScreen instanceof InventoryScreen || Wrapper.INSTANCE.getMinecraft().currentScreen instanceof MerchantScreen)
                    return;
                for (int i = 0; i < 36; i++) {
                    itemStack = InventoryHelper.INSTANCE.getInventory().getStack(i);
                    if (itemStack != null && itemStack.getItem() instanceof ArmorItem) {
                        armorItem = (ArmorItem) itemStack.getItem();
                        ItemStack equippedStack = Wrapper.INSTANCE.getLocalPlayer().getEquippedStack(armorItem.getSlotType());

                        if (equippedStack == null || Wrapper.INSTANCE.getLocalPlayer().getEquippedStack(armorItem.getSlotType()).getItem() instanceof AirBlockItem) {
                            stackToMove = i;
                            continue;
                        }
                        if (equippedStack.getItem() == Items.ELYTRA)
                            continue;
                        try {
                            equipped = (ArmorItem) Wrapper.INSTANCE.getLocalPlayer().getEquippedStack(armorItem.getSlotType()).getItem();
                            if (equipped.getMaterial().getProtectionAmount(armorItem.getSlotType()) < armorItem.getMaterial().getProtectionAmount(armorItem.getSlotType())) {
                                stackToMove = i;
                            }
                            if (equipped.getMaterial().getProtectionAmount(armorItem.getSlotType()) == armorItem.getMaterial().getProtectionAmount(armorItem.getSlotType())) {
                                if (InventoryHelper.INSTANCE.compareEnchants(equippedStack, itemStack, Enchantments.PROTECTION)) {
                                    stackToMove = i;

                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                }
                if (stackToMove != -1) {
                    if (equipped != null)
                        Wrapper.INSTANCE.getInteractionManager().clickSlot(0, getArmorSlot(equipped), 0, SlotActionType.THROW, Wrapper.INSTANCE.getLocalPlayer());

                    //Wrapper.INSTANCE.getInteractionManager().clickSlot(0, stackToMove < 9 ? stackToMove + 36 : stackToMove, 0, SlotActionType.QUICK_MOVE, Wrapper.INSTANCE.getLocalPlayer());
                    InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, stackToMove < 9 ? stackToMove + 36 : stackToMove, SlotActionType.QUICK_MOVE);
                    timer.reset();
                }
            }
        }
    }

    public int getArmorSlot(ArmorItem armorItem) {
        switch (armorItem.getSlotType()) {
            case FEET:
                return 8;
            case LEGS:
                return 7;
            case CHEST:
                return 6;
            case HEAD:
                return 5;
        }
        return -1;
    }

}
