package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
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
        if (!(Wrapper.INSTANCE.getMinecraft().currentScreen instanceof DisconnectedScreen) || serverAddress == null)
            timer.reset();
    });

    @EventPointer
    private final EventListener<EventSetScreen> eventSetScreenEventListener = new EventListener<>(event -> {
        if (event.getScreen() instanceof DisconnectedScreen)
            timer.reset();
    });

    @EventPointer
    private final EventListener<EventConnect> eventConnectEventListener = new EventListener<>(event -> {
        this.serverAddress = event.getServerAddress();
    });

    @EventPointer
    private final EventListener<EventDrawScreen> eventDrawScreenEventListener = new EventListener<>(event -> {
        if (event.getMode() == EventDrawScreen.Mode.POST && Wrapper.INSTANCE.getMinecraft().currentScreen instanceof DisconnectedScreen) {
            float timeLeft = (timer.getLastMS() + delay) - timer.getCurrentMS();
            timeLeft /= 1000;
            String messageString = String.format("Reconnecting in %.1fs", timeLeft);
            FontHelper.INSTANCE.drawCenteredString(event.getMatrixStack(), messageString, Wrapper.INSTANCE.getWindow().getScaledWidth() / 2.f, 2, ColorHelper.INSTANCE.getClientColor());

            if (timer.hasPassed(delay)) {
                connect();
                timer.reset();
            }
        }
    });

    public void connect() {
        ConnectScreen.connect(new MultiplayerScreen(new JexTitleScreen()), Wrapper.INSTANCE.getMinecraft(), serverAddress, null);
    }
}
