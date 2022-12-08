package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.gui.hud.BossBarHud;

public class EventRenderBossBar extends Event {
    private final BossBarHud bossBar;

    public EventRenderBossBar(BossBarHud bossBar) {
	this.bossBar = bossBar;
    }
	public BossBarHud getBossBar() {
        return this.bossBar;
    }
}
