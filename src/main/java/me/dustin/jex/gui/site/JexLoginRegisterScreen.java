package me.dustin.jex.gui.site;

import me.dustin.jex.gui.account.impl.GuiPasswordField;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.jexsite.JexSiteHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class JexLoginRegisterScreen extends Screen {
    private boolean login;

    private TextFieldWidget usernameWidget;
    private TextFieldWidget emailWidget;
    private GuiPasswordField passwordWidget;
    private GuiPasswordField passwordConfirmWidget;

    private ButtonWidget doneButton;
    private ButtonWidget cancelButton;
    private ButtonWidget registerButton;

    public static String message = "";
    private Screen parent;
    public JexLoginRegisterScreen(boolean login, Screen parent) {
        super(login ? Text.translatable("jex.site.login") : Text.translatable("jex.site.register"));
        this.login = login;
        this.parent = parent;
        message = "";
    }

    @Override
    protected void init() {
        if (this.login) {
            this.addSelectableChild(this.usernameWidget = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), width / 2 - 100, height / 2 - 65, 200, 20, Text.translatable("jex.site.username")));
            this.addSelectableChild(this.passwordWidget = new GuiPasswordField(Wrapper.INSTANCE.getTextRenderer(), width / 2 - 100, height / 2 - 25, 200, 20, Text.translatable("jex.site.password")));
            this.addDrawableChild(this.doneButton = new ButtonWidget(width / 2 - 100, height / 2 + 5, 200, 20, Text.translatable("jex.site.login"), attempt));
            this.addDrawableChild(this.cancelButton = new ButtonWidget(width / 2 - 100, height / 2 + 35, 200, 20, Text.translatable("jex.button.cancel"), button -> Wrapper.INSTANCE.getMinecraft().setScreen(parent)));
            this.addDrawableChild(this.registerButton = new ButtonWidget(width / 2 - 100, height / 2 + 95, 200, 20, Text.translatable("jex.site.register"), button -> Wrapper.INSTANCE.getMinecraft().setScreen(new JexLoginRegisterScreen(false, parent))));
        } else {
            this.addSelectableChild(this.usernameWidget = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), width / 2 - 100, height / 2 - 100, 200, 20, Text.translatable("jex.site.username")));
            this.addSelectableChild(this.emailWidget = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), width / 2 - 100, height / 2 - 60, 200, 20, Text.translatable("jex.site.email")));
            this.addSelectableChild(this.passwordWidget = new GuiPasswordField(Wrapper.INSTANCE.getTextRenderer(), width / 2 - 100, height / 2 - 20, 200, 20, Text.translatable("jex.site.password")));
            this.addSelectableChild(this.passwordConfirmWidget = new GuiPasswordField(Wrapper.INSTANCE.getTextRenderer(), width / 2 - 100, height / 2 + 20, 200, 20, Text.translatable("jex.site.password_confirm")));
            this.addDrawableChild(this.doneButton = new ButtonWidget(width / 2 - 100, height / 2 + 50, 200, 20, Text.translatable("jex.site.register"), attempt));
            this.addDrawableChild(this.cancelButton = new ButtonWidget(width / 2 - 100, height / 2 + 80, 200, 20, Text.translatable("jex.button.cancel"), button -> Wrapper.INSTANCE.getMinecraft().setScreen(parent)));
        }
        super.init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        FontHelper.INSTANCE.drawCenteredString(matrices, message, width / 2.f, 2, 0xffff0000);
        if (this.usernameWidget != null) {
            FontHelper.INSTANCE.drawCenteredString(matrices, Text.translatable("jex.site.username"), width / 2.f, this.usernameWidget.y - 11, -1);
            this.usernameWidget.render(matrices, mouseX, mouseY, delta);
        }
        if (this.emailWidget != null) {
            FontHelper.INSTANCE.drawCenteredString(matrices, Text.translatable("jex.site.email"), width / 2.f, this.emailWidget.y - 11, -1);
            this.emailWidget.render(matrices, mouseX, mouseY, delta);
        }
        if (this.passwordWidget != null) {
            FontHelper.INSTANCE.drawCenteredString(matrices, Text.translatable("jex.site.password"), width / 2.f, this.passwordWidget.y - 11, -1);
            this.passwordWidget.render(matrices, mouseX, mouseY, delta);
        }
        if (this.passwordConfirmWidget != null) {
            FontHelper.INSTANCE.drawCenteredString(matrices, Text.translatable("jex.site.password_confirm"), width / 2.f, this.passwordConfirmWidget.y - 11, -1);
            this.passwordConfirmWidget.render(matrices, mouseX, mouseY, delta);
        }
        if (this.registerButton != null) {
            FontHelper.INSTANCE.drawCenteredString(matrices, Text.translatable("jex.site.no_account_question"), width / 2.f, this.registerButton.y - 11, -1);
            this.registerButton.render(matrices, mouseX, mouseY, delta);
        }
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        this.doneButton.active = (usernameWidget == null || !usernameWidget.getText().isEmpty()) && (passwordWidget == null || !passwordWidget.getText().isEmpty()) && (emailWidget == null || !emailWidget.getText().isEmpty()) && (passwordConfirmWidget == null || !passwordConfirmWidget.getText().isEmpty());
        super.tick();
    }

    ButtonWidget.PressAction attempt = button -> {
        if (this.login)
            message = JexSiteHelper.INSTANCE.login(this.usernameWidget.getText(), this.passwordWidget.getText());
        else
            message = JexSiteHelper.INSTANCE.register(this.usernameWidget.getText(), this.emailWidget.getText(), this.passwordWidget.getText(), this.passwordConfirmWidget.getText());
        if (message.isEmpty())
            Wrapper.INSTANCE.getMinecraft().setScreen(new JexWebsiteScreen(this.parent));
    };
}
