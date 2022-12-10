package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.text.Text;
import me.dustin.events.core.Event;

public class EventRenderBossBar extends Event {

    private final BossBarHud bossbar;
    private final ClientBossBar clientbossbar;

    public EventRenderBossBar(BossBarHud bossbar, ClientBossBar clientbossbar) {
        this.bossbar = bossbar;
        this.clientbossbar = clientbossbar;
    }

public BossBarHud getBossBarHud() {
        return bossbar;
    }
    
    public ClientBossBar getClientBossBar() {
        return bossbar;
    }
    
}
