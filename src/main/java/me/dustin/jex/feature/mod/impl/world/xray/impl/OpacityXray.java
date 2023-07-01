package me.dustin.jex.feature.mod.impl.world.xray.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.render.*;
import me.dustin.jex.feature.mod.core.FeatureExtension;
import me.dustin.jex.feature.mod.impl.world.xray.Xray;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderLayer;

public class OpacityXray extends FeatureExtension {

    public OpacityXray() {
        super(Xray.Mode.OPACITY, Xray.class);
    }

    @Override
    public void pass(Event event) {
        if (event instanceof EventMarkChunkClosed eventMarkChunkClosed) {
            eventMarkChunkClosed.cancel();
        } else if (event instanceof EventBlockBrightness eventBlockBrightness) {
            eventBlockBrightness.setBrightness(15);
        } else if (event instanceof EventGetRenderType eventGetRenderType) {
            if (!Xray.INSTANCE.isValid(eventGetRenderType.getState().getBlock())) {
                eventGetRenderType.setRenderType(RenderLayer.getTranslucent());
                eventGetRenderType.cancel();
            }
        } else if (event instanceof EventShouldDrawSide eventShouldDrawSide) {
            if (Xray.INSTANCE.isValid(eventShouldDrawSide.getBlock())) {
                eventShouldDrawSide.setShouldDrawSide(Xray.INSTANCE.shouldDrawSide(eventShouldDrawSide.getSide(), eventShouldDrawSide.getBlockPos()));
                event.cancel();
            }
        } else if (event instanceof EventWorldRender eventWorldRender && eventWorldRender.getMode() == EventWorldRender.Mode.PRE) {
            ShaderHelper.INSTANCE.getOpacityXrayShader().setUpdateUniforms(() -> {
                ShaderHelper.INSTANCE.getOpacityXrayShader().getUniform("Alpha").setFloat(Xray.INSTANCE.alphaProperty.value() / 100.f);
            });
        } else if (event instanceof EventBindShader eventBindShader) {
            if (eventBindShader.getShader() == GameRenderer.getRenderTypeTranslucentShader() && eventBindShader.getMode() == EventBindShader.Mode.POST) {
                ShaderHelper.INSTANCE.getOpacityXrayShader().bind();
            }
        } else if (event instanceof EventSodiumQuadAlpha eventSodiumQuadAlpha) {
            eventSodiumQuadAlpha.setAlpha((int)(Xray.INSTANCE.alphaProperty.value() * 2.55f));
        }
    }
}
