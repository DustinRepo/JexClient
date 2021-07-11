package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

@Feature.Manifest(name = "AutoTotem", category = Feature.Category.COMBAT, description = "Keep a Totem in your offhand at all times.")
public class AutoTotem extends Feature {

    @EventListener(events = {EventPlayerPackets.class})
    public void runEvent(EventPlayerPackets event) {
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            if (!(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler instanceof PlayerScreenHandler) || Wrapper.INSTANCE.getMinecraft().currentScreen instanceof InventoryScreen)
                return;
            if (Wrapper.INSTANCE.getLocalPlayer().getOffHandStack() == null || Wrapper.INSTANCE.getLocalPlayer().getOffHandStack().getItem() == Items.AIR && getFirstTotem() != -1) {
                InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, getFirstTotem() < 9 ? getFirstTotem() + 36 : getFirstTotem(), SlotActionType.PICKUP);
                InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, 45, SlotActionType.PICKUP);
            }
            this.setSuffix(getTotems() + "");
        }
    }

    public int getTotems() {
        int count = 0;
        for (int i = 0; i < 44; i++) {
            ItemStack itemStack = InventoryHelper.INSTANCE.getInventory().getStack(i);
            if (itemStack != null && itemStack.getItem() == Items.TOTEM_OF_UNDYING)
                count++;
        }
        return count;
    }

    public int getFirstTotem() {
        for (int i = 0; i < 44; i++) {
            ItemStack itemStack = InventoryHelper.INSTANCE.getInventory().getStack(i);
            if (itemStack != null && itemStack.getItem() == Items.TOTEM_OF_UNDYING)
                return i;
        }
        return -1;
    }

}
