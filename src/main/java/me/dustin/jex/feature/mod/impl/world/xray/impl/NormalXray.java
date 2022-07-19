package me.dustin.jex.feature.mod.impl.world.xray.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.render.*;
import me.dustin.jex.feature.mod.core.FeatureExtension;
import me.dustin.jex.feature.mod.impl.world.xray.Xray;
import me.dustin.jex.helper.world.WorldHelper;

public class NormalXray extends FeatureExtension {

    public NormalXray() {
        super(Xray.Mode.NORMAL, Xray.class);
    }

    @Override
    public void pass(Event event) {
        if (event instanceof EventMarkChunkClosed eventMarkChunkClosed) {
            eventMarkChunkClosed.cancel();
        } else if (event instanceof EventBlockBrightness eventBlockBrightness) {
            eventBlockBrightness.setBrightness(15);
        } else if (event instanceof EventRenderBlockEntity eventRenderBlockEntity) {
            if (!Xray.INSTANCE.isValid(WorldHelper.INSTANCE.getBlock(eventRenderBlockEntity.blockEntity.getPos())))
                event.cancel();
        } else if (event instanceof EventRenderBlock eventRenderBlock) {
            if (!Xray.INSTANCE.isValid(eventRenderBlock.block))
                event.cancel();
        } else if (event instanceof EventRenderFluid eventRenderFluid) {
            if (!Xray.INSTANCE.isValid(eventRenderFluid.getBlock()))
                event.cancel();
        } else if (event instanceof EventShouldDrawSide eventShouldDrawSide) {
            if (Xray.INSTANCE.isValid(eventShouldDrawSide.getBlock()))
                eventShouldDrawSide.setShouldDrawSide(Xray.INSTANCE.shouldDrawSide(eventShouldDrawSide.getSide(), eventShouldDrawSide.getBlockPos()));
            else eventShouldDrawSide.setShouldDrawSide(false);
            eventShouldDrawSide.cancel();
        }
    }
}
