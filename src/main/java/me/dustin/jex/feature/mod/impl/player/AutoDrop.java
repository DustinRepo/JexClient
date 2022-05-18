package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import java.util.ArrayList;
import java.util.Map;

@Feature.Manifest(category = Feature.Category.PLAYER, description = "Automatically drop junk items from your inventory. Configurable in the Jex Options screen.")
public class AutoDrop extends Feature {
    public static AutoDrop INSTANCE;

    @Op(name = "Drop Delay(MS)", max = 1000, inc = 5)
    public int dropDelay = 50;

    private final ArrayList<Item> items = new ArrayList<>();
    private final StopWatch stopWatch = new StopWatch();

    public AutoDrop() {
        INSTANCE = this;
        items.add(Items.DIORITE);
        items.add(Items.ANDESITE);
        items.add(Items.GRANITE);
        items.add(Items.SUNFLOWER);
        items.add(Items.LILAC);
        items.add(Items.POPPY);
        items.add(Items.ROSE_BUSH);
        items.add(Items.GRASS);
        items.add(Items.TALL_GRASS);
        items.add(Items.ORANGE_TULIP);
        items.add(Items.PINK_TULIP);
        items.add(Items.RED_TULIP);
        items.add(Items.WHITE_TULIP);
        items.add(Items.FEATHER);
        items.add(Items.ROTTEN_FLESH);
        items.add(Items.SPIDER_EYE);
        items.add(Items.PHANTOM_MEMBRANE);
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (!stopWatch.hasPassed(dropDelay))
            return;
        Map<Integer, ItemStack> inventory = InventoryHelper.INSTANCE.getStacksFromInventory(true);
        if (dropDelay == 0) {
            inventory.forEach((slot, itemStack) -> {
                if (items.contains(itemStack.getItem())) {
                    InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, slot <= 27 ? slot + 9 : slot, SlotActionType.THROW, 1);
                }
            });
        } else {
            for (int slot : inventory.keySet()) {
                ItemStack itemStack = inventory.get(slot);
                if (items.contains(itemStack.getItem())) {
                    InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, slot <= 27 ? slot + 9 : slot, SlotActionType.THROW, 1);
                    stopWatch.reset();
                    return;
                }
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    public ArrayList<Item> getItems() {
        return items;
    }
}
