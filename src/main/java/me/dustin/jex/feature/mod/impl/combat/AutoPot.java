package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

@Feature.Manifest(name = "AutoPot", category = Feature.Category.COMBAT, description = "Uses health potions when health goes below selected amount.")
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
	
	@EventListener(events = { EventPlayerPackets.class })
	public void run(Event event) {
		if (event instanceof EventPlayerPackets playerPacketEvent) {
			this.setSuffix(getPotions() + "");
			if (playerPacketEvent.getMode() == EventPlayerPackets.Mode.PRE) {
				if (throwing) {
					playerPacketEvent.setPitch(90);
				}
				if (!timer.hasPassed(delay) || throwing)
					return;
				if (Wrapper.INSTANCE.getLocalPlayer().getHealth() <= health && getPotions() > 0) {
					if (getFirstPotion() < 9) {
						throwing = true;

						playerPacketEvent.setPitch(90);
						playerPacketEvent.setYaw(PlayerHelper.INSTANCE.getYaw());
						savedSlot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
						NetworkHelper.INSTANCE.sendPacket(new UpdateSelectedSlotC2SPacket(getFirstPotion()));
						InventoryHelper.INSTANCE.getInventory().selectedSlot = getFirstPotion();
						timer.reset();
					} else {
						InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, getFirstPotion() < 9 ? getFirstPotion() + 36 : getFirstPotion(), SlotActionType.SWAP, 8);
						playerPacketEvent.setPitch(90);
						playerPacketEvent.setYaw(PlayerHelper.INSTANCE.getYaw());
						savedSlot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
						NetworkHelper.INSTANCE.sendPacket(new UpdateSelectedSlotC2SPacket(8));
						InventoryHelper.INSTANCE.getInventory().selectedSlot = 8;
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
							NetworkHelper.INSTANCE.sendPacket(new UpdateSelectedSlotC2SPacket(savedSlot));
							InventoryHelper.INSTANCE.getInventory().selectedSlot = savedSlot;
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
		}
	}

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
