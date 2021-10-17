package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.event.misc.EventMouseButton;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

@Feature.Manifest(category = Feature.Category.PLAYER, description = "Automatically throw ender pearl from hotbar on button press.")
public class ThrowPearl extends Feature {

    @Op(name = "Throw Key", isKeybind = true)
    public int throwKey = GLFW.GLFW_KEY_Y;

    @EventListener(events = {EventKeyPressed.class})
    private void runMethod(EventKeyPressed eventKeyPressed) {
        if (eventKeyPressed.getKey() == throwKey && eventKeyPressed.getType() == EventKeyPressed.PressType.IN_GAME) {
            int slot = InventoryHelper.INSTANCE.getFromHotbar(Items.ENDER_PEARL);
            boolean offhand = InventoryHelper.INSTANCE.getInventory().getStack(45).getItem() == Items.ENDER_PEARL;
            if (slot == -1 && !offhand) {
                ChatHelper.INSTANCE.addClientMessage("You have no ender pearls in your hotbar");
            } else {
                int savedSlot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
                if (slot != -1) {
                    InventoryHelper.INSTANCE.setSlot(slot, true, true);
                }
                NetworkHelper.INSTANCE.sendPacket(new PlayerInteractItemC2SPacket(offhand ? Hand.OFF_HAND : Hand.MAIN_HAND));
                NetworkHelper.INSTANCE.sendPacket(new HandSwingC2SPacket(offhand ? Hand.OFF_HAND : Hand.MAIN_HAND));
                if (slot != -1) {
                    InventoryHelper.INSTANCE.setSlot(savedSlot, true, true);
                }
            }
        }
    }
}
