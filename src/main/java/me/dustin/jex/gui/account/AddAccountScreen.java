package me.dustin.jex.gui.account;

import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.AltFile;
import me.dustin.jex.gui.account.account.MinecraftAccount;
import me.dustin.jex.helper.network.login.minecraft.MinecraftAccountManager;
import me.dustin.jex.gui.account.impl.GuiPasswordField;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import me.dustin.jex.helper.render.Render2DHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.UUID;

public class AddAccountScreen extends Screen {

	EditBox username;
	EditBox email;
	GuiPasswordField password;
	private MinecraftAccount.MojangAccount editingAccount;
	private Screen parent;
	private boolean isMicrosoft;

	public AddAccountScreen(MinecraftAccount.MojangAccount editingAccount, Screen parent) {
		super(Component.nullToEmpty("Add Account"));
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
		Wrapper.INSTANCE.getMinecraft().keyboardHandler.setSendRepeatsToGui(true);
		username = new EditBox(Wrapper.INSTANCE.getTextRenderer(), (Render2DHelper.INSTANCE.getScaledWidth() / 2) - 100, 12, 200, 20, Component.nullToEmpty("Username"));
		email = new EditBox(Wrapper.INSTANCE.getTextRenderer(), (Render2DHelper.INSTANCE.getScaledWidth() / 2) - 100, 47, 200, 20, Component.nullToEmpty("Email"));
		password = new GuiPasswordField(Wrapper.INSTANCE.getTextRenderer(), (Render2DHelper.INSTANCE.getScaledWidth() / 2) - 100, 82, 200, 20, Component.nullToEmpty("Password"));

		username.setFocus(true);
		username.setMaxLength(16);
		this.email.setMaxLength(100);
		this.password.setMaxLength(250);

		if (editingAccount != null) {
			username.setValue(editingAccount.getUsername());
			email.setValue(editingAccount.getEmail());
			password.setText(editingAccount.getPassword());
		}

		this.children().clear();
		username.setCanLoseFocus(true);
		email.setCanLoseFocus(true);
		this.addWidget(username);
		this.addWidget(email);
		this.addWidget(password);
		this.addRenderableWidget(new Button((Render2DHelper.INSTANCE.getScaledWidth() / 2) - 60, Render2DHelper.INSTANCE.getScaledHeight() - 54, 120, 20, Component.nullToEmpty("Cancel"), button -> {
			Wrapper.INSTANCE.getMinecraft().setScreen(parent);
		}));

		this.addRenderableWidget(new Button((Render2DHelper.INSTANCE.getScaledWidth() / 2) - 60, Render2DHelper.INSTANCE.getScaledHeight() - 75, 120, 20, editingAccount == null ? Component.nullToEmpty("Add") : Component.nullToEmpty("Save"), button -> {
			if (isMicrosoft) {
				MinecraftAccount.MicrosoftAccount microsoftAccount = new MinecraftAccount.MicrosoftAccount(username.getValue(), email.getValue(), password.getText(), "", "", UUID.randomUUID().toString());
				MinecraftAccountManager.INSTANCE.getAccounts().add(microsoftAccount);
			} else {
				MinecraftAccount.MojangAccount account;
				if (email.getValue().equalsIgnoreCase("") || password.getText().equalsIgnoreCase("")) {
					account = new MinecraftAccount.MojangAccount(username.getValue());
				} else {
					account = new MinecraftAccount.MojangAccount(username.getValue(), email.getValue(), password.getText());
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
			this.addRenderableWidget(new Button((Render2DHelper.INSTANCE.getScaledWidth() / 2) - 60, Render2DHelper.INSTANCE.getScaledHeight() - 105, 120, 20, Component.nullToEmpty("\2476Mojang Account"), button -> {
				isMicrosoft = !isMicrosoft;
				button.setMessage(Component.nullToEmpty(isMicrosoft ? "\247aMicrosoft Account" : "\2476Mojang Account"));
			}));
		super.init();
	}

	@Override
	public void onClose() {
		Wrapper.INSTANCE.getMinecraft().keyboardHandler.setSendRepeatsToGui(false);
		super.onClose();
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		renderBackground(matrixStack);
		username.render(matrixStack, mouseX, mouseY, partialTicks);
		email.render(matrixStack, mouseX, mouseY, partialTicks);
		password.renderButton(matrixStack, mouseX, mouseY, partialTicks);
		FontHelper.INSTANCE.drawWithShadow(matrixStack, "Username / Only needed for cracked", username.x, username.y - 10, 0xff696969);
		FontHelper.INSTANCE.drawWithShadow(matrixStack, "Email", email.x, email.y - 10, 0xff696969);
		FontHelper.INSTANCE.drawWithShadow(matrixStack, "Password", email.x, password.y - 10, 0xff696969);
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
