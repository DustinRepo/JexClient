package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.boss.BossBar;

public class EventRenderBossBar extends Event {
    private final BossBar bossBar;
    private final MatrixStack poseStack;

    public EventRenderBossBar(MatrixStack poseStack, BossBar bossBar) {
        this.poseStack = poseStack;
		this.bossbar = bossBar;
    }

    public MatrixStack getPoseStack() {
        return this.poseStack;
    }
	public BossBar getBossBar() {
        return this.bossBar;
    }

}
