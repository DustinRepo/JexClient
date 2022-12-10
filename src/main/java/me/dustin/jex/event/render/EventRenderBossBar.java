package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.text.Text;
import me.dustin.events.core.Event;

public class EventRenderBossBar extends Event {

    private final ClientBossBar clientbossbar;

    public EventRenderBossBar(ClientBossBar clientbossbar) {
        this.clientbossbar = clientbossbar;
    }
    
    public ClientBossBar getClientBossBar() {
        return clientbossbar;
    }
    
}
