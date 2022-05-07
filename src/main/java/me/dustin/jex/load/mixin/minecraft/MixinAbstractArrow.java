package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.load.impl.IPersistentProjectileEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractArrow.class)
public abstract class MixinAbstractArrow implements IPersistentProjectileEntity {

    @Shadow protected abstract boolean canHitEntity(Entity entity);

    @Override
    public boolean callCanHit(Entity entity) {
        return this.canHitEntity(entity);
    }
}
