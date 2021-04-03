package me.dustin.jex.load.mixin;

import me.dustin.jex.event.world.EventSpawnEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class MixinClientWorld {

    @Inject(method = "addEntityPrivate", at = @At("HEAD"))
    public void addEntityPrivate1(int id, Entity entity, CallbackInfo ci) {
        new EventSpawnEntity(entity).run();
    }

}
