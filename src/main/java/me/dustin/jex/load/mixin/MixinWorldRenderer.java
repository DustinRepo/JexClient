package me.dustin.jex.load.mixin;

import me.dustin.jex.event.render.EventRenderRain;
import me.dustin.jex.load.impl.IWorldRenderer;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.impl.render.esp.ESP;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer implements IWorldRenderer {

    @Shadow
    @Final
    private BufferBuilderStorage bufferBuilders;

    private Identifier my_outline = new Identifier("jex", "shaders/entity_outline.json");

    @Redirect(method = "loadEntityOutlineShader", at = @At(value = "NEW", target = "net/minecraft/util/Identifier"))
    public Identifier getIDForOutline(String id) {
        if (Module.get(ESP.class).getState() && ((ESP) Module.get(ESP.class)).mode.equalsIgnoreCase("Shader")) {
            return my_outline;
        }
        return new Identifier("shaders/post/entity_outline.json");
    }

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    public void renderWeather(LightmapTextureManager manager, float f, double d, double e, double g, CallbackInfo ci) {
        EventRenderRain eventRenderRain = new EventRenderRain().run();
        if (eventRenderRain.isCancelled())
            ci.cancel();
    }

    @Override
    public BufferBuilderStorage getBufferBuilders() {
        return this.bufferBuilders;
    }
}
