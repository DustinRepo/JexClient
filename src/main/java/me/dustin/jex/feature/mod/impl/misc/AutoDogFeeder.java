package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

@Feature.Manifest(category = Feature.Category.MISC, description = "Automatically feed your pups to keep them at full health at all times.")
public class AutoDogFeeder extends Feature {

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        int savedSlot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
        int slot = getDogFoodSlot();
        if (slot == -1)
            return;
        Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
            if (entity instanceof WolfEntity wolfEntity && EntityHelper.INSTANCE.doesPlayerOwn(wolfEntity)) {
                if (wolfEntity.getHealth() < wolfEntity.getMaxHealth()) {
                    InventoryHelper.INSTANCE.setSlot(slot, false, true);
                    Wrapper.INSTANCE.getInteractionManager().interactEntity(Wrapper.INSTANCE.getLocalPlayer(), wolfEntity, Hand.MAIN_HAND);
                    InventoryHelper.INSTANCE.setSlot(savedSlot, false, true);
                }
            }
        });
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    private int getDogFoodSlot() {
        int steak = InventoryHelper.INSTANCE.getFromHotbar(Items.COOKED_BEEF);
        if (steak != -1)
            return steak;
        int porkChop = InventoryHelper.INSTANCE.getFromHotbar(Items.COOKED_PORKCHOP);
        if (porkChop != -1)
            return porkChop;
        int chicken = InventoryHelper.INSTANCE.getFromHotbar(Items.COOKED_CHICKEN);
        if (chicken != -1)
            return chicken;
        int mutton = InventoryHelper.INSTANCE.getFromHotbar(Items.COOKED_MUTTON);
        if (mutton != -1)
            return mutton;
        return InventoryHelper.INSTANCE.getFromHotbar(Items.COOKED_RABBIT);
    }

}
