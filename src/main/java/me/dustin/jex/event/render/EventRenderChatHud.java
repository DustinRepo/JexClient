package me.dustin.jex.event.render;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.events.core.Event;
import net.minecraft.client.gui.components.ChatComponent;

public class EventRenderChatHud extends Event {
    private final ChatComponent chatHud;
    private final PoseStack poseStack;
    private final int tickDelta;

    public EventRenderChatHud(ChatComponent chatHud, PoseStack poseStack, int tickDelta) {
        this.chatHud = chatHud;
        this.poseStack = poseStack;
        this.tickDelta = tickDelta;
    }

    public ChatComponent getChatHud() {
        return chatHud;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public int getTickDelta() {
        return tickDelta;
    }
}
