package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.load.impl.IProjectile;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Projectile.class)
public abstract class MixinProjectile implements IProjectile {

    @Shadow protected abstract boolean canHitEntity(Entity entity);

    @Override
    public boolean callCanHit(Entity entity) {
        return this.canHitEntity(entity);
    }
}
