package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.text.Text;
import java.util.Iterator;

package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.util.math.MatrixStack;

public class EventRenderCrosshair extends Event {

    private final MatrixStack poseStack;

    public EventRenderBossBar(ClientBossBar clientbossbar, MatrixStack matrixStack,BossBarHud bossbarhud) {
        this.poseStack = poseStack;
        this.bossbarhud = bossbarhud;
        this.clientbossbar = clientbossbar;
    }

    public MatrixStack getPoseStack() {
        return this.poseStack;
    }
 
public ClientBossBar getClientBossBar() {
        return this.clientbossbar;
    }
 
 public BossBarHud getBossBarHud() {
        return this.bossbarhud;
    }
 
}
