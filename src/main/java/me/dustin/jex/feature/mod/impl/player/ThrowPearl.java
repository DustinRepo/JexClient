package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.KeyPressFilter;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

@Feature.Manifest(category = Feature.Category.PLAYER, description = "Automatically throw ender pearl from hotbar on button press.")
public class ThrowPearl extends Feature {

    @Op(name = "Throw Key", isKeybind = true)
    public int throwKey = GLFW.GLFW_KEY_Y;

    @EventPointer
    private final EventListener<EventKeyPressed> eventKeyPressedEventListener = new EventListener<>(event -> {
        if (event.getKey() == throwKey) {
            int slot = InventoryHelper.INSTANCE.getFromHotbar(Items.ENDER_PEARL);
            boolean offhand = InventoryHelper.INSTANCE.getInventory().getItem(45).getItem() == Items.ENDER_PEARL;
            if (slot == -1 && !offhand) {
                ChatHelper.INSTANCE.addClientMessage("You have no ender pearls in your hotbar");
            } else {
                int savedSlot = InventoryHelper.INSTANCE.getInventory().selected;
                if (slot != -1) {
                    InventoryHelper.INSTANCE.setSlot(slot, true, true);
                }
                Wrapper.INSTANCE.getMultiPlayerGameMode().useItem(Wrapper.INSTANCE.getPlayer(), offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
                NetworkHelper.INSTANCE.sendPacket(new ServerboundSwingPacket(offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND));
                if (slot != -1) {
                    InventoryHelper.INSTANCE.setSlot(savedSlot, true, true);
                }
            }
        }
    }, new KeyPressFilter(EventKeyPressed.PressType.IN_GAME));
}
