package me.dustin.jex.gui.account;

import com.mojang.authlib.exceptions.AuthenticationException;
import me.dustin.jex.gui.account.account.MinecraftAccount;
import me.dustin.jex.gui.account.impl.GuiPasswordField;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.login.MicrosoftLogin;
import me.dustin.jex.helper.network.login.MojangLogin;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.util.UUID;

public class DirectLoginScreen extends Screen {

	TextFieldWidget username;
	TextFieldWidget email;
	GuiPasswordField password;
	private Screen parent;
	private String errorMessage = "";
	private boolean isMicrosoft;

	public DirectLoginScreen(Screen parent) {
		super(new LiteralText("Direct Login"));
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

		username = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), (Render2DHelper.INSTANCE.getScaledWidth() / 2) - 100, 12, 200, 20, new LiteralText("Username"));
		email = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), (Render2DHelper.INSTANCE.getScaledWidth() / 2) - 100, 47, 200, 20, new LiteralText("Email"));
		password = new GuiPasswordField(Wrapper.INSTANCE.getTextRenderer(), (Render2DHelper.INSTANCE.getScaledWidth() / 2) - 100, 82, 200, 20, new LiteralText("Password"));
		username.changeFocus(true);
		username.setMaxLength(16);
		this.email.setMaxLength(100);
		this.password.setMaxLength(250);
		this.children().clear();
		username.setFocusUnlocked(true);
		email.setFocusUnlocked(true);
		this.addSelectableChild(username);
		this.addSelectableChild(email);
		this.addSelectableChild(password);
		this.addDrawableChild(new ButtonWidget((Render2DHelper.INSTANCE.getScaledWidth() / 2) - 60, Render2DHelper.INSTANCE.getScaledHeight() - 54, 120, 20, new LiteralText("Cancel"), button -> {
			Wrapper.INSTANCE.getMinecraft().openScreen(parent);
		}));

		this.addDrawableChild(new ButtonWidget((Render2DHelper.INSTANCE.getScaledWidth() / 2) - 60, Render2DHelper.INSTANCE.getScaledHeight() - 75, 120, 20, new LiteralText("Login"), button -> {
			this.errorMessage = "Logging in...";
			if (isMicrosoft) {
				MinecraftAccount.MicrosoftAccount microsoftAccount = new MinecraftAccount.MicrosoftAccount(username.getText(), email.getText(), password.getText(), "", "", UUID.randomUUID().toString());
				new MicrosoftLogin(microsoftAccount, session -> {
					if (session != null) {
						Wrapper.INSTANCE.getIMinecraft().setSession(session);
						Wrapper.INSTANCE.getMinecraft().openScreen(parent);
					} else
						this.errorMessage = "\247cError, could not log in.";
				}).login();
			} else {
				MinecraftAccount.MojangAccount mojangAccount = new MinecraftAccount.MojangAccount(username.getText(), email.getText(), password.getText());
				mojangAccount.setCracked(!email.getText().contains("@"));
				new MojangLogin(mojangAccount, session -> {
					if (session != null) {
						Wrapper.INSTANCE.getIMinecraft().setSession(session);
						Wrapper.INSTANCE.getMinecraft().openScreen(parent);
					} else
						this.errorMessage = "\247cError, could not log in.";
				}).login();
			}
		}));


		this.addDrawableChild(new ButtonWidget((Render2DHelper.INSTANCE.getScaledWidth() / 2) - 60, Render2DHelper.INSTANCE.getScaledHeight() - 105, 120, 20, new LiteralText("\2476Mojang Account"), button -> {
			isMicrosoft = !isMicrosoft;
			button.setMessage(new LiteralText(isMicrosoft ? "\247aMicrosoft Account" : "\2476Mojang Account"));
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
		FontHelper.INSTANCE.drawWithShadow(matrixStack, "Username / Only needed for cracked", username.x, username.y - 10, 0xff696969);
		FontHelper.INSTANCE.drawWithShadow(matrixStack, "Email", email.x, email.y - 10, 0xff696969);
		FontHelper.INSTANCE.drawWithShadow(matrixStack, "Password", email.x, password.y - 10, 0xff696969);
		FontHelper.INSTANCE.drawCenteredString(matrixStack, errorMessage, width / 2.f, password.y + 30, 0xff696969);
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
