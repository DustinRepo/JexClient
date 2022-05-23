package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import me.dustin.events.core.annotate.EventPointer;

public class AutoSoup extends Feature {

    public final Property<Integer> healthProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Health")
            .value(17)
            .min(1)
            .max(20)
            .build();
    public final Property<Long> delayProperty = new Property.PropertyBuilder<Long>(this.getClass())
            .name("Delay (MS)")
            .value(160L)
            .max(1000)
            .inc(10)
            .build();
    public final Property<Long> usedelayProperty = new Property.PropertyBuilder<Long>(this.getClass())
            .name("Use Delay (MS)")
            .value(20L)
            .max(1000)
            .build();
    public boolean throwing = false;
    int savedSlot;
    private final StopWatch stopWatch = new StopWatch();

    public AutoSoup() {
        super(Category.COMBAT, "Eats soup when your health gets below a certain amount.");
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {

        this.setSuffix(getSoups() + "");
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            if (!stopWatch.hasPassed(delayProperty.value()) || throwing)
                return;
            if (Wrapper.INSTANCE.getLocalPlayer().getHealth() <= healthProperty.value() && getSoups() > 0) {
                if (getFirstSoup() < 9) {
                    throwing = true;

                    savedSlot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
                    InventoryHelper.INSTANCE.setSlot(getFirstSoup(), true, true);
                    stopWatch.reset();
                } else {
                    InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, getFirstSoup() < 9 ? getFirstSoup() + 36 : getFirstSoup(), SlotActionType.SWAP, 8);
                    savedSlot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
                    InventoryHelper.INSTANCE.setSlot(8, true, true);
                    throwing = true;
                    stopWatch.reset();
                }
            } else {
                throwing = false;
            }
        } else {
            if (throwing && stopWatch.hasPassed(usedelayProperty.value())) {
                if (getFirstSoup() != -1) {
                    if (getFirstSoup() < 9) {
                        Wrapper.INSTANCE.getClientPlayerInteractionManager().interactItem(Wrapper.INSTANCE.getPlayer(), Hand.MAIN_HAND);
                        InventoryHelper.INSTANCE.setSlot(savedSlot, true, true);
                        throwing = false;
                        stopWatch.reset();
                    } else {
                        InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, getFirstSoup() < 9 ? getFirstSoup() + 36 : getFirstSoup(), SlotActionType.SWAP, 8);
                    }
                } else {
                    throwing = false;

                }

            }
        }
    });

    public int getSoups() {
        int potions = 0;
        for (int i = 0; i < 45; i++) {
            ItemStack itemStack = InventoryHelper.INSTANCE.getInventory().getStack(i);
            if (isSoup(itemStack)) {
                potions++;
            }
        }
        return potions;
    }

    public int getFirstSoup() {
        for (int i = 0; i < 45; i++) {
            ItemStack itemStack = InventoryHelper.INSTANCE.getInventory().getStack(i);
            if (isSoup(itemStack)) {
                return i;
            }
        }
        return -1;
    }

    public boolean isSoup(ItemStack itemStack) {
        return itemStack.getItem() == Items.MUSHROOM_STEW || itemStack.getItem() == Items.RABBIT_STEW || itemStack.getItem() == Items.BEETROOT_SOUP || itemStack.getItem() == Items.SUSPICIOUS_STEW;
    }

    @Override
    public void onDisable() {
        throwing = false;
        super.onDisable();
    }
}
