package me.dustin.jex.load.mixin;

import me.dustin.jex.JexClient;
import me.dustin.jex.event.render.EventSodiumBeginShader;
import me.dustin.jex.feature.mod.impl.world.xray.impl.OpacityXray;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.gl.shader.*;
import me.jellysquid.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkProgram;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderBindingPoints;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = ShaderChunkRenderer.class, remap = false)
public class SodiumMixinShaderChunkRenderer {

    @Shadow protected ChunkProgram activeProgram;

    @Inject(method = "createShader", at = @At("HEAD"), cancellable = true)
    public void createShaderOverride(RenderDevice device, String path, ChunkShaderOptions options, CallbackInfoReturnable<ChunkProgram> cir) {
        if ("blocks/block_layer_translucent".equalsIgnoreCase(path)) {
            ShaderConstants constants = options.constants();
            GlShader vertShader = ShaderLoader.loadShader(ShaderType.VERTEX, new Identifier("sodium", path + ".vsh"), constants);
            GlShader fragShader = ShaderLoader.loadShader(ShaderType.FRAGMENT, new Identifier("jex", path + ".fsh"), constants);

            ChunkProgram var7;
            GlProgram builder;
            try {
                builder = GlProgram.builder(new Identifier("jex", "xray_shader")).attachShader(vertShader).attachShader(fragShader).bindAttribute("a_Pos", ChunkShaderBindingPoints.ATTRIBUTE_POSITION_ID).bindAttribute("a_Color", ChunkShaderBindingPoints.ATTRIBUTE_COLOR).bindAttribute("a_TexCoord", ChunkShaderBindingPoints.ATTRIBUTE_BLOCK_TEXTURE).bindAttribute("a_LightCoord", ChunkShaderBindingPoints.ATTRIBUTE_LIGHT_TEXTURE).bindFragmentData("fragColor", ChunkShaderBindingPoints.FRAG_COLOR).build((name) -> {
                    return new ChunkProgram(device, name, options);
                });
                var7 = (ChunkProgram) builder;
            } finally {
                vertShader.delete();
                fragShader.delete();
            }

            OpacityXray.alphaLocation = builder.getUniformLocation("Alpha");
            if (OpacityXray.alphaLocation < 0) {
                JexClient.INSTANCE.getLogger().error("Couldn't find Alpha uniform! " + OpacityXray.alphaLocation);
            }
            cir.setReturnValue(var7);
        }
    }

    @Inject(method = "begin", at = @At("RETURN"))
    public void beginEnd(BlockRenderPass pass, MatrixStack matrixStack, CallbackInfo ci) {
        new EventSodiumBeginShader().run();
    }
}
