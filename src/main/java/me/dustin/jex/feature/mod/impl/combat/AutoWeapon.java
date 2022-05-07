package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.player.AutoEat;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import java.util.Map;

@Feature.Manifest(category = Feature.Category.COMBAT, description = "Automatically swap to the best weapon when attacking.")
public class AutoWeapon extends Feature {

    @Op(name = "Living Only")
    public boolean livingOnly = true;
    @Op(name = "Mode", all = {"Sword", "Sword&Axe", "All Tools"})
    public String mode = "Sword";

    @EventPointer
    private final EventListener<EventAttackEntity> eventAttackEntityEventListener = new EventListener<>(event -> {
        if (AutoEat.isEating)
            return;
        if (livingOnly && !(event.getEntity() instanceof LivingEntity))
            return;
        int slot = -1;
        float str = 1;
        ItemStack stack = null;
        for (int i = 0; i < 9; i++) {
            ItemStack stackInSlot = InventoryHelper.INSTANCE.getInventory().getItem(i);
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
        if (slot != -1 && slot != InventoryHelper.INSTANCE.getInventory().selected) {
            InventoryHelper.INSTANCE.setSlot(slot, true, true);
        }
    });

    private boolean isGoodItem(Item item) {
        return switch (mode.toLowerCase()) {
            case "sword" -> item instanceof SwordItem;
            case "sword&axe" -> item instanceof SwordItem || item instanceof AxeItem;
            case "all tools" -> item instanceof TieredItem;
            default -> false;
        };
    }

    private float getAdjustedDamage(ItemStack itemStack) {
        float damage = 1;
        if (itemStack.getItem() instanceof SwordItem itemSword) {
            damage = itemSword.getDamage();
        } else if (itemStack.getItem() instanceof DiggerItem miningToolItem) {
            damage = miningToolItem.getAttackDamage();
        }
        return damage + getSharpnessModifier(itemStack);
    }

    public float getSharpnessModifier(ItemStack itemStack) {
        if (itemStack.isEnchanted()) {
            Map<Enchantment, Integer> equippedEnchants = EnchantmentHelper.getEnchantments(itemStack);
            if (equippedEnchants.containsKey(Enchantments.SHARPNESS)) {
                int level = equippedEnchants.get(Enchantments.SHARPNESS);
                return 0.5f * level + 0.5f;
            }
        }
        return 0;
    }
}
