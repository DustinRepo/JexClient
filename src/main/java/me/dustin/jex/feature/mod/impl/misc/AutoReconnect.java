package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventJoinWorld;
import me.dustin.jex.event.misc.EventSetScreen;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.render.EventDrawScreen;
import me.dustin.jex.gui.minecraft.JexTitleScreen;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

@Feature.Manifest(category = Feature.Category.MISC, description = "Reconnect automatically.")
public class AutoReconnect extends Feature {

    @Op(name = "Delay", min = 1000, max = 20000, inc = 500)
    public int delay = 5000;

    public Timer timer = new Timer();
    private ServerInfo serverEntry;

    @EventListener(events = {EventTick.class, EventSetScreen.class, EventJoinWorld.class, EventDrawScreen.class})
    private void runMethod(Event event) {
        if (event instanceof EventTick) {
            if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof DisconnectedScreen && serverEntry != null) {
                if (timer.hasPassed(delay)) {
                    connect();
                    timer.reset();
                }
            } else
                timer.reset();
        } else if (event instanceof EventSetScreen eventSetScreen) {
            if (eventSetScreen.getScreen() instanceof DisconnectedScreen)
                timer.reset();
        } else if (event instanceof EventJoinWorld) {
            if (Wrapper.INSTANCE.getMinecraft().getCurrentServerEntry() != null) {
                this.serverEntry = Wrapper.INSTANCE.getMinecraft().getCurrentServerEntry();
            }
        } else if (event instanceof EventDrawScreen eventDrawScreen) {
            if (eventDrawScreen.getMode() == EventDrawScreen.Mode.POST && Wrapper.INSTANCE.getMinecraft().currentScreen instanceof DisconnectedScreen) {
                float timeLeft = (timer.getLastMS() + delay) - timer.getCurrentMS();
                timeLeft /= 1000;
                String messageString = String.format("\247aReconnecting in %.1fs", timeLeft);
                FontHelper.INSTANCE.drawCenteredString(eventDrawScreen.getMatrixStack(), messageString, Wrapper.INSTANCE.getWindow().getScaledWidth() / 2.f, 2, -1);
            }
        }
    }

    public void connect() {
        String address = serverEntry.address;
        int port = 25565;
        if (address.contains(":")) {
            address = address.split(":")[0];
            port = Integer.parseInt(address.split(":")[1]);
        }
        ConnectScreen.connect(new MultiplayerScreen(new JexTitleScreen()), Wrapper.INSTANCE.getMinecraft(), new ServerAddress(address, port), serverEntry);
    }

}
