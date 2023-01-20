package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import me.dustin.events.core.annotate.EventPointer;

public class AutoPot extends Feature {

	public final Property<Integer> healthProperty = new Property.PropertyBuilder<Integer>(this.getClass())
			.name("Health")
			.value(17)
			.min(1)
			.max(20)
		        .inc(1)
			.build();
	public final Property<Long> delayProperty = new Property.PropertyBuilder<Long>(this.getClass())
			.name("Delay (MS)")
			.value(160L)
		        .min(0)
			.max(1000)
			.inc(10)
			.build();
	public final Property<Long> throwdelayProperty = new Property.PropertyBuilder<Long>(this.getClass())
			.name("Throw Delay (MS)")
			.value(20L)
		        .min(0)
			.max(1000)
		        .inc(10)
			.build();

	public boolean throwing = false;
	int savedSlot;
	private final StopWatch stopWatch = new StopWatch();

	public AutoPot() {
		super(Category.COMBAT, "Uses health potions when health goes below selected amount.");
	}

	@EventPointer
	private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
		this.setSuffix(getPotions() + "");
		if (event.getMode() == EventPlayerPackets.Mode.PRE) {
			if (throwing) {
				event.setPitch(90);
			}
			if (!stopWatch.hasPassed(delayProperty.value()) || throwing)
				return;
			if (Wrapper.INSTANCE.getLocalPlayer().getHealth() <= healthProperty.value() && getPotions() > 0) {
				if (getFirstPotion() < 9) {
					throwing = true;

					event.setPitch(90);
					event.setYaw(PlayerHelper.INSTANCE.getYaw());
					savedSlot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
					InventoryHelper.INSTANCE.setSlot(getFirstPotion(), true, true);
					stopWatch.reset();
				} else {
					InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, getFirstPotion() < 9 ? getFirstPotion() + 36 : getFirstPotion(), SlotActionType.SWAP, 8);
					event.setPitch(90);
					event.setYaw(PlayerHelper.INSTANCE.getYaw());
					savedSlot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
					InventoryHelper.INSTANCE.setSlot(8, true, true);
					throwing = true;
					stopWatch.reset();
				}
			} else {
				throwing = false;
			}
		} else {
			if (throwing && stopWatch.hasPassed(throwdelayProperty.value())) {
				if (getFirstPotion() != -1) {
					if (getFirstPotion() < 9) {
						Wrapper.INSTANCE.getClientPlayerInteractionManager().interactItem(Wrapper.INSTANCE.getPlayer(), Hand.MAIN_HAND);
						InventoryHelper.INSTANCE.setSlot(savedSlot, true, true);
						throwing = false;
						stopWatch.reset();
					} else {
						InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, getFirstPotion() < 9 ? getFirstPotion() + 36 : getFirstPotion(), SlotActionType.SWAP, 8);
					}
				} else {
					throwing = false;

				}

			}
		}
	});

	public int getPotions() {
		int potions = 0;
		for (int i = 0; i < 45; i++) {
			ItemStack itemStack = InventoryHelper.INSTANCE.getInventory().getStack(i);
			if (isPotion(itemStack)) {
				potions++;
			}
		}
		return potions;
	}

	public int getFirstPotion() {
		for (int i = 0; i < 45; i++) {
			ItemStack itemStack = InventoryHelper.INSTANCE.getInventory().getStack(i);
			if (isPotion(itemStack)) {
				return i;
			}
		}
		return -1;
	}

	public boolean isPotion(ItemStack itemStack) {
		if (itemStack != null && itemStack.getItem() instanceof SplashPotionItem) {
			if (itemStack.getNbt().getString("Potion").equalsIgnoreCase("minecraft:healing") || itemStack.getNbt().getString("Potion").equalsIgnoreCase("minecraft:strong_healing"))
				return true;
		}
		return false;
	}

	@Override
	public void onDisable() {
		throwing = false;
		super.onDisable();
	}
}
