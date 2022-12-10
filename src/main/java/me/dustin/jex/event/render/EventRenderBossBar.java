package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.text.Text;
import me.dustin.events.core.Event;
import net.minecraft.client.util.math.MatrixStack;

public class EventRenderBossBar extends Event {

    private final BossBarHud bossbar;
    private final ClientBossBar clientbossbar;
     private final MatrixStack poseStack;

    public EventRenderBossBar(BossBarHud bossbar, MatrixStack poseStack) {
        this.bossbar = bossbar;
        this.poseStack= poseStack;
        this.clientbossbar = clientbossbar;
    }

public BossBarHud getBossBarHud() {
        return bossbar;
    }
    
    public ClientBossBar getClientBossBar() {
        return bossbar;
    }
    
  public MatrixStack getPoseStack() {
        return poseStack;
    }
    
}
