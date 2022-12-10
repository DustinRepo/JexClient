package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.Text;
import me.dustin.events.core.Event;
import net.minecraft.client.util.math.MatrixStack;

public class EventRenderBoss extends Event {

    private final BossBar boss;
    private final MatrixStack poseStack;

    public EventRenderBossBar(BossBar boss, MatrixStack poseStack) {
        this.poseStack = poseStack;
        this.boss = boss;
    }

public BossBar getBoss() {
        return boss;
    }    
    
  public MatrixStack getPoseStack() {
        return poseStack;
    }
    
}
