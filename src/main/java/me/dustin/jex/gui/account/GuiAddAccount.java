package me.dustin.jex.gui.account;

import me.dustin.jex.file.AltFile;
import me.dustin.jex.gui.account.account.MinecraftAccount;
import me.dustin.jex.gui.account.account.MinecraftAccountManager;
import me.dustin.jex.gui.account.impl.GuiPasswordField;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class GuiAddAccount extends Screen {

    TextFieldWidget username = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), (Render2DHelper.INSTANCE.getScaledWidth() / 2) - 100, (Render2DHelper.INSTANCE.getScaledHeight() / 2) - 140, 200, 20, new LiteralText("Username"));
    TextFieldWidget email = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), (Render2DHelper.INSTANCE.getScaledWidth() / 2) - 100, (Render2DHelper.INSTANCE.getScaledHeight() / 2) - 119 + 20, 200, 20, new LiteralText("Email"));
    GuiPasswordField password = new GuiPasswordField(Wrapper.INSTANCE.getTextRenderer(), (Render2DHelper.INSTANCE.getScaledWidth() / 2) - 100, (Render2DHelper.INSTANCE.getScaledHeight() / 2) - 98 + 40, 200, 20, new LiteralText("Password"));
    private MinecraftAccount editingAccount;
    private Screen parent;


    public GuiAddAccount(MinecraftAccount editingAccount, Screen parent) {
        super(new LiteralText("Add Account"));
        this.editingAccount = editingAccount;
        this.parent = parent;

        if (editingAccount != null) {
            username.setText(editingAccount.getUsername());
            email.setText(editingAccount.getEmail());
            password.setText(editingAccount.getPassword());
        }
    }

    @Override
    public void tick() {
        username.tick();
        email.tick();
        password.tick();
        super.tick();
    }

    @Override
    public void init() {
        Wrapper.INSTANCE.getMinecraft().keyboard.setRepeatEvents(true);
        username.changeFocus(true);
        username.setMaxLength(16);
        this.email.setMaxLength(100);
        this.password.setMaxLength(250);
        this.buttons.clear();
        username.setFocusUnlocked(true);
        email.setFocusUnlocked(true);
        this.children.add(username);
        this.children.add(email);
        this.children.add(password);
        this.addButton(new ButtonWidget((Render2DHelper.INSTANCE.getScaledWidth() / 2) - 60, Render2DHelper.INSTANCE.getScaledHeight() - 54, 120, 20, new LiteralText("Cancel"), button -> {
            Wrapper.INSTANCE.getMinecraft().openScreen(parent);
        }));
        this.addButton(new ButtonWidget((Render2DHelper.INSTANCE.getScaledWidth() / 2) - 60, Render2DHelper.INSTANCE.getScaledHeight() - 75, 120, 20, editingAccount == null ? new LiteralText("Add") : new LiteralText("Edit"), button -> {
            MinecraftAccount account;
            if (email.getText().equalsIgnoreCase("") || password.getText().equalsIgnoreCase("")) {
                account = new MinecraftAccount(username.getText());
            } else {
                account = new MinecraftAccount(username.getText(), email.getText(), password.getText());
            }
            if (editingAccount != null) {
                editingAccount.setUsername(account.getUsername());
                editingAccount.setEmail(account.getEmail());
                editingAccount.setPassword(account.getPassword());
            } else
                MinecraftAccountManager.INSTANCE.getAccounts().add(account);
            AltFile.write();
            Wrapper.INSTANCE.getMinecraft().openScreen(parent);
        }));
        super.init();
    }

    @Override
    public void onClose() {
        Wrapper.INSTANCE.getMinecraft().keyboard.setRepeatEvents(false);
        super.onClose();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        username.render(matrixStack, mouseX, mouseY, partialTicks);
        email.render(matrixStack, mouseX, mouseY, partialTicks);
        password.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, "Username", username.x, username.y - 10, 0xff696969);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, "Email", email.x, email.y - 10, 0xff696969);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, "Password", email.x, email.y + 30, 0xff696969);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.username.mouseClicked(mouseX, mouseY, button);
        this.email.mouseClicked(mouseX, mouseY, button);
        this.password.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

}
