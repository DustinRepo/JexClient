package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.player.AutoEat;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;

import java.util.Map;

public class AutoWeapon extends Feature {

    public final Property<Boolean> livingOnlyProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Living Only")
            .description("Only pull your weapon out for living entities.")
            .value(true)
            .build();
    public final Property<AttackMode> mode = new Property.PropertyBuilder<AttackMode>(this.getClass())
            .name("Mode")
            .value(AttackMode.SWORD)
            .build();

    public AutoWeapon() {
        super(Category.COMBAT, "Automatically swap to the best weapon when attacking.");
    }

    @EventPointer
    private final EventListener<EventAttackEntity> eventAttackEntityEventListener = new EventListener<>(event -> {
        if (AutoEat.isEating)
            return;
        if (livingOnlyProperty.value() && !(event.getEntity() instanceof LivingEntity))
            return;
        int slot = -1;
        float str = 1;
        ItemStack stack = null;
        for (int i = 0; i < 9; i++) {
            ItemStack stackInSlot = InventoryHelper.INSTANCE.getInventory().getStack(i);
            if (stackInSlot != null) {
                if (!isGoodItem(stackInSlot.getItem()))
                    continue;
                float damage = InventoryHelper.INSTANCE.getAdjustedDamage(stackInSlot);

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
            InventoryHelper.INSTANCE.setSlot(slot, true, true);
        }
    });

    private boolean isGoodItem(Item item) {
        return switch (mode.value()) {
            case SWORD -> item instanceof SwordItem;
            case SWORD_AND_AXE -> item instanceof SwordItem || item instanceof AxeItem;
            case ALL_TOOLS -> item instanceof ToolItem;
        };
    }

    public enum AttackMode {
        SWORD, SWORD_AND_AXE, ALL_TOOLS
    }
}
