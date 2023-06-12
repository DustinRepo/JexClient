package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import me.dustin.jex.feature.mod.core.Feature;

public class ChestStealer extends Feature {

    public final Property<Long> delayProperty = new Property.PropertyBuilder<Long>(this.getClass())
            .name("Delay")
            .value(50L)
            .max(1000)
            .inc(10)
            .build();
    public final Property<Boolean> dumpProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Dump")
            .value(false)
            .build();
     public final Property<Boolean> contProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Containers")
            .value(false)
            .build();
    public final Property<Boolean> chestProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Chest")
            .value(false)
            .build();
    public final Property<Boolean> shulkerProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Shulker")
            .value(false)
            .build();
    public final Property<Boolean> hopperProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Hopper")
            .value(false)
            .build();
    public final Property<Boolean> horseProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Horse")
            .value(false)
            .build();
    public final Property<Boolean> bsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("BrewingStand")
            .value(false)
            .build();

    private final StopWatch stopWatch = new StopWatch();
    
    public ChestStealer() {
        super(Category.WORLD);
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        Wrapper.INSTANCE.getMinecraft().currentScreen = screen;
        if (!stopWatch.hasPassed(delayProperty.value()))
            return;
        if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof HandledScreen && isValid(screen)) {
            if (InventoryHelper.INSTANCE.isInventoryFull() && !dumpProperty.value()) {
                Wrapper.INSTANCE.getLocalPlayer().closeHandledScreen();
                return;
               }
        }
            if (InventoryHelper.INSTANCE.isContainerEmpty(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler)) {
                Wrapper.INSTANCE.getLocalPlayer().closeHandledScreen();
            } else {
                int most = Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler.slots.size() - 36;
                for (int i = 0; i < most; i++) {
                    Slot slot = Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler.slots.get(i);
                    ItemStack stack = slot.getStack();
                    if (stack != null && stack.getItem() != Items.AIR) {
                        InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, slot.id, dumpProperty.value() ? SlotActionType.THROW : SlotActionType.QUICK_MOVE, dumpProperty.value() ? 1 : 0);
                        stopWatch.reset();
                        if (delayProperty.value() > 0)
                            return;
                    }
                }
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
private class boolean isValid() {
if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof GenericContainerScreen)
return chestProperty.value();
if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof ShulkerBoxScreen)
return shulkerProperty.value();
if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof HopperScreen)
return hopperProperty.value();
if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof HorseScreen)
return horseProperty.value();
if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof BrewingStandScreen)
return bsProperty.value();
   }
}
