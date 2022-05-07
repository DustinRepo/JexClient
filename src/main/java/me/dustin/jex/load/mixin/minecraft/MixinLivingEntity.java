package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.player.EventSetPlayerHealth;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    @Shadow public abstract void setHealth(float health);

    @Shadow public abstract float getHealth();

    @Inject(method = "setHealth", at = @At("HEAD"), cancellable = true)
    public void checkHealth(float health, CallbackInfo ci) {
        if (getThis() != Wrapper.INSTANCE.getLocalPlayer())
            return;
        EventSetPlayerHealth eventSetPlayerHealth = new EventSetPlayerHealth(health).run();
        if (eventSetPlayerHealth.isCancelled()) {
            ci.cancel();
            if (eventSetPlayerHealth.getHealth() != health)
                setHealth(eventSetPlayerHealth.getHealth());
        }
    }

    private LivingEntity getThis() {
        return (LivingEntity) (Object)this;
    }

}
