package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventDisplayScreen;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.gui.screen.DeathScreen;

@Feature.Manifest(category = Feature.Category.PLAYER, description = "Never accept death.")
public class Ghost extends Feature {

    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof DeathScreen)
                Wrapper.INSTANCE.getMinecraft().openScreen(null);
            if (Wrapper.INSTANCE.getLocalPlayer().getHealth() == 0)
                Wrapper.INSTANCE.getLocalPlayer().setHealth(1);
        }
    }

    @Override
    public void onDisable() {
        if (Wrapper.INSTANCE.getLocalPlayer() != null && Wrapper.INSTANCE.getLocalPlayer().getHealth() == 1)
            Wrapper.INSTANCE.getLocalPlayer().requestRespawn();
        super.onDisable();
    }
}
