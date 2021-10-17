package me.dustin.jex.gui.account;

import com.mojang.authlib.exceptions.AuthenticationException;
import me.dustin.jex.gui.account.account.MinecraftAccount;
import me.dustin.jex.gui.account.impl.GuiPasswordField;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.login.MojangLogin;
import me.dustin.jex.helper.network.login.MicrosoftLogin;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class DirectLoginScreen extends Screen {

	TextFieldWidget username;
	TextFieldWidget email;
	GuiPasswordField password;
	private Screen parent;
	private String errorMessage = "";

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
		MicrosoftLogin microsoftLogin = new MicrosoftLogin(false);
		this.addDrawableChild(new ButtonWidget((Render2DHelper.INSTANCE.getScaledWidth() / 2) - 60, Render2DHelper.INSTANCE.getScaledHeight() - 54, 120, 20, new LiteralText("Cancel"), button -> {
			Wrapper.INSTANCE.getMinecraft().openScreen(parent);
			microsoftLogin.stopLoginProcess();
		}));

		this.addDrawableChild(new ButtonWidget((Render2DHelper.INSTANCE.getScaledWidth() / 2) - 60, Render2DHelper.INSTANCE.getScaledHeight() - 75, 120, 20, new LiteralText("Login"), button -> {
			this.errorMessage = "Logging in...";
			MinecraftAccount.MojangAccount mojangAccount = new MinecraftAccount.MojangAccount(username.getText(), email.getText(), password.getText());
			mojangAccount.setCracked(!email.getText().contains("@"));
			try {
				if (MojangLogin.INSTANCE.login(mojangAccount)) {
					Wrapper.INSTANCE.getMinecraft().openScreen(parent);
				} else {
					this.errorMessage = "\247cError, could not log in.";
				}
			} catch (AuthenticationException e) {
				e.printStackTrace();
				this.errorMessage = "\247cError, could not log in.";
			}
		}));

		this.addDrawableChild(new ButtonWidget((Render2DHelper.INSTANCE.getScaledWidth() / 2) - 60, Render2DHelper.INSTANCE.getScaledHeight() - 105, 120, 20, new LiteralText("Microsoft Account"), button -> {
			microsoftLogin.startLoginProcess();
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
