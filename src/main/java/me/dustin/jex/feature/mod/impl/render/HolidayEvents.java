package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRenderChest;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Have holiday events like christmas chests show year-round")
public class HolidayEvents extends Feature {

    @Op(name = "Christmas Chest")
    public boolean christmas = true;
    @Op(name = "Halloween Mobs")
    public boolean halloween = true;

    @EventPointer
    private final EventListener<EventRenderChest> eventRenderChestEventListener = new EventListener<>(event -> {
        if (event.getMode() == EventRenderChest.Mode.PRE) {
            event.setChristmas(this.getState() && this.christmas);
            if (!this.getState())
                super.onDisable();
        }
    });

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        Wrapper.INSTANCE.getWorld().entitiesForRendering().forEach(entity -> {
            if (entity instanceof Player || !(entity instanceof LivingEntity livingEntity))
                return;
            if (getState() && halloween) {
                if (livingEntity.hasItemInSlot(EquipmentSlot.HEAD)) {
                    return;
                }
                livingEntity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.JACK_O_LANTERN));
            } else {
                if (livingEntity.getItemBySlot(EquipmentSlot.HEAD).getItem() != Items.JACK_O_LANTERN) {
                    return;
                }
                livingEntity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.AIR));
            }
        });
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @Override
    public void onDisable() {
        Wrapper.INSTANCE.getWorld().entitiesForRendering().forEach(entity -> {
            if (entity instanceof Player || !(entity instanceof LivingEntity livingEntity))
                return;
            if (livingEntity.getItemBySlot(EquipmentSlot.HEAD).getItem() != Items.JACK_O_LANTERN) {
                return;
            }
            livingEntity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.AIR));
        });
    }
}
