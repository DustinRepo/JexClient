package me.dustin.jex.feature.mod.impl.render;

import me.dustin.jex.feature.property.Property;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.client.gui.hud.*;
import me.dustin.events.core.EventListener;
import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.event.render.EventRenderHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.boss.BossBar;
import java.util.HashMap;
import java.util.WeakHashMap;

public class OverlayDisabler extends Feature {
	
public static final WeakHashMap<ClientBossBar, Integer> barMap = new WeakHashMap<>();
			
     public OverlayDisabler() {
        super(Category.VISUAL, "Removes unnecessary interface elements");
    }
public final EventListener<EventRenderHud> eventRenderHudEventListener = new EventListener<>(event -> {
	public ClientBossBar {
	event.cancel();
	}
	)};	
}
