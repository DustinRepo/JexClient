package me.dustin.jex.feature.mod.impl.player;

import bedrockminer.utils.BreakingFlowController;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClickBlockFilter;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.world.EventClickBlock;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;

public class AutoTool extends Feature {

    public final Property<Boolean> returnToSlotProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Return to Original Slot")
            .value(false)
            .build();

    private boolean attackingBlock;
    private int savedSlot;

    public AutoTool() {
        super(Category.PLAYER, "Switch to the best tool for your needs.");
    }

    @EventPointer
    private final EventListener<EventClickBlock> eventClickBlockEventListener = new EventListener<>(event -> {
        if (AutoEat.isEating || BreakingFlowController.isWorking() || !WorldHelper.INSTANCE.isBreakable(WorldHelper.INSTANCE.getBlock(event.getBlockPos())))
            return;
        if (!Wrapper.INSTANCE.getLocalPlayer().isCreative()) {
            int slot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
            BlockState blockState = Wrapper.INSTANCE.getWorld().getBlockState(event.getBlockPos());

            float best = 1;
            boolean found = false;
            for (int index = 0; index < 9; index++) {
                ItemStack itemStack = InventoryHelper.INSTANCE.getInventory().getStack(index);
                if (blockState.getBlock() != Blocks.AIR) {
                    float miningSpeedMultiplier = itemStack.getMiningSpeedMultiplier(blockState);
                    boolean isHoeOnCrop = blockState.getBlock() instanceof CropBlock && itemStack.getItem() instanceof HoeItem;
                    //apparently axes are considerably faster than hoes for crops, but if you have a hoe odds are it's being used for those crops
                    if (isHoeOnCrop) {
                        miningSpeedMultiplier *= 100;
                    }

                    if (miningSpeedMultiplier > best) {
                        best = miningSpeedMultiplier;
                        slot = index;
                        found = true;
                    } else if (miningSpeedMultiplier == best && best > 1) {
                        if (isHoeOnCrop) {
                            if (InventoryHelper.INSTANCE.compareEnchants(InventoryHelper.INSTANCE.getInventory().getStack(slot), itemStack, Enchantments.FORTUNE)) {
                                best = miningSpeedMultiplier;
                                slot = index;
                            }
                        } else if (InventoryHelper.INSTANCE.compareEnchants(InventoryHelper.INSTANCE.getInventory().getStack(slot), itemStack, Enchantments.EFFICIENCY)) {
                            best = miningSpeedMultiplier;
                            slot = index;
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
    }, new ClickBlockFilter(EventClickBlock.Mode.PRE));

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (!Wrapper.INSTANCE.getClientPlayerInteractionManager().isBreakingBlock() && attackingBlock) {
            if (returnToSlotProperty.value()) {
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
