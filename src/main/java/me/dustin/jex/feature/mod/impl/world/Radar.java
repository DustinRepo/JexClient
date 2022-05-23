package me.dustin.jex.feature.mod.impl.world;

import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import me.dustin.jex.feature.mod.core.Feature;

public class Radar extends Feature {
    public static Radar INSTANCE;

    public final Property<Boolean> waypointsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Waypoints")
            .value(true)
            .build();
    public final Property<Boolean> playersProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Players")
            .value(true)
            .build();
    public final Property<Boolean> bossesProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Bosses")
            .value(true)
            .build();
    public final Property<Boolean> hostilesProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Hostiles")
            .value(true)
            .build();
    public final Property<Boolean> neutralsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Neutrals")
            .value(true)
            .build();
    public final Property<Boolean> passivesProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Passives")
            .value(true)
            .build();
    public final Property<Boolean> itemsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Items")
            .value(true)
            .build();

    public Radar() {
        super(Category.WORLD, "Draws a Radar on your HUD telling you where entities are");
        INSTANCE = this;
    }

    public boolean isValid(Entity entity) {
        if (entity instanceof PlayerEntity && entity != Wrapper.INSTANCE.getLocalPlayer())
            return playersProperty.value();
        if (entity instanceof ItemEntity)
            return itemsProperty.value();
        if (EntityHelper.INSTANCE.isBossMob(entity))
            return bossesProperty.value();
        if (EntityHelper.INSTANCE.isNeutralMob(entity))
            return neutralsProperty.value();
        if (EntityHelper.INSTANCE.isHostileMob(entity))
            return hostilesProperty.value();
        if (EntityHelper.INSTANCE.isPassiveMob(entity))
            return passivesProperty.value();
        return false;
    }
}
