package me.dustin.jex.feature.impl.misc;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.RenameItemC2SPacket;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

@Feat(name = "FastAnvilDupe", category = FeatureCategory.MISC, description = "Speeds up the current anvil dupe")
public class FastAnvilDupe extends Feature {

    private boolean pickedUp;
    private boolean alertedXPEmpty;
    private boolean alertedInventoryNotFull;

    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            if (Wrapper.INSTANCE.getLocalPlayer().age % 2 != 0)
                return;
            if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AnvilScreen anvilScreen) {
                if (!InventoryHelper.INSTANCE.isInventoryFull()) {
                    if (!alertedInventoryNotFull) {
                        ChatHelper.INSTANCE.addClientMessage("Inventory is not full! You must have a full inventory to do this!.");
                        alertedInventoryNotFull = true;
                    }
                    return;
                }
                AnvilScreenHandler anvilScreenHandler = anvilScreen.getScreenHandler();
                if (pickedUp) {
                    InventoryHelper.INSTANCE.windowClick(anvilScreenHandler, 0, SlotActionType.PICKUP);
                    pickedUp = false;
                } else
                if (anvilScreenHandler.getSlot(2).getStack().getItem() != Items.AIR) {
                    InventoryHelper.INSTANCE.windowClick(anvilScreenHandler, 2, SlotActionType.PICKUP);
                    pickedUp = true;
                } else
                if (anvilScreenHandler.getSlot(0).getStack().getItem() != Items.AIR) {
                    if (Wrapper.INSTANCE.getLocalPlayer().experienceLevel < 1) {
                        if (!alertedXPEmpty) {
                            ChatHelper.INSTANCE.addClientMessage("Out of XP! Can not continue dupe.");
                            alertedXPEmpty = true;
                        }
                        pickedUp = false;
                        return;
                    }
                    alertedXPEmpty = false;
                    alertedInventoryNotFull = false;
                    String currentName = anvilScreenHandler.getSlot(0).getStack().getName().getString();
                    NetworkHelper.INSTANCE.sendPacket(new RenameItemC2SPacket(currentName.equalsIgnoreCase("dupe") ? "dupe-1" : "dupe"));
                    anvilScreenHandler.updateResult();
                }
            }
        }
    }

}
