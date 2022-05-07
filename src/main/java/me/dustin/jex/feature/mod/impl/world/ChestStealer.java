package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;

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
        if (Wrapper.INSTANCE.getMinecraft().screen instanceof ContainerScreen) {
            if (InventoryHelper.INSTANCE.isInventoryFull() && !dump) {
                Wrapper.INSTANCE.getLocalPlayer().closeContainer();
                return;
            }
            if (InventoryHelper.INSTANCE.isContainerEmpty(Wrapper.INSTANCE.getLocalPlayer().containerMenu)) {
                Wrapper.INSTANCE.getLocalPlayer().closeContainer();
            } else {
                int most = Wrapper.INSTANCE.getLocalPlayer().containerMenu.slots.size() - 36;
                for (int i = 0; i < most; i++) {
                    Slot slot = Wrapper.INSTANCE.getLocalPlayer().containerMenu.slots.get(i);
                    ItemStack stack = slot.getItem();
                    if (stack != null && stack.getItem() != Items.AIR) {
                        InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().containerMenu, slot.index, dump ? ClickType.THROW : ClickType.QUICK_MOVE, dump ? 1 : 0);
                        stopWatch.reset();
                        if (delay > 0)
                            return;
                    }
                }
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
