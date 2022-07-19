package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.render.EventBindShader;
import net.minecraft.client.render.Shader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Shader.class)
public class MixinShader {

    @Inject(method = "bind", at = @At("HEAD"), cancellable = true)
    public void bindPre(CallbackInfo ci) {
        EventBindShader eventBindShader = new EventBindShader((Shader)(Object)this, EventBindShader.Mode.PRE).run();
        if (eventBindShader.isCancelled())
            ci.cancel();
    }

    @Inject(method = "bind", at = @At("RETURN"))
    public void bindPost(CallbackInfo ci) {
        new EventBindShader((Shader)(Object)this, EventBindShader.Mode.POST).run();
    }

}
