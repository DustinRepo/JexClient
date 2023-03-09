package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.KeyPressFilter;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class ThrowPearl extends Feature {

    public final Property<Integer> throwKeyProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Throw Key")
            .value(GLFW.GLFW_KEY_Y)
            .isKey()
            .build();

    public ThrowPearl() {
        super(Category.PLAYER);
    }
//lol
    @EventPointer
    private final EventListener<EventKeyPressed> eventKeyPressedEventListener = new EventListener<>(event -> {
        if (event.getKey() == throwKeyProperty.value()) {
            int slot = InventoryHelper.INSTANCE.getFromHotbar(Items.ENDER_PEARL);
            boolean offhand = InventoryHelper.INSTANCE.getInventory().getStack(45).getItem() == Items.ENDER_PEARL;
            if (slot == -1 && !offhand) {
                ChatHelper.INSTANCE.addClientMessage("You have no ender pearls in your hotbar");
            } else {
                int savedSlot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
                if (slot != -1) {
                    InventoryHelper.INSTANCE.setSlot(slot, true, true);
                }
                Wrapper.INSTANCE.getClientPlayerInteractionManager().interactItem(Wrapper.INSTANCE.getPlayer(), offhand ? Hand.OFF_HAND : Hand.MAIN_HAND);
                NetworkHelper.INSTANCE.sendPacket(new HandSwingC2SPacket(offhand ? Hand.OFF_HAND : Hand.MAIN_HAND));
                if (slot != -1) {
                    InventoryHelper.INSTANCE.setSlot(savedSlot, true, true);
                }
            }
        }
    }, new KeyPressFilter(EventKeyPressed.PressType.IN_GAME));
}
