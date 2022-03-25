package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.misc.EventControlLlama;
import net.minecraft.entity.passive.LlamaEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LlamaEntity.class)
public class MixinLlamaEntity {

    @Inject(method = "canBeSaddled", at = @At("HEAD"), cancellable = true)
    public void canBeControlledByRider(CallbackInfoReturnable<Boolean> ci) {
        EventControlLlama eventControlLlama = new EventControlLlama().run();
        if (eventControlLlama.isCancelled())
            ci.setReturnValue(eventControlLlama.isControl());
    }

}
