package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.gui.hud.ClientBossBar;

public class EventRenderBossBar extends Event {
    private final ClientBossBar bossBar;

    public EventRenderBossBar(ClientBossBar bossBar) {
	this.bossBar = bossBar;
    }
	public ClientBossBar getBossBar() {
        return this.bossBar;
    }
}
