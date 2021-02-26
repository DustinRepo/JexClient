package me.dustin.jex.module.impl.misc;

import com.google.gson.JsonObject;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.Tag;
import net.minecraft.screen.slot.SlotActionType;

@ModClass(name = "MendingSaver", category = ModCategory.MISC, description = "Save your mending tools from breaking by putting them away automatically.")
public class MendingSaver extends Module {

    @Op(name = "Notify")
    public boolean notify;

    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            ItemStack currentStack = Wrapper.INSTANCE.getLocalPlayer().getMainHandStack();
            if (currentStack != null && currentStack.hasEnchantments()) {
                for (Tag tag : currentStack.getEnchantments()) {
                    JsonObject jsonObject = JsonHelper.INSTANCE.gson.fromJson(tag.toString(), JsonObject.class);
                    if (jsonObject.get("id").getAsString().contains("mending")) {
                        if (currentStack.isDamageable() && currentStack.getDamage() > currentStack.getMaxDamage() - 10) {
                            if (notify)
                                ChatHelper.INSTANCE.addClientMessage("MendingSaver just saved your item");

                            if (!InventoryHelper.INSTANCE.isInventoryFull())
                                InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, InventoryHelper.INSTANCE.getInventory().selectedSlot + 36, SlotActionType.QUICK_MOVE);
                            else
                                InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, InventoryHelper.INSTANCE.getInventory().selectedSlot + 36, SlotActionType.SWAP, 8);
                        }
                    }
                }
            }
        }
    }
}
