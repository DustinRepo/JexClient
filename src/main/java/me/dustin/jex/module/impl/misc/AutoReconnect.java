package me.dustin.jex.module.impl.misc;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventDisplayScreen;
import me.dustin.jex.event.misc.EventJoinWorld;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.render.EventDrawScreen;
import me.dustin.jex.gui.minecraft.JexTitleScreen;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.FontHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerInfo;

@ModClass(name = "AutoReconnect", category = ModCategory.MISC, description = "Reconnect automatically.")
public class AutoReconnect extends Module {

    @Op(name = "Delay", min = 1000, max = 20000, inc = 500)
    public int delay = 5000;

    public Timer timer = new Timer();
    private ServerInfo serverEntry;

    @EventListener(events = {EventTick.class, EventDisplayScreen.class, EventJoinWorld.class, EventDrawScreen.class})
    private void runMethod(Event event) {
        if (event instanceof EventTick) {
            if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof DisconnectedScreen && serverEntry != null) {
                if (timer.hasPassed(delay)) {
                    connect();
                    timer.reset();
                }
            } else
                timer.reset();
        } else if (event instanceof EventDisplayScreen) {
            if (((EventDisplayScreen) event).getScreen() instanceof DisconnectedScreen)
                timer.reset();
        } else if (event instanceof EventJoinWorld) {
            if (Wrapper.INSTANCE.getMinecraft().getCurrentServerEntry() != null) {
                this.serverEntry = Wrapper.INSTANCE.getMinecraft().getCurrentServerEntry();
            }
        } else if (event instanceof EventDrawScreen) {
            EventDrawScreen eventDrawScreen = (EventDrawScreen) event;
            if (eventDrawScreen.getMode() == EventDrawScreen.Mode.POST && Wrapper.INSTANCE.getMinecraft().currentScreen instanceof DisconnectedScreen) {
                float timeLeft = (timer.getLastMS() + delay) - timer.getCurrentMS();
                timeLeft /= 1000;
                String messageString = String.format("§aReconnecting in %.1fs", timeLeft);
                FontHelper.INSTANCE.drawCenteredString(eventDrawScreen.getMatrixStack(), messageString, Wrapper.INSTANCE.getWindow().getScaledWidth() / 2, 2, -1);
            }
        }
    }

    public void connect() {
        Wrapper.INSTANCE.getMinecraft().openScreen(new ConnectScreen(new MultiplayerScreen(new JexTitleScreen()), Wrapper.INSTANCE.getMinecraft(), serverEntry));
    }

}
