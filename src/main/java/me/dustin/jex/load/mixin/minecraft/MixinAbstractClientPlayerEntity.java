package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.player.EventGetSkinTexture;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public class MixinAbstractClientPlayerEntity {

    @Inject(method = "getSkinTexture", at = @At("HEAD"), cancellable = true)
    public void getSkinTexture(CallbackInfoReturnable<Identifier> cir) {
        EventGetSkinTexture eventGetSkinTexture = new EventGetSkinTexture((AbstractClientPlayerEntity)(Object)this, cir.getReturnValue()).run();
        if (eventGetSkinTexture.isCancelled())
            cir.setReturnValue(eventGetSkinTexture.getSkin());
    }

}
