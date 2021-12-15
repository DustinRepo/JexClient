package me.dustin.jex.feature.mod.impl.player;

import bedrockminer.utils.BreakingFlowController;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.world.EventClickBlock;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;

@Feature.Manifest(category = Feature.Category.PLAYER, description = "Switch to the best tool for your needs.")
public class AutoTool extends Feature {

    @Op(name = "Return to Original Slot")
    public boolean returnToSlot = false;

    private boolean attackingBlock;
    private int savedSlot;

    @EventPointer
    private final EventListener<EventClickBlock> eventClickBlockEventListener = new EventListener<>(event -> {
        if (AutoEat.isEating || BreakingFlowController.isWorking())
            return;
        if (!Wrapper.INSTANCE.getLocalPlayer().isCreative()) {
            int slot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
            BlockState blockState = Wrapper.INSTANCE.getWorld().getBlockState(event.getBlockPos());
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
                if (InventoryHelper.INSTANCE.getInventory().getStack(InventoryHelper.INSTANCE.getInventory().selectedSlot).isDamageable() && !InventoryHelper.INSTANCE.hasEnchantment(InventoryHelper.INSTANCE.getInventory().getStack(InventoryHelper.INSTANCE.getInventory().selectedSlot), Enchantments.SILK_TOUCH)) {
                    slot = getNonDamageSlot();
                }
            }
            if (!attackingBlock && slot != -1) {
                savedSlot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
            }
            if (slot != -1 && slot != InventoryHelper.INSTANCE.getInventory().selectedSlot) {
                InventoryHelper.INSTANCE.setSlot(slot, true, true);
            }
            attackingBlock = true;
        }
    });

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (!Wrapper.INSTANCE.getInteractionManager().isBreakingBlock() && attackingBlock) {
            if (returnToSlot) {
                InventoryHelper.INSTANCE.setSlot(savedSlot, true, true);
            }
            attackingBlock = false;
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    private int getNonDamageSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = InventoryHelper.INSTANCE.getInventory().getStack(i);
            if (!itemStack.isDamageable())
                return i;
        }
        return -1;
    }

}
