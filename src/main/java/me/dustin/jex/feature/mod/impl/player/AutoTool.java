package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.world.EventClickBlock;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

@Feature.Manifest(name = "AutoTool", category = Feature.Category.PLAYER, description = "Switch to the best tool for your needs.")
public class AutoTool extends Feature {

    @Op(name = "Return to Original Slot")
    public boolean returnToSlot = true;

    private boolean attackingBlock;
    private int savedSlot;

    @EventListener(events = {EventClickBlock.class, EventPlayerPackets.class})
    public void run(Event event) {
        if (event instanceof EventClickBlock eventClickBlock) {
            if (AutoEat.isEating)
                return;
            if (!Wrapper.INSTANCE.getLocalPlayer().isCreative()) {
                int slot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
                BlockState blockState = Wrapper.INSTANCE.getWorld().getBlockState(eventClickBlock.getBlockPos());
                float best = 1;
                boolean found = false;
                for (int index = 0; index < 9; index++) {
                    ItemStack itemStack = InventoryHelper.INSTANCE.getInventory().getStack(index);
                    if (blockState.getBlock() != Blocks.AIR) {
                        if (itemStack.getMiningSpeedMultiplier(blockState) > best) {
                            best = itemStack.getMiningSpeedMultiplier(blockState);
                            slot = index;
                            found = true;
                        } else if (itemStack.getMiningSpeedMultiplier(blockState) == best && best > 1) {
                            if (InventoryHelper.INSTANCE.compareEnchants(InventoryHelper.INSTANCE.getInventory().getStack(slot), itemStack, Enchantments.EFFICIENCY)) {
                                best = itemStack.getMiningSpeedMultiplier(blockState);
                                slot = index;
                                found = true;
                            }
                        }
                    }
                }
                if (slot == InventoryHelper.INSTANCE.getInventory().selectedSlot && !found) {
                    if (InventoryHelper.INSTANCE.getInventory().getStack(InventoryHelper.INSTANCE.getInventory().selectedSlot).isDamageable()) {
                        slot = getNonDamageSlot();
                    }
                }
                if (!attackingBlock && slot != -1) {
                    savedSlot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
                }
                if (slot != -1 && slot != InventoryHelper.INSTANCE.getInventory().selectedSlot) {
                    NetworkHelper.INSTANCE.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
                    InventoryHelper.INSTANCE.getInventory().selectedSlot = slot;
                }
                attackingBlock = true;
            }
        } else if (event instanceof EventPlayerPackets eventPlayerPackets && eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            if (!Wrapper.INSTANCE.getInteractionManager().isBreakingBlock() && attackingBlock) {
                if (returnToSlot) {
                    InventoryHelper.INSTANCE.getInventory().selectedSlot = savedSlot;
                    NetworkHelper.INSTANCE.sendPacket(new UpdateSelectedSlotC2SPacket(savedSlot));
                }
                attackingBlock = false;
            }
        }
    }

    private int getNonDamageSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = InventoryHelper.INSTANCE.getInventory().getStack(i);
            if (!itemStack.isDamageable())
                return i;
        }
        return -1;
    }

}
