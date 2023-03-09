package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRenderChest;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class HolidayEvents extends Feature {

    public final Property<Boolean> christmasProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Christmas Chest")
            .value(true)
            .build();
    public final Property<Boolean> halloweenProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Halloween Mobs")
            .value(true)
            .build();

    public HolidayEvents() {
        super(Category.VISUAL);
    }

    @EventPointer
    private final EventListener<EventRenderChest> eventRenderChestEventListener = new EventListener<>(event -> {
        if (event.getMode() == EventRenderChest.Mode.PRE) {
            event.setChristmas(this.getState() && this.christmasProperty.value());
            if (!this.getState())
                super.onDisable();
        }
    });

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
            if (entity instanceof PlayerEntity || !(entity instanceof LivingEntity livingEntity))
                return;
            if (getState() && halloweenProperty.value()) {
                if (livingEntity.hasStackEquipped(EquipmentSlot.HEAD)) {
                    return;
                }
                livingEntity.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.JACK_O_LANTERN));
            } else {
                if (livingEntity.getEquippedStack(EquipmentSlot.HEAD).getItem() != Items.JACK_O_LANTERN) {
                    return;
                }
                livingEntity.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.AIR));
            }
        });
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @Override
    public void onDisable() {
        Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
            if (entity instanceof PlayerEntity || !(entity instanceof LivingEntity livingEntity))
                return;
            if (livingEntity.getEquippedStack(EquipmentSlot.HEAD).getItem() != Items.JACK_O_LANTERN) {
                return;
            }
            livingEntity.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.AIR));
        });
    }
}
