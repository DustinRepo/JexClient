package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.events.core.enums.EventPriority;
import me.dustin.jex.event.misc.EventDisplayScreen;
import me.dustin.jex.event.misc.EventJoinWorld;
import me.dustin.jex.event.player.EventSetPlayerHealth;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.gui.screen.DeathScreen;

@Feature.Manifest(category = Feature.Category.PLAYER, description = "Never accept death. Relog for godmode. Only works on vanilla/fabric")
public class Ghost extends Feature {

    @EventListener(events = {EventSetPlayerHealth.class, EventDisplayScreen.class, EventJoinWorld.class}, priority = EventPriority.HIGH)
    private void runMethod(Event event) {
        if (event instanceof EventSetPlayerHealth eventSetPlayerHealth) {
            if (eventSetPlayerHealth.getHealth() <= 0) {
                eventSetPlayerHealth.cancel();
                eventSetPlayerHealth.setHealth(1);
            }
        }
        else if (event instanceof EventDisplayScreen eventDisplayScreen) {
            if (eventDisplayScreen.getScreen() instanceof DeathScreen) {
                event.cancel();
                eventDisplayScreen.setScreen(null);
            }
        }
        else if (event instanceof EventJoinWorld eventJoinWorld) {
            if (Wrapper.INSTANCE.getLocalPlayer().getHealth() <= 0) {
                Wrapper.INSTANCE.getLocalPlayer().setHealth(1);
                Wrapper.INSTANCE.getMinecraft().openScreen(null);
            }
        }
    }

    @Override
    public void onDisable() {
        if (Wrapper.INSTANCE.getLocalPlayer() != null && Wrapper.INSTANCE.getLocalPlayer().getHealth() == 1)
            Wrapper.INSTANCE.getLocalPlayer().requestRespawn();
        super.onDisable();
    }
}
