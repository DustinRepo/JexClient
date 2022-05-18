package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

public class EventDrawScreen extends Event {

	private final Screen screen;
	private final MatrixStack poseStack;
	private final Mode mode;

	public EventDrawScreen(Screen screen, MatrixStack poseStack, Mode mode) {
		this.screen = screen;
		this.poseStack = poseStack;
		this.mode = mode;
	}

	public Screen getScreen() {
		return screen;
	}

	public MatrixStack getPoseStack() {
		return poseStack;
	}

	public Mode getMode() {
		return mode;
	}

	public enum Mode {
		PRE, POST, POST_CONTAINER
	}
}
