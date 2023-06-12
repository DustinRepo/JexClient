package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public class XPBottleSpammer extends Feature {

    public final Property<Integer> delayProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Delay")
            .value(1)
            .min(0)
            .max(1000)
            .inc(10)
            .build();
    public final Property<Integer> throwKeyProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Throw Key")
            .value(KeyboardHelper.INSTANCE.MIDDLE_CLICK)
            .isKey()
            .build();

    public XPBottleSpammer() {
        super(Category.WORLD);
    }
    
 private final StopWatch stopWatch = new StopWatch();
    
    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            if (!KeyboardHelper.INSTANCE.isPressed(throwKeyProperty.value()))
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
            if (!KeyboardHelper.INSTANCE.isPressed(throwKeyProperty.value()))
                return;
            int xpBottleHotbar = InventoryHelper.INSTANCE.getFromHotbar(Items.EXPERIENCE_BOTTLE);
            if (xpBottleHotbar == -1)
                return;
            InventoryHelper.INSTANCE.setSlot(xpBottleHotbar, false, true);
           if (stopWatch.hasPassed(delayProperty.value())) {
                Wrapper.INSTANCE.getClientPlayerInteractionManager().interactItem(Wrapper.INSTANCE.getLocalPlayer(), Hand.MAIN_HAND);
           }
            InventoryHelper.INSTANCE.setSlot(InventoryHelper.INSTANCE.getInventory().selectedSlot, false, true);
        }
    });
}
