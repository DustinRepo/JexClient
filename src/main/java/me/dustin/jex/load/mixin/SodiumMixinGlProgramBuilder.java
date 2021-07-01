package me.dustin.jex.load.mixin;

import me.dustin.jex.feature.impl.world.Xray;
import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlProgram.Builder.class)
public class SodiumMixinGlProgramBuilder {

    @Shadow @Final private Identifier name;

    @Shadow @Final private int program;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void initBuilder(Identifier name, CallbackInfo ci) {
        if (this.name.getNamespace().equalsIgnoreCase("jex")) {
            Xray.sodiumShaderProgram = this.program;
        }
    }

}
