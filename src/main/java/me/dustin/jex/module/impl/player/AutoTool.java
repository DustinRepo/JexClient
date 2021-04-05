package me.dustin.jex.module.impl.player;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.event.world.EventClickBlock;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

import java.util.Map;

@ModClass(name = "AutoTool", category = ModCategory.PLAYER, description = "Switch to the best tool for your needs.")
public class AutoTool extends Module {

    @Op(name = "Attack")
    public boolean swords = true;
    @OpChild(name = "Allow All Tools", parent = "Attack")
    public boolean allTools = false;

    @EventListener(events = {EventClickBlock.class, EventAttackEntity.class})
    public void run(Event event) {
        if (AutoEat.isEating)
            return;
        if (event instanceof EventClickBlock && !Wrapper.INSTANCE.getLocalPlayer().isCreative()) {
            int slot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
            BlockState blockState = Wrapper.INSTANCE.getWorld().getBlockState(((EventClickBlock) event).getBlockPos());
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
            if (slot != -1 && slot != InventoryHelper.INSTANCE.getInventory().selectedSlot) {
                NetworkHelper.INSTANCE.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
                InventoryHelper.INSTANCE.getInventory().selectedSlot = slot;
            }
        }
        if (event instanceof EventAttackEntity && swords) {
            EventAttackEntity eventAttackEntity = (EventAttackEntity) event;
            int slot = -1;
            float str = 1;
            ItemStack stack = null;
            for (int i = 0; i < 9; i++) {
                ItemStack stackInSlot = InventoryHelper.INSTANCE.getInventory().getStack(i);
                if (stackInSlot != null && (stackInSlot.getItem() instanceof SwordItem || stackInSlot.getItem() instanceof MiningToolItem)) {
                    if (!allTools && stackInSlot.getItem() instanceof MiningToolItem) {
                        if (!(stackInSlot.getItem() instanceof AxeItem))
                            continue;
                    }
                    float damage = getAdjustedDamage(stackInSlot);

                    if (damage > str) {
                        str = damage;
                        slot = i;
                        stack = stackInSlot;
                    }
                    if (damage == str && str != 1) {
                        if (InventoryHelper.INSTANCE.compareEnchants(stack, stackInSlot, Enchantments.SHARPNESS)) {
                            str = damage;
                            slot = i;
                            stack = stackInSlot;
                        }
                    }
                }

            }
            if (slot != -1 && slot != InventoryHelper.INSTANCE.getInventory().selectedSlot) {
                NetworkHelper.INSTANCE.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
                InventoryHelper.INSTANCE.getInventory().selectedSlot = slot;
            }
        }
    }

    private float getAdjustedDamage(ItemStack itemStack) {
        float damage = 1;
        if (itemStack.getItem() instanceof SwordItem) {
            SwordItem itemSword = (SwordItem) itemStack.getItem();
            damage = itemSword.getAttackDamage();
        } else if (itemStack.getItem() instanceof MiningToolItem) {
            MiningToolItem miningToolItem = (MiningToolItem) itemStack.getItem();
            damage = miningToolItem.getAttackDamage();
        }
        return damage + getSharpnessModifier(itemStack);
    }

    public float getSharpnessModifier(ItemStack itemStack) {
        if (itemStack.hasEnchantments()) {
            Map<Enchantment, Integer> equippedEnchants = EnchantmentHelper.get(itemStack);
            if (equippedEnchants.containsKey(Enchantments.SHARPNESS)) {
                int level = equippedEnchants.get(Enchantments.SHARPNESS);
                return 0.5f * level + 0.5f;
            }
        }
        return 0;
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
