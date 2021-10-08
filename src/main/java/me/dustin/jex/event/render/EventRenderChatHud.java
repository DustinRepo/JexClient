package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.util.math.MatrixStack;

public class EventRenderChatHud extends Event {
    private ChatHud chatHud;
    private MatrixStack matrixStack;
    private int tickDelta;

    public EventRenderChatHud(ChatHud chatHud, MatrixStack matrixStack, int tickDelta) {
        this.chatHud = chatHud;
        this.matrixStack = matrixStack;
        this.tickDelta = tickDelta;
    }

    public ChatHud getChatHud() {
        return chatHud;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public int getTickDelta() {
        return tickDelta;
    }
}
