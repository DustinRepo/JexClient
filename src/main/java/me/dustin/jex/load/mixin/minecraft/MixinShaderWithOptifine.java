package me.dustin.jex.load.mixin.minecraft;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.JexClient;
import me.dustin.jex.load.impl.IShader;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.Shader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;

@Mixin(Shader.class)
public class MixinShaderWithOptifine implements IShader {

    @Shadow
    @Final
    private String name;

    private Map<String, GlUniform> customUniforms = Maps.newHashMap();

    //fuck optifabric, this method needs to be static with it, but not without
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "net/minecraft/util/Identifier.<init>(Ljava/lang/String;)V"), index = 0)
    private static String renameID(String originalID) {
        if (originalID.contains("jex:")) {
            //remove original one with the id split in the middle of the name
            String newString = originalID.replace("jex:","");
            //and put it back at from
            return "jex:" + newString;
        }
        return originalID;
    }

    @ModifyArg(method = "loadProgram", at = @At(value = "INVOKE", target = "net/minecraft/util/Identifier.<init>(Ljava/lang/String;)V"), index = 0)
    private static String renameIDOfHelpers(String originalID) {
        if (originalID.contains("jex:")) {
            //remove original one with the id split in the middle of the name
            String newString = originalID.replace("jex:","");
            //and put it back at from
            JexClient.INSTANCE.getLogger().info("Loading shader jex:" + newString);
            return "jex:" + newString;
        }
        return originalID;
    }

    @ModifyArg(method = "addUniform", at = @At(value = "INVOKE", target = "java/util/List.add(Ljava/lang/Object;)Z"))
    public Object renameIDOfHelpers(Object orig) {
        if (orig instanceof GlUniform glUniform && this.name.contains("jex:")) {
            customUniforms.put(glUniform.getName(), glUniform);
        }
        return orig;
    }

    @Override
    public GlUniform getCustomUniform(String name) {
        RenderSystem.assertOnRenderThread();
        return this.customUniforms.get(name);
    }
}
