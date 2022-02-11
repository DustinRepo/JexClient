package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.DrawScreenFilter;
import me.dustin.jex.event.filters.SetScreenFilter;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventSetScreen;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.packet.EventConnect;
import me.dustin.jex.event.render.EventDrawScreen;
import me.dustin.jex.gui.minecraft.JexTitleScreen;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;

@Feature.Manifest(category = Feature.Category.MISC, description = "Reconnect automatically.")
public class AutoReconnect extends Feature {

    @Op(name = "Delay", min = 1000, max = 20000, inc = 500)
    public int delay = 5000;

    public Timer timer = new Timer();
    private ServerAddress serverAddress;

    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
        if (timer.hasPassed(delay) && Wrapper.INSTANCE.getMinecraft().currentScreen instanceof DisconnectedScreen) {
            connect();
            timer.reset();
        }
    }, new TickFilter(EventTick.Mode.POST));

    @EventPointer
    private final EventListener<EventSetScreen> eventSetScreenEventListener = new EventListener<>(event -> {
        timer.reset();
    }, new SetScreenFilter(DisconnectedScreen.class));

    @EventPointer
    private final EventListener<EventConnect> eventConnectEventListener = new EventListener<>(event -> {
        this.serverAddress = event.getServerAddress();
    });

    @EventPointer
    private final EventListener<EventDrawScreen> eventDrawScreenEventListener = new EventListener<>(event -> {
        float timeLeft = (timer.getLastMS() + delay) - timer.getCurrentMS();
        timeLeft /= 1000;
        String messageString = String.format("Reconnecting in %.1fs", timeLeft);
        FontHelper.INSTANCE.drawCenteredString(event.getMatrixStack(), messageString, Wrapper.INSTANCE.getWindow().getScaledWidth() / 2.f, 2, ColorHelper.INSTANCE.getClientColor());
    }, new DrawScreenFilter(EventDrawScreen.Mode.POST, DisconnectedScreen.class));

    public void connect() {
        ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), Wrapper.INSTANCE.getMinecraft(), serverAddress, null);
    }

    public ServerAddress getServerAddress() {
        return serverAddress;
    }
}
