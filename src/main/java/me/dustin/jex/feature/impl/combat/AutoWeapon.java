package me.dustin.jex.feature.impl.combat;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.impl.player.AutoEat;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

import java.util.Map;

@Feature.Manifest(name = "AutoWeapon", category = Feature.Category.COMBAT, description = "Automatically swap to the best weapon when attacking.")
public class AutoWeapon extends Feature {

    @Op(name = "Living Only")
    public boolean livingOnly = true;
    @Op(name = "Mode", all = {"Sword", "Sword&Axe", "All Tools"})
    public String mode = "Sword";

    @EventListener(events = {EventAttackEntity.class})
    public void run(EventAttackEntity eventAttackEntity) {
        if (AutoEat.isEating)
            return;
        if (livingOnly && !(eventAttackEntity.getEntity() instanceof LivingEntity))
            return;
            int slot = -1;
            float str = 1;
            ItemStack stack = null;
            for (int i = 0; i < 9; i++) {
                ItemStack stackInSlot = InventoryHelper.INSTANCE.getInventory().getStack(i);
                if (stackInSlot != null) {
                    if (!isGoodItem(stackInSlot.getItem()))
                        continue;
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
    private boolean isGoodItem(Item item) {
        return switch (mode.toLowerCase()) {
            case "sword" -> item instanceof SwordItem;
            case "sword&axe" -> item instanceof SwordItem || item instanceof AxeItem;
            case "all tools" -> item instanceof ToolItem;
            default -> false;
        };
    }

    private float getAdjustedDamage(ItemStack itemStack) {
        float damage = 1;
        if (itemStack.getItem() instanceof SwordItem itemSword) {
            damage = itemSword.getAttackDamage();
        } else if (itemStack.getItem() instanceof MiningToolItem miningToolItem) {
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
}
