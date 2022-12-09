package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.text.Text;
import me.dustin.events.core.Event;
import net.minecraft.client.util.math.MatrixStack;

public class EventRenderBossBar extends Event {

    private final MatrixStack poseStack;

    public void EventRenderBossBar(MatrixStack matrixStack,BossBarHud bossbarhud) {
        this.matrixStack = matrixStack;
        this.bossbarhud = bossbarhud;
    }

    public MatrixStack getPoseStack() {
        return this.matrixStack;
    }
 
 public BossBarHud getBossBarHud() {
        return this.bossbarhud;
    }
 
}
