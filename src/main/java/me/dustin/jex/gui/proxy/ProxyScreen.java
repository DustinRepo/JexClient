package me.dustin.jex.gui.proxy;

import com.google.common.net.HostAndPort;
import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.ProxyHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;

public class ProxyScreen extends Screen {

    private Button proxyTypeButton;
    private Button connectButton;
    private Button disconnectButton;
    private EditBox proxyField;
    private EditBox usernameField;
    private EditBox passwordField;
    private boolean socks5 = true;

    public ProxyScreen() {
        super(Component.nullToEmpty("Proxy"));
    }

    @Override
    protected void init() {
        this.addRenderableWidget(proxyTypeButton = new Button(width / 2 - 100, height / 2 - 60, 200, 20, Component.nullToEmpty("Proxy: SOCKS5"), button -> {
            this.socks5 = !this.socks5;
            proxyTypeButton.setMessage(Component.nullToEmpty("Proxy: " + (socks5 ? "SOCKS5" : "SOCKS4")));
        }));
        String currentProxyString = "";
        if (ProxyHelper.INSTANCE.isConnectedToProxy()) {
            ProxyHelper.ClientProxy proxy = ProxyHelper.INSTANCE.getProxy();
            currentProxyString = proxy.host() + ":" + proxy.port();
        }
        this.addWidget(proxyField = new EditBox(Wrapper.INSTANCE.getTextRenderer(), width / 2 - 100, height / 2 - 25, 200, 20, Component.nullToEmpty(currentProxyString)));
        this.addWidget(usernameField = new EditBox(Wrapper.INSTANCE.getTextRenderer(), width / 2 - 100, height / 2, 200, 20, Component.nullToEmpty("")));
        this.addWidget(passwordField = new EditBox(Wrapper.INSTANCE.getTextRenderer(), width / 2 - 100, height / 2 + 25, 200, 20, Component.nullToEmpty("")));
        this.addRenderableWidget(connectButton = new Button(width / 2 - 100, height / 2 + 50, 200, 20, Component.nullToEmpty("Connect to Proxy"), button -> {
            HostAndPort hostAndPort = HostAndPort.fromString(proxyField.getValue());
            ProxyHelper.INSTANCE.connectToProxy(this.socks5 ? ProxyHelper.SocksType.FIVE : ProxyHelper.SocksType.FOUR, hostAndPort.getHost(), hostAndPort.getPort(), usernameField.getValue(), passwordField.getValue());
            Wrapper.INSTANCE.getMinecraft().setScreen(new JoinMultiplayerScreen(new TitleScreen()));
        }));
        this.addRenderableWidget(disconnectButton = new Button(width / 2 - 100, height / 2 + 75, 200, 20, Component.nullToEmpty("Disconnect from Proxy"), button -> {
            ProxyHelper.INSTANCE.disconnectFromProxy();
        }));
        this.addRenderableWidget(new Button(width / 2 - 100, height / 2 + 100, 200, 20, Component.nullToEmpty("Close"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new JoinMultiplayerScreen(new TitleScreen()));
        }));
        super.init();
    }

    @Override
    public void tick() {
        proxyField.tick();
        usernameField.tick();
        passwordField.tick();
        super.tick();
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        connectButton.active = ServerAddress.isValidAddress(proxyField.getValue()) && proxyField.getValue().contains(":");
        disconnectButton.active = ProxyHelper.INSTANCE.isConnectedToProxy();
        proxyField.render(matrices, mouseX, mouseY, delta);
        usernameField.render(matrices, mouseX, mouseY, delta);
        passwordField.render(matrices, mouseX, mouseY, delta);
        if (usernameField.getValue().isEmpty() && !usernameField.isFocused()) {
            FontHelper.INSTANCE.drawCenteredString(matrices, "Optional Username (SOCKS5 only)", width / 2.f, height / 2.f + 6, 0xff696969);
        }
        if (passwordField.getValue().isEmpty() && !passwordField.isFocused()) {
            FontHelper.INSTANCE.drawCenteredString(matrices, "Optional Password (SOCKS5 only)", width / 2.f, height / 2.f + 31, 0xff696969);
        }
        FontHelper.INSTANCE.drawCenteredString(matrices, "Proxy hostname:port", width / 2.f, height / 2.f - 38, -1);
        if (ProxyHelper.INSTANCE.isConnectedToProxy()) {
            ProxyHelper.ClientProxy proxy = ProxyHelper.INSTANCE.getProxy();
            FontHelper.INSTANCE.drawCenteredString(matrices, "Current Proxy: " + proxy.host() + ":" + proxy.port(), width / 2.f, height / 2.f - 70, -1);
        }
        super.render(matrices, mouseX, mouseY, delta);
    }
}
