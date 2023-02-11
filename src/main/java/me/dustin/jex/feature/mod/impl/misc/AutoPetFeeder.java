package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.feature.property.Property;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoPetFeeder extends Feature {
    
    public final Property<TargetMode> modeProperty = new Property.PropertyBuilder<TargetMode>(this.getClass())
			.name("Pet")
			.value(TargetMode.DOG)
			.build();

    public AutoPetFeeder() {
        super(Category.MISC, "Automatically feed your pets to keep them at full health at all times.");
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        int slotdog = getDogFoodSlot();
        if (slotdog == -1)
            return;
	int slotcat = getCatFoodSlot();
        if (slotcat == -1)
            return;
        int savedSlot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
	    Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
		if (modeProperty.value() == TargetMode.DOG) {
            if (entity instanceof WolfEntity wolfEntity && EntityHelper.INSTANCE.doesPlayerOwn(wolfEntity)) {
                if (wolfEntity.getHealth() < wolfEntity.getMaxHealth()) {
                    InventoryHelper.INSTANCE.setSlot(slotdog, false, true);
                    Wrapper.INSTANCE.getClientPlayerInteractionManager().interactEntity(Wrapper.INSTANCE.getLocalPlayer(), wolfEntity, Hand.MAIN_HAND);
                    InventoryHelper.INSTANCE.setSlot(savedSlot, false, true);
                }
            }
	}		
if (modeProperty.value() == TargetMode.CAT) {
            if (entity instanceof CatEntity catEntity && EntityHelper.INSTANCE.doesPlayerOwn(catEntity)) {
                if (catEntity.getHealth() < catEntity.getMaxHealth()) {
                    InventoryHelper.INSTANCE.setSlot(slotcat, false, true);
                    Wrapper.INSTANCE.getClientPlayerInteractionManager().interactEntity(Wrapper.INSTANCE.getLocalPlayer(), catEntity, Hand.MAIN_HAND);
                    InventoryHelper.INSTANCE.setSlot(savedSlot, false, true);
                }
            }	
          }
	if (modeProperty.value() == TargetMode.BOTH) {
            if (entity instanceof CatEntity catEntity && EntityHelper.INSTANCE.doesPlayerOwn(catEntity)) {
			if (catEntity.getHealth() < catEntity.getMaxHealth()) {
                    InventoryHelper.INSTANCE.setSlot(slotcat, false, true);
                    Wrapper.INSTANCE.getClientPlayerInteractionManager().interactEntity(Wrapper.INSTANCE.getLocalPlayer(), catEntity, Hand.MAIN_HAND);
                    InventoryHelper.INSTANCE.setSlot(savedSlot, false, true);
                }
	    }
		if (entity instanceof WolfEntity wolfEntity && EntityHelper.INSTANCE.doesPlayerOwn(wolfEntity)) {
                if (wolfEntity.getHealth() < wolfEntity.getMaxHealth()) {
                    InventoryHelper.INSTANCE.setSlot(slotdog, false, true);
                    Wrapper.INSTANCE.getClientPlayerInteractionManager().interactEntity(Wrapper.INSTANCE.getLocalPlayer(), wolfEntity, Hand.MAIN_HAND);
                    InventoryHelper.INSTANCE.setSlot(savedSlot, false, true);
                }
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
	
public int getCatFoodSlot() {
        int cod = InventoryHelper.INSTANCE.getFromHotbar(Items.COD);
        if (cod != -1)
            return cod;
        int salmon = InventoryHelper.INSTANCE.getFromHotbar(Items.SALMON);
        if (salmon != -1)
            return salmon;
	return InventoryHelper.INSTANCE.getFromHotbar(Items.SALMON);
}
    public enum TargetMode {
     BOTH, DOG, CAT
   }
}
