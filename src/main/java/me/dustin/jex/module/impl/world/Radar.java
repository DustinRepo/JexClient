package me.dustin.jex.module.impl.world;

import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;

@ModClass(name = "Radar", category = ModCategory.WORLD, description = "Draws a Radar on your HUD telling you where entities are")
public class Radar extends Module {
    public static Radar INSTANCE;
    @Op(name = "Players")
    public boolean players = true;
    @Op(name = "Hostiles")
    public boolean hostiles = true;
    @Op(name = "Passives")
    public boolean passives = true;
    @Op(name = "Items")
    public boolean items = true;

    public Radar() {
        INSTANCE = this;
    }

    public boolean isValid(Entity entity) {
        if (entity instanceof PlayerEntity && entity != Wrapper.INSTANCE.getLocalPlayer())
            return players;
        if (entity instanceof ItemEntity)
            return items;
        if (EntityHelper.INSTANCE.isHostileMob(entity))
            return hostiles;
        if (EntityHelper.INSTANCE.isPassiveMob(entity))
            return passives;
        return false;
    }
}
