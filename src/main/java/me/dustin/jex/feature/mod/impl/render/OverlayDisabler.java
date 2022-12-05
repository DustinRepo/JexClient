package me.dustin.jex.feature.mod.impl.render;

import me.dustin.jex.feature.property.Property;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import net.minecraft.client.gui.hud.*;
import me.dustin.events.core.EventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.entity.boss.BossBar;

public class OverlayDisabler extends Feature {
 
  public final Property<Boolean> bossbarProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("BossBar")
            .value(true)
            .build();
			
     public OverlayDisabler() {
        super(Category.VISUAL, "Removes unnecessary interface elements");
    }
	
    public void onRender(BossBar bossbar, CallbackInfo info) {
        if (bossbarProperty.value()) {
            bossbar.setCanceled(true);
        }
    }
	
}
