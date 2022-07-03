package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;

import java.util.ArrayList;

public class EventPlayerEntityGetBodyParts extends Event {
    private final PlayerEntityModel<?> playerEntityModel;
    private final ArrayList<ModelPart> bodyParts;

    public EventPlayerEntityGetBodyParts(PlayerEntityModel<?> playerEntityModel, ArrayList<ModelPart> bodyParts) {
        this.playerEntityModel = playerEntityModel;
        this.bodyParts = bodyParts;
    }

    public ArrayList<ModelPart> getBodyParts() {
        return bodyParts;
    }

    public PlayerEntityModel<?> getPlayerEntityModel() {
        return playerEntityModel;
    }
}
