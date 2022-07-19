package me.dustin.jex.load.mixin.sodium;

import me.jellysquid.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = ShaderChunkRenderer.class, remap = false)
public class MixinShaderChunkRenderer {
    @ModifyArg(method = "compileProgram", at = @At(value = "INVOKE", target = "me/jellysquid/mods/sodium/client/render/chunk/ShaderChunkRenderer.createShader(Ljava/lang/String;Lme/jellysquid/mods/sodium/client/render/chunk/shader/ChunkShaderOptions;)Lme/jellysquid/mods/sodium/client/gl/shader/GlProgram;"), index = 0)
    public String getPath(String path) {
        return "opacity_xray";
    }
}
