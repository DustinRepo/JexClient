package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Spam XP Bottles on a button press")
public class XPBottleSpammer extends Feature {

    @Op(name = "Speed", min = 1, max = 5)
    public int speed = 1;

    @Op(name = "Throw Key", isKeybind = true)
    public int throwKey = KeyboardHelper.INSTANCE.MIDDLE_CLICK;

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            if (!KeyboardHelper.INSTANCE.isPressed(throwKey))
                return;
            int xpBottleHotbar = InventoryHelper.INSTANCE.getFromHotbar(Items.EXPERIENCE_BOTTLE);
            if (xpBottleHotbar == -1) {
                int xpBottleInv = InventoryHelper.INSTANCE.getFromInv(Items.EXPERIENCE_BOTTLE);
                if (xpBottleInv == -1)
                    return;
                InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, xpBottleInv < 9 ? xpBottleInv + 36 : xpBottleInv, SlotActionType.SWAP, 8);
            }
            event.setRotation(new RotationVector(Wrapper.INSTANCE.getLocalPlayer().getYaw(), 90));
        } else if (event.getMode() == EventPlayerPackets.Mode.POST) {
            if (!KeyboardHelper.INSTANCE.isPressed(throwKey))
                return;
            int xpBottleHotbar = InventoryHelper.INSTANCE.getFromHotbar(Items.EXPERIENCE_BOTTLE);
            if (xpBottleHotbar == -1)
                return;
            InventoryHelper.INSTANCE.setSlot(xpBottleHotbar, false, true);
            for (int i = 0; i < speed; i++)
                Wrapper.INSTANCE.getInteractionManager().interactItem(Wrapper.INSTANCE.getLocalPlayer(), Hand.MAIN_HAND);
            InventoryHelper.INSTANCE.setSlot(InventoryHelper.INSTANCE.getInventory().selectedSlot, false, true);
        }
    });
}
