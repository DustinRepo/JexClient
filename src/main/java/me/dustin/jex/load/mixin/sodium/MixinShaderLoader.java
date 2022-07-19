package me.dustin.jex.load.mixin.sodium;

import me.dustin.jex.JexClient;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderLoader;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ShaderLoader.class, remap = false)
public class MixinShaderLoader {

    @Inject(method = "getShaderSource", at = @At("RETURN"), cancellable = true)
    private static void getShaderSource(Identifier name, CallbackInfoReturnable<String> cir) {
        /*if (name.getPath().contains(".vsh")) {
            JexClient.INSTANCE.getLogger().info(name.getPath());
            JexClient.INSTANCE.getLogger().info(cir.getReturnValue());
        }*/
    }

}
