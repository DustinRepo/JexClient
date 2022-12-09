package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.text.Text;
import me.dustin.events.core.Event;
import net.minecraft.client.util.math.MatrixStack;

public class EventRenderBossBar extends Event {

    private final BossBarHud bossbarhud;
     private final MatrixStack poseStack;

    public EventRenderBossBar(BossBarHud bossbarhud, MatrixStack poseStack) {
        this.bossbarhud = bossbarhud;
        this.poseStack= poseStack;
    }

public BossBarHud getBossBarHud() {
        return bossbarhud;
    }
    
  public MatrixStack getPoseStack() {
        return poseStack;
    }
    
}
