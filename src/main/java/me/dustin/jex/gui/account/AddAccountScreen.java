package me.dustin.jex.gui.account;

import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.AltFile;
import me.dustin.jex.gui.account.account.MinecraftAccount;
import me.dustin.jex.helper.network.login.minecraft.MinecraftAccountManager;
import me.dustin.jex.gui.account.impl.GuiPasswordField;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import me.dustin.jex.helper.render.Render2DHelper;
import net.minecraft.util.Formatting;

import java.util.UUID;

public class AddAccountScreen extends Screen {

	TextFieldWidget username;
	TextFieldWidget email;
	GuiPasswordField password;
	private MinecraftAccount.MojangAccount editingAccount;
	private Screen parent;
	private boolean isMicrosoft;

	public AddAccountScreen(MinecraftAccount.MojangAccount editingAccount, Screen parent) {
		super(Text.translatable("jex.account.add"));
		this.editingAccount = editingAccount;
		this.parent = parent;
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
		username = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), (Render2DHelper.INSTANCE.getScaledWidth() / 2) - 100, 12, 200, 20, Text.translatable("jex.account.username"));
		email = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), (Render2DHelper.INSTANCE.getScaledWidth() / 2) - 100, 47, 200, 20, Text.translatable("jex.account.email"));
		password = new GuiPasswordField(Wrapper.INSTANCE.getTextRenderer(), (Render2DHelper.INSTANCE.getScaledWidth() / 2) - 100, 82, 200, 20, Text.translatable("jex.account.password"));

		username.setTextFieldFocused(true);
		username.setMaxLength(16);
		this.email.setMaxLength(100);
		this.password.setMaxLength(250);

		if (editingAccount != null) {
			username.setText(editingAccount.getUsername());
			email.setText(editingAccount.getEmail());
			password.setText(editingAccount.getPassword());
		}

		this.children().clear();
		username.setFocusUnlocked(true);
		email.setFocusUnlocked(true);
		this.addSelectableChild(username);
		this.addSelectableChild(email);
		this.addSelectableChild(password);
		this.addDrawableChild(new ButtonWidget((Render2DHelper.INSTANCE.getScaledWidth() / 2) - 60, Render2DHelper.INSTANCE.getScaledHeight() - 54, 120, 20, Text.translatable("jex.button.cancel"), button -> {
			Wrapper.INSTANCE.getMinecraft().setScreen(parent);
		}));

		this.addDrawableChild(new ButtonWidget((Render2DHelper.INSTANCE.getScaledWidth() / 2) - 60, Render2DHelper.INSTANCE.getScaledHeight() - 75, 120, 20, editingAccount == null ? Text.translatable("jex.button.add") : Text.translatable("jex.button.save"), button -> {
			if (isMicrosoft) {
				MinecraftAccount.MicrosoftAccount microsoftAccount = new MinecraftAccount.MicrosoftAccount(username.getText(), email.getText(), password.getText(), "", "", UUID.randomUUID().toString());
				MinecraftAccountManager.INSTANCE.getAccounts().add(microsoftAccount);
			} else {
				MinecraftAccount.MojangAccount account;
				if (email.getText().equalsIgnoreCase("") || password.getText().equalsIgnoreCase("")) {
					account = new MinecraftAccount.MojangAccount(username.getText());
				} else {
					account = new MinecraftAccount.MojangAccount(username.getText(), email.getText(), password.getText());
				}
				if (editingAccount != null) {
					editingAccount.setUsername(account.getUsername());
					editingAccount.setEmail(account.getEmail());
					editingAccount.setPassword(account.getPassword());
				} else
					MinecraftAccountManager.INSTANCE.getAccounts().add(account);
			}
			ConfigManager.INSTANCE.get(AltFile.class).write();
			Wrapper.INSTANCE.getMinecraft().setScreen(parent);
		}));

		if (editingAccount == null)
			this.addDrawableChild(new ButtonWidget((Render2DHelper.INSTANCE.getScaledWidth() / 2) - 60, Render2DHelper.INSTANCE.getScaledHeight() - 105, 120, 20, Text.translatable("jex.account.mojang").styled(style -> style.withColor(Formatting.GOLD)), button -> {
				isMicrosoft = !isMicrosoft;
				button.setMessage(isMicrosoft ? Text.translatable("jex.account.microsoft").styled(style -> style.withColor(Formatting.GREEN)) : Text.translatable("jex.account.mojang").styled(style -> style.withColor(Formatting.GOLD)));
			}));
		super.init();
	}

	@Override
	public void close() {
		Wrapper.INSTANCE.getMinecraft().keyboard.setRepeatEvents(false);
		super.close();
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		renderBackground(matrixStack);
		username.render(matrixStack, mouseX, mouseY, partialTicks);
		email.render(matrixStack, mouseX, mouseY, partialTicks);
		password.renderButton(matrixStack, mouseX, mouseY, partialTicks);
		FontHelper.INSTANCE.drawWithShadow(matrixStack, Text.translatable("jex.account.username.title"), username.x, username.y - 10, 0xff696969);
		FontHelper.INSTANCE.drawWithShadow(matrixStack, Text.translatable("jex.account.email"), email.x, email.y - 10, 0xff696969);
		FontHelper.INSTANCE.drawWithShadow(matrixStack, Text.translatable("jex.account.password"), email.x, password.y - 10, 0xff696969);
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
