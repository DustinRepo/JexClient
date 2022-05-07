package me.dustin.jex.event.render;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.events.core.Event;
import net.minecraft.client.gui.screens.Screen;

public class EventDrawScreen extends Event {

	private final Screen screen;
	private final PoseStack poseStack;
	private final Mode mode;

	public EventDrawScreen(Screen screen, PoseStack poseStack, Mode mode) {
		this.screen = screen;
		this.poseStack = poseStack;
		this.mode = mode;
	}

	public Screen getScreen() {
		return screen;
	}

	public PoseStack getPoseStack() {
		return poseStack;
	}

	public Mode getMode() {
		return mode;
	}

	public enum Mode {
		PRE, POST, POST_CONTAINER
	}
}
