package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.api.EventAPI;
import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRenderChest;
import me.dustin.jex.event.world.EventSpawnEntity;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Have holiday events like christmas chests show year-round")
public class HolidayEvents extends Feature {

    @Op(name = "Christmas Chest")
    public boolean christmas = true;
    @Op(name = "Halloween Mobs")
    public boolean halloween = true;

    @EventListener(events = {EventRenderChest.class, EventPlayerPackets.class})
    private void runMethod(Event event) {
        if (event instanceof EventRenderChest eventRenderChest && eventRenderChest.getMode() == EventRenderChest.Mode.PRE) {
            eventRenderChest.setChristmas(this.getState() && this.christmas);
            if (!this.getState())
                super.onDisable();
        } else if (event instanceof EventPlayerPackets eventPlayerPackets && eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
                if (entity instanceof PlayerEntity || !(entity instanceof LivingEntity livingEntity))
                    return;
                if (getState() && halloween) {
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
        }
    }

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
