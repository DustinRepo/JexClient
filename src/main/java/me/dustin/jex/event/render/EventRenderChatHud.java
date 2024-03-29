package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.util.math.MatrixStack;

public class EventRenderChatHud extends Event {
    private final ChatHud chatHud;
    private final MatrixStack poseStack;
    private final int tickDelta;

    public EventRenderChatHud(ChatHud chatHud, MatrixStack poseStack, int tickDelta) {
        this.chatHud = chatHud;
        this.poseStack = poseStack;
        this.tickDelta = tickDelta;
    }

    public ChatHud getChatHud() {
        return chatHud;
    }

    public MatrixStack getPoseStack() {
        return poseStack;
    }

    public int getTickDelta() {
        return tickDelta;
    }
}
