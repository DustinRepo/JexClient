package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.load.impl.IPersistentProjectileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PersistentProjectileEntity.class)
public abstract class MixinPersistentProjectileEntity implements IPersistentProjectileEntity {


    @Shadow
    protected abstract boolean canHit(Entity entity);

    @Override
    public boolean method(Entity entity) {
        return this.canHit(entity);
    }
}
