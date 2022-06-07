package me.dustin.jex.gui.mcleaks;

import me.dustin.events.EventManager;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.WebHelper;
import me.dustin.jex.helper.network.login.mcleaks.MCLeaksHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class MCLeaksScreen extends Screen {
    private Screen parent;

    private TextFieldWidget tokenField;
    private ButtonWidget restoreButton;
    private ButtonWidget useTokenButton;
    private String message;

    private boolean sessionRestored;

    private Screen settingScreen;

    public MCLeaksScreen(Screen parent, boolean sessionRestored) {
        super(Text.translatable("jex.mcleaks"));
        this.parent = parent;
        this.sessionRestored = sessionRestored;
    }

    public MCLeaksScreen(Screen parent, boolean sessionRestored, String message) {
        super(Text.translatable("jex.mcleaks"));
        this.parent = parent;
        this.sessionRestored = sessionRestored;
        this.message = message;
    }

    @Override
    protected void init() {
        restoreButton = new ButtonWidget(this.width / 2 - 150, this.height / 4 + 96 + 18, 128, 20, this.sessionRestored ? Text.translatable("jex.mcleaks.session.restored") : Text.translatable("jex.mcleaks.session.restore"), button -> {
            MCLeaksHelper.INSTANCE.restoreSession();
            Wrapper.INSTANCE.getMinecraft().setScreen(new MCLeaksScreen(this.parent, true));
            EventManager.unregister(MCLeaksHelper.INSTANCE);
        });
        useTokenButton = new ButtonWidget(this.width / 2 - 18, this.height / 4 + 96 + 18, 168, 20, Text.translatable("jex.mcleaks.redeem"), button -> {
            if (this.tokenField.getText().length() != 16) {
                Wrapper.INSTANCE.getMinecraft().setScreen(new MCLeaksScreen(this.parent, false, Formatting.RED + Text.translatable("jex.mcleaks.bad_length").getString()));
                return;
            }
            button.active = false;
            button.setMessage(Text.translatable("jex.mcleaks.wait"));
            new Thread(() -> {
                MCLeaksHelper.MCLeaksAccount account = MCLeaksHelper.INSTANCE.getAccount(tokenField.getText());
                if (account != null) {
                    EventManager.register(MCLeaksHelper.INSTANCE);
                    MCLeaksHelper.INSTANCE.setActiveAccount(account);
                    settingScreen = new MCLeaksScreen(this.parent, false, Formatting.GREEN + Text.translatable("jex.mcleaks.redeem.success").getString());
                } else {
                    settingScreen = new MCLeaksScreen(this.parent, false, Formatting.RED + Text.translatable("jex.mcleaks.redeem.fail").getString());
                }
            }).start();
        });
        ButtonWidget getTokenButton = new ButtonWidget(this.width / 2 - 150, this.height / 4 + 120 + 18, 158, 20, Text.translatable("jex.mcleaks.get_token"), button -> {
            WebHelper.INSTANCE.openLink("https://mcleaks.net/");
        });
        ButtonWidget cancelButton = new ButtonWidget(this.width / 2 + 12, this.height / 4 + 120 + 18, 138, 20, Text.translatable("jex.button.cancel"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(parent);
        });
        tokenField = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), this.width / 2 - 100, 128, 200, 20, Text.of(""));
        this.addDrawableChild(restoreButton);
        this.addDrawableChild(useTokenButton);
        this.addDrawableChild(getTokenButton);
        this.addDrawableChild(cancelButton);
        this.addSelectableChild(tokenField);
        super.init();
    }

    @Override
    public void tick() {
        if (settingScreen != null) {
            Wrapper.INSTANCE.getMinecraft().setScreen(settingScreen);
            settingScreen = null;
            return;
        }
        tokenField.tick();
        super.tick();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        tokenField.render(matrices, mouseX, mouseY, delta);

        FontHelper.INSTANCE.drawCenteredString(matrices, Formatting.WHITE + "- " + Formatting.AQUA + "MCLeaks" + Formatting.WHITE + "." + Formatting.AQUA + "net " + Formatting.WHITE + "-", this.width / 2.f, 17, 16777215);
        FontHelper.INSTANCE.drawCenteredString(matrices, Text.translatable("jex.mcleaks.description"), this.width / 2.f, 32, 16777215);

        String status = Formatting.GOLD + Text.translatable("jex.mcleaks.token.not_active", Formatting.YELLOW + Wrapper.INSTANCE.getMinecraft().getSession().getUsername() + Formatting.GOLD).getString();
        if (MCLeaksHelper.INSTANCE.activeAccount != null) {
            status = Formatting.GREEN + Text.translatable("jex.mcleaks.token.active", Formatting.AQUA + MCLeaksHelper.INSTANCE.activeAccount.mcname + Formatting.GREEN).getString();
        }
        FontHelper.INSTANCE.drawCenteredString(matrices, Text.translatable("jex.mcleaks.status"), this.width / 2.f, 68, 16777215);
        FontHelper.INSTANCE.drawCenteredString(matrices, status, this.width / 2.f, 78, 16777215);

        FontHelper.INSTANCE.drawWithShadow(matrices, Text.translatable("jex.mcleaks.token"), this.width / 2.f - 100, 115, 10526880);

        if (message != null) {
            FontHelper.INSTANCE.drawCenteredString(matrices, this.message, this.width / 2.f, 158, 16777215);
        }
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            Wrapper.INSTANCE.getMinecraft().setScreen(parent);
            return false;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
