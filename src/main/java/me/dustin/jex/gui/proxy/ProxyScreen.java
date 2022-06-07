package me.dustin.jex.gui.proxy;

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
import net.minecraft.text.Text;

public class ProxyScreen extends Screen {

    private ButtonWidget proxyTypeButton;
    private ButtonWidget connectButton;
    private ButtonWidget disconnectButton;
    private TextFieldWidget proxyField;
    private TextFieldWidget usernameField;
    private TextFieldWidget passwordField;
    private boolean socks5 = true;

    public ProxyScreen() {
        super(Text.translatable("jex.proxy"));
    }

    @Override
    protected void init() {
        this.addDrawableChild(proxyTypeButton = new ButtonWidget(width / 2 - 100, height / 2 - 60, 200, 20, socks5 ? Text.translatable("jex.proxy.socks5") : Text.translatable("jex.proxy.socks4"), button -> {
            this.socks5 = !this.socks5;
            proxyTypeButton.setMessage(socks5 ? Text.translatable("jex.proxy.socks5") : Text.translatable("jex.proxy.socks4"));
        }));
        String currentProxyString = "";
        if (ProxyHelper.INSTANCE.isConnectedToProxy()) {
            ProxyHelper.ClientProxy proxy = ProxyHelper.INSTANCE.getProxy();
            currentProxyString = proxy.host() + ":" + proxy.port();
        }
        this.addSelectableChild(proxyField = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), width / 2 - 100, height / 2 - 25, 200, 20, Text.of(currentProxyString)));
        this.addSelectableChild(usernameField = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), width / 2 - 100, height / 2, 200, 20, Text.of("")));
        this.addSelectableChild(passwordField = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), width / 2 - 100, height / 2 + 25, 200, 20, Text.of("")));
        this.addDrawableChild(connectButton = new ButtonWidget(width / 2 - 100, height / 2 + 50, 200, 20, Text.translatable("jex.proxy.connect"), button -> {
            HostAndPort hostAndPort = HostAndPort.fromString(proxyField.getText());
            ProxyHelper.INSTANCE.connectToProxy(this.socks5 ? ProxyHelper.SocksType.FIVE : ProxyHelper.SocksType.FOUR, hostAndPort.getHost(), hostAndPort.getPort(), usernameField.getText(), passwordField.getText());
            Wrapper.INSTANCE.getMinecraft().setScreen(new MultiplayerScreen(new TitleScreen()));
        }));
        this.addDrawableChild(disconnectButton = new ButtonWidget(width / 2 - 100, height / 2 + 75, 200, 20, Text.translatable("jex.proxy.disconnect"), button -> {
            ProxyHelper.INSTANCE.disconnectFromProxy();
        }));
        this.addDrawableChild(new ButtonWidget(width / 2 - 100, height / 2 + 100, 200, 20, Text.translatable("jex.button.done"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new MultiplayerScreen(new TitleScreen()));
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
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        connectButton.active = ServerAddress.isValid(proxyField.getText()) && proxyField.getText().contains(":");
        disconnectButton.active = ProxyHelper.INSTANCE.isConnectedToProxy();
        proxyField.render(matrices, mouseX, mouseY, delta);
        usernameField.render(matrices, mouseX, mouseY, delta);
        passwordField.render(matrices, mouseX, mouseY, delta);
        if (usernameField.getText().isEmpty() && !usernameField.isFocused()) {
            FontHelper.INSTANCE.drawCenteredString(matrices, Text.translatable("jex.proxy.username"), width / 2.f, height / 2.f + 6, 0xff696969);
        }
        if (passwordField.getText().isEmpty() && !passwordField.isFocused()) {
            FontHelper.INSTANCE.drawCenteredString(matrices, Text.translatable("jex.proxy.password"), width / 2.f, height / 2.f + 31, 0xff696969);
        }
        FontHelper.INSTANCE.drawCenteredString(matrices, Text.translatable("jex.proxy.title"), width / 2.f, height / 2.f - 38, -1);
        if (ProxyHelper.INSTANCE.isConnectedToProxy()) {
            ProxyHelper.ClientProxy proxy = ProxyHelper.INSTANCE.getProxy();
            FontHelper.INSTANCE.drawCenteredString(matrices, Text.translatable("jex.proxy.current", proxy.host() + ":" + proxy.port()), width / 2.f, height / 2.f - 70, -1);
        }
        super.render(matrices, mouseX, mouseY, delta);
    }
}
