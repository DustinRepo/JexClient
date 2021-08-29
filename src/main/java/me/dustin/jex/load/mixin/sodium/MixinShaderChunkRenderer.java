package me.dustin.jex.load.mixin.sodium;

import me.dustin.jex.event.render.EventSodiumBeginShader;
import me.jellysquid.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = ShaderChunkRenderer.class, remap = false)
public class MixinShaderChunkRenderer {

    /*@Inject(method = "createShader", at = @At("HEAD"), cancellable = true)
    public void createShaderOverride(String path, ChunkShaderOptions options, CallbackInfoReturnable<GlProgram<ChunkShaderInterface>> cir) {
        if ("blocks/block_layer_translucent".equalsIgnoreCase(path)) {
            ShaderConstants constants = options.constants();
            GlShader vertShader = ShaderLoader.loadShader(ShaderType.VERTEX, new Identifier("sodium", path + ".vsh"), constants);
            GlShader fragShader = ShaderLoader.loadShader(ShaderType.FRAGMENT, new Identifier("jex", path + ".fsh"), constants);

            GlProgram<ChunkShaderInterface> var7;
            GlProgram<ChunkShaderInterface> builder;
            try {
                builder = GlProgram.builder(new Identifier("jex", "xray_shader")).attachShader(vertShader).attachShader(fragShader).bindAttribute("a_Pos", ChunkShaderBindingPoints.ATTRIBUTE_POSITION_ID).bindAttribute("a_Color", ChunkShaderBindingPoints.ATTRIBUTE_COLOR).bindAttribute("a_TexCoord", ChunkShaderBindingPoints.ATTRIBUTE_BLOCK_TEXTURE).bindAttribute("a_LightCoord", ChunkShaderBindingPoints.ATTRIBUTE_LIGHT_TEXTURE).bindFragmentData("fragColor", ChunkShaderBindingPoints.FRAG_COLOR).link((shader) -> {
                    return new ChunkShaderInterface(shader, options);
                });
                var7 = (GlProgram<ChunkShaderInterface>) builder;
            } finally {
                vertShader.delete();
                fragShader.delete();
            }

            OpacityXray.alphaLocation = builder.getInterface("Alpha");
            if (OpacityXray.alphaLocation < 0) {
                JexClient.INSTANCE.getLogger().error("Couldn't find Alpha uniform! " + OpacityXray.alphaLocation);
            }
            cir.setReturnValue(var7);

            ShaderConstants constants = options.constants();
            GlShader vertShader = ShaderLoader.loadShader(ShaderType.VERTEX, new class_2960("sodium", path + ".vsh"), constants);
            GlShader fragShader = ShaderLoader.loadShader(ShaderType.FRAGMENT, new class_2960("sodium", path + ".fsh"), constants);

            GlProgram var6;
            try {
                var6 = GlProgram.builder(new class_2960("sodium", "chunk_shader")).attachShader(vertShader).attachShader(fragShader).bindAttribute("a_Pos", ChunkShaderBindingPoints.ATTRIBUTE_POSITION_ID).bindAttribute("a_Color", ChunkShaderBindingPoints.ATTRIBUTE_COLOR).bindAttribute("a_TexCoord", ChunkShaderBindingPoints.ATTRIBUTE_BLOCK_TEXTURE).bindAttribute("a_LightCoord", ChunkShaderBindingPoints.ATTRIBUTE_LIGHT_TEXTURE).bindFragmentData("fragColor", ChunkShaderBindingPoints.FRAG_COLOR).link((shader) -> {
                    return new ChunkShaderInterface(shader, options);
                });
            } finally {
                vertShader.delete();
                fragShader.delete();
            }

            return var6;
        }
    }*/

    @Inject(method = "begin", at = @At("RETURN"))
    public void beginEnd(BlockRenderPass pass, CallbackInfo ci) {
        new EventSodiumBeginShader().run();
    }
}
