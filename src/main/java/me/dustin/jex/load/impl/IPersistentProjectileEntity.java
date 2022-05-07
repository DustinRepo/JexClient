package me.dustin.jex.load.impl;

import net.minecraft.world.entity.Entity;

public interface IPersistentProjectileEntity {

    boolean callCanHit(Entity entity);

}
