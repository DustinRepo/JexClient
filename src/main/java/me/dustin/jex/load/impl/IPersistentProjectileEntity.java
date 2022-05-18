package me.dustin.jex.load.impl;

import net.minecraft.entity.Entity;

public interface IPersistentProjectileEntity {

    boolean callCanHit(Entity entity);

}
