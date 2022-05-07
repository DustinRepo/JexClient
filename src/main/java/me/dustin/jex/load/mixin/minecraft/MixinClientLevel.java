package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.world.EventSpawnEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class MixinClientLevel {

    @Inject(method = "addEntity", at = @At("HEAD"))
    public void addEntityPrivate1(int id, Entity entity, CallbackInfo ci) {
        new EventSpawnEntity(entity).run();
    }

}
