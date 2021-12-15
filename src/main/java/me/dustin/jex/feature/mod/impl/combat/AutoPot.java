package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.events.core.annotate.EventPointer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

@Feature.Manifest(category = Feature.Category.COMBAT, description = "Uses health potions when health goes below selected amount.")
public class AutoPot extends Feature {

	@Op(name = "Health", min = 1, max = 20)
	public int health = 17;

	@Op(name = "Delay (MS)", max = 1000, inc = 10)
	public int delay = 160;

	@Op(name = "Throw Delay (MS)", max = 1000, inc = 1)
	public int throwdelay = 20;
	public boolean throwing = false;
	int savedSlot;
	private Timer timer = new Timer();

	@EventPointer
	private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
		this.setSuffix(getPotions() + "");
		if (event.getMode() == EventPlayerPackets.Mode.PRE) {
			if (throwing) {
				event.setPitch(90);
			}
			if (!timer.hasPassed(delay) || throwing)
				return;
			if (Wrapper.INSTANCE.getLocalPlayer().getHealth() <= health && getPotions() > 0) {
				if (getFirstPotion() < 9) {
					throwing = true;

					event.setPitch(90);
					event.setYaw(PlayerHelper.INSTANCE.getYaw());
					savedSlot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
					InventoryHelper.INSTANCE.setSlot(getFirstPotion(), true, true);
					timer.reset();
				} else {
					InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, getFirstPotion() < 9 ? getFirstPotion() + 36 : getFirstPotion(), SlotActionType.SWAP, 8);
					event.setPitch(90);
					event.setYaw(PlayerHelper.INSTANCE.getYaw());
					savedSlot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
					InventoryHelper.INSTANCE.setSlot(8, true, true);
					throwing = true;
					timer.reset();
				}
			} else {
				throwing = false;
			}
		} else {
			if (throwing && timer.hasPassed(throwdelay)) {
				if (getFirstPotion() != -1) {
					if (getFirstPotion() < 9) {
						NetworkHelper.INSTANCE.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND));
						InventoryHelper.INSTANCE.setSlot(savedSlot, true, true);
						throwing = false;
						timer.reset();
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
