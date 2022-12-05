package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
 
 public class EventRenderChatHud extends Event {
    private final InGameHud ingamehud;
    private final MatrixStack poseStack;
    private final int tickDelta;
	
	public EventRenderHud(InGameHud ingamehud, MatrixStack poseStack, int tickDelta) {
        this.ingamehud = ingamehud;
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
