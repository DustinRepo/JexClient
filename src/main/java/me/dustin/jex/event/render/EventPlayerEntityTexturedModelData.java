package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;

public class EventPlayerEntityTexturedModelData extends Event {
    private final ModelData modelData;
    private final Dilation dilation;

    public EventPlayerEntityTexturedModelData(ModelData modelData, Dilation dilation) {
        this.modelData = modelData;
        this.dilation = dilation;
    }

    public ModelData getModelData() {
        return modelData;
    }

    public Dilation getDilation() {
        return dilation;
    }
}
