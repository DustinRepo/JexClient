package me.dustin.jex.feature.impl.world.xray.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.Event;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.render.*;
import me.dustin.jex.extension.FeatureExtension;
import me.dustin.jex.feature.impl.world.xray.Xray;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import me.dustin.jex.helper.render.shader.ShaderUniform;
import me.dustin.jex.load.impl.IShader;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.RenderLayer;

public class OpacityXray extends FeatureExtension {

    public OpacityXray() {
        super("Opacity", Xray.class);
    }

    public static int alphaLocation = -1;

    private GlUniform vanillaUniform;
    private ShaderUniform sodiumUniform;

    @Override
    public void pass(Event event) {
        if (event instanceof EventMarkChunkClosed) {
            event.cancel();
        } else if (event instanceof EventSodiumBeginShader) {
            if (this.sodiumUniform == null) {
                this.sodiumUniform = new ShaderUniform("Alpha", alphaLocation);
            }
            updateAlpha();
        } else if (!Xray.INSTANCE.isSodiumLoaded() && event instanceof EventTick) {
            updateAlpha();
        } else if (event instanceof EventGetRenderLayer eventGetRenderLayer) {
            if (!Xray.INSTANCE.isValid(eventGetRenderLayer.getState().getBlock())) {
                eventGetRenderLayer.setRenderLayer(RenderLayer.getTranslucent());
                event.cancel();
            }
        } else if (!Xray.INSTANCE.isSodiumLoaded() && event instanceof EventGetTranslucentShader eventGetTranslucentShader) {
            eventGetTranslucentShader.setShader(ShaderHelper.getTranslucentShader());
            eventGetTranslucentShader.cancel();
        } else if (event instanceof EventBlockBrightness eventBlockBrightness) {
            if (Xray.INSTANCE.isValid(eventBlockBrightness.getBlock()))
                eventBlockBrightness.setBrightness(15);
        } else if (event instanceof EventShouldDrawSide eventShouldDrawSide) {
            if (Xray.INSTANCE.isValid(eventShouldDrawSide.getBlock())) {
                eventShouldDrawSide.setShouldDrawSide(Xray.INSTANCE.shouldDrawSide(eventShouldDrawSide.getSide(), eventShouldDrawSide.getBlockPos()));
                event.cancel();
            }
        }
    }

    @Override
    public void enable() {
        if (vanillaUniform == null && !Xray.INSTANCE.isSodiumLoaded()) {
            IShader iShader = (IShader) ShaderHelper.getTranslucentShader();
            if (iShader != null)
                vanillaUniform = iShader.getCustomUniform("Alpha");
        }
        Xray.INSTANCE.renderChunksSmooth();
        super.enable();
    }

    @Override
    public void disable() {
        super.disable();
        if (RenderSystem.isOnRenderThread()) {
            if (vanillaUniform != null)
                vanillaUniform.set(1.1f);
            else if (sodiumUniform != null)
                sodiumUniform.setFloat(1.1f);
        }
        Xray.INSTANCE.renderChunksSmooth();
    }

    private void updateAlpha() {
        if (sodiumUniform != null) {
            sodiumUniform.setFloat(Xray.INSTANCE.alphaValue);
        } else if (vanillaUniform != null) {
            vanillaUniform.set(Xray.INSTANCE.alphaValue);
        }
    }
}
