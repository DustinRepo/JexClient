package me.dustin.jex.feature.impl.world;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

@Feat(name = "ChestStealer", category = FeatureCategory.WORLD, description = "Automatically steal from chests when opened.")
public class ChestStealer extends Feature {

    @Op(name = "Delay", max = 1000, inc = 10)
    public int delay = 50;
    @Op(name = "Dump")
    public boolean dump;

    private Timer timer = new Timer();

    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            if (!timer.hasPassed(delay))
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
                            timer.reset();
                            if (delay > 0)
                                return;
                        }
                    }
                }
            }
        }
    }
}
