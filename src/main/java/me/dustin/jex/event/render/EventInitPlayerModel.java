package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.model.ModelPart;

public class EventInitPlayerModel extends Event {
    private final ModelPart root;

    public EventInitPlayerModel(ModelPart root) {
        this.root = root;
    }

    public ModelPart getRoot() {
        return root;
    }
}
