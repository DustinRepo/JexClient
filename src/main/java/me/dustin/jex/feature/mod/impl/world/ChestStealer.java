package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Automatically steal from chests when opened.")
public class ChestStealer extends Feature {

    @Op(name = "Delay", max = 1000, inc = 10)
    public int delay = 50;
    @Op(name = "Dump")
    public boolean dump;

    private final StopWatch stopWatch = new StopWatch();

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (!stopWatch.hasPassed(delay))
            return;
        if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof GenericContainerScreen) {
            if (InventoryHelper.INSTANCE.isInventoryFull() && !dump) {
                Wrapper.INSTANCE.getLocalPlayer().closeHandledScreen();
                return;
            }
            if (InventoryHelper.INSTANCE.isContainerEmpty(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler)) {
                Wrapper.INSTANCE.getLocalPlayer().closeHandledScreen();
            } else {
                int most = Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler.slots.size() - 36;
                for (int i = 0; i < most; i++) {
                    Slot slot = Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler.slots.get(i);
                    ItemStack stack = slot.getStack();
                    if (stack != null && stack.getItem() != Items.AIR) {
                        InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, slot.id, dump ? SlotActionType.THROW : SlotActionType.QUICK_MOVE, dump ? 1 : 0);
                        stopWatch.reset();
                        if (delay > 0)
                            return;
                    }
                }
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
