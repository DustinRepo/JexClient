package me.dustin.jex.load.mixin;

import me.dustin.jex.load.impl.IProjectileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ProjectileEntity.class)
public abstract class MixinProjectileEntity implements IProjectileEntity {
    @Shadow
    protected abstract boolean method_26958(Entity entity);

    @Override
    public boolean method(Entity entity) {
        return this.method_26958(entity);
    }
}
