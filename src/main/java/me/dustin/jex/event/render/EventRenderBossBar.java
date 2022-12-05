package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.util.math.MatrixStack;

public class EventRenderBossBar extends Event {
    private final Bossbar bossBar;
    private final MatrixStack poseStack;

    public EventRenderBossBar(MatrixStack poseStack, Bossbar bossBar) {
        this.poseStack = poseStack;
		this.bossbar = bossBar;
    }

    public MatrixStack getPoseStack() {
        return this.poseStack;
    }
	public Bossbar getBossBar() {
        return this.bossBar;
    }

}