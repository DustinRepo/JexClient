package me.dustin.jex.gui.minecraft;

import com.google.common.net.HostAndPort;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.ProxyHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class ProxyScreen extends Screen {

    private ButtonWidget proxyTypeButton;
    private ButtonWidget connectButton;
    private TextFieldWidget proxyField;
    private boolean socks5 = true;

    public ProxyScreen() {
        super(new LiteralText("Proxy"));
    }

    @Override
    protected void init() {
        this.addDrawableChild(proxyTypeButton = new ButtonWidget(width / 2 - 100, height / 2 - 60, 200, 20, new LiteralText("Proxy: SOCKS5"), button -> {
            this.socks5 = !this.socks5;
            proxyTypeButton.setMessage(new LiteralText("Proxy: " + (socks5 ? "SOCKS5" : "SOCKS4")));
        }));
        String currentProxyString = "";
        if (ProxyHelper.INSTANCE.isConnectedToProxy()) {
            ProxyHelper.ClientProxy proxy = ProxyHelper.INSTANCE.getProxy();
            currentProxyString = proxy.getHost() + ":" + proxy.getPort();
        }
        this.addSelectableChild(proxyField = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), width / 2 - 100, height / 2 - 25, 200, 20, new LiteralText(currentProxyString)));
        this.addDrawableChild(connectButton = new ButtonWidget(width / 2 - 100, height / 2, 200, 20, new LiteralText("Connect to Proxy"), button -> {
            HostAndPort hostAndPort = HostAndPort.fromString(proxyField.getText());
            ProxyHelper.INSTANCE.connectToProxy(this.socks5 ? ProxyHelper.SocksType.FIVE : ProxyHelper.SocksType.FOUR, hostAndPort.getHost(), hostAndPort.getPort());
            Wrapper.INSTANCE.getMinecraft().setScreen(new MultiplayerScreen(new TitleScreen()));
        }));
        this.addDrawableChild(new ButtonWidget(width / 2 - 100, height / 2 + 25, 200, 20, new LiteralText("Disconnect from Proxy"), button -> {
            ProxyHelper.INSTANCE.disconnectFromProxy();
        }));
        this.addDrawableChild(new ButtonWidget(width / 2 - 100, height / 2 + 50, 200, 20, new LiteralText("Close"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new MultiplayerScreen(new TitleScreen()));
        }));
        super.init();
    }

    @Override
    public void tick() {
        proxyField.tick();
        super.tick();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        connectButton.active = ServerAddress.isValid(proxyField.getText()) && proxyField.getText().contains(":");
        proxyField.render(matrices, mouseX, mouseY, delta);
        FontHelper.INSTANCE.drawCenteredString(matrices, "Proxy hostname:port", width / 2.f, height / 2.f - 38, -1);
        if (ProxyHelper.INSTANCE.isConnectedToProxy()) {
            ProxyHelper.ClientProxy proxy = ProxyHelper.INSTANCE.getProxy();
            FontHelper.INSTANCE.drawCenteredString(matrices, "Current Proxy: " + proxy.getHost() + ":" + proxy.getPort(), width / 2.f, height / 2.f - 70, -1);
        }
        super.render(matrices, mouseX, mouseY, delta);
    }
}
