package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.entity.boss.BossBar;

public class EventRenderBossBar extends Event {
    private final BossBar bossBar;

    public EventRenderBossBar(BossBar bossBar) {
	this.bossBar = bossBar;
    }
	public BossBar getBossBar() {
        return this.bossBar;
    }
}
