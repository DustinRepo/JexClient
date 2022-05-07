package me.dustin.jex.gui.mcleaks;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.events.EventManager;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.WebHelper;
import me.dustin.jex.helper.network.login.mcleaks.MCLeaksHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class MCLeaksScreen extends Screen {
    private Screen parent;

    private EditBox tokenField;
    private Button restoreButton;
    private Button useTokenButton;
    private String message;

    private boolean sessionRestored;

    private Screen settingScreen;

    public MCLeaksScreen(Screen parent, boolean sessionRestored) {
        super(Component.nullToEmpty("MCLeaks"));
        this.parent = parent;
        this.sessionRestored = sessionRestored;
    }

    public MCLeaksScreen(Screen parent, boolean sessionRestored, String message) {
        super(Component.nullToEmpty("MCLeaks"));
        this.parent = parent;
        this.sessionRestored = sessionRestored;
        this.message = message;
    }

    @Override
    protected void init() {
        restoreButton = new Button(this.width / 2 - 150, this.height / 4 + 96 + 18, 128, 20, Component.nullToEmpty(this.sessionRestored ? "Session restored!" : "Restore Session"), button -> {
            MCLeaksHelper.INSTANCE.restoreSession();
            Wrapper.INSTANCE.getMinecraft().setScreen(new MCLeaksScreen(this.parent, true));
            EventManager.unregister(MCLeaksHelper.INSTANCE);
        });
        useTokenButton = new Button(this.width / 2 - 18, this.height / 4 + 96 + 18, 168, 20, Component.nullToEmpty("Redeem Token"), button -> {
            if (this.tokenField.getValue().length() != 16) {
                Wrapper.INSTANCE.getMinecraft().setScreen(new MCLeaksScreen(this.parent, false, ChatFormatting.RED + "The token has to be 16 characters long!"));
                return;
            }
            button.active = false;
            button.setMessage(Component.nullToEmpty("Please wait ..."));
            new Thread(() -> {
                MCLeaksHelper.MCLeaksAccount account = MCLeaksHelper.INSTANCE.getAccount(tokenField.getValue());
                if (account != null) {
                    EventManager.register(MCLeaksHelper.INSTANCE);
                    MCLeaksHelper.INSTANCE.setActiveAccount(account);
                    settingScreen = new MCLeaksScreen(this.parent, false, ChatFormatting.GREEN + "Your token was redeemed successfully!");
                } else {
                    settingScreen = new MCLeaksScreen(this.parent, false, ChatFormatting.RED + "Invalid token!");
                }
            }).start();
        });
        Button getTokenButton = new Button(this.width / 2 - 150, this.height / 4 + 120 + 18, 158, 20, Component.nullToEmpty("Get Token"), button -> {
            WebHelper.INSTANCE.openLink("https://mcleaks.net/");
        });
        Button cancelButton = new Button(this.width / 2 + 12, this.height / 4 + 120 + 18, 138, 20, Component.nullToEmpty("Cancel"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(parent);
        });
        tokenField = new EditBox(Wrapper.INSTANCE.getTextRenderer(), this.width / 2 - 100, 128, 200, 20, Component.nullToEmpty(""));
        this.addRenderableWidget(restoreButton);
        this.addRenderableWidget(useTokenButton);
        this.addRenderableWidget(getTokenButton);
        this.addRenderableWidget(cancelButton);
        this.addWidget(tokenField);
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
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        tokenField.render(matrices, mouseX, mouseY, delta);

        FontHelper.INSTANCE.drawCenteredString(matrices, ChatFormatting.WHITE + "- " + ChatFormatting.AQUA + "MCLeaks" + ChatFormatting.WHITE + "." + ChatFormatting.AQUA + "net " + ChatFormatting.WHITE + "-", this.width / 2.f, 17, 16777215);
        FontHelper.INSTANCE.drawCenteredString(matrices, "Free minecraft accounts", this.width / 2.f, 32, 16777215);

        String status = ChatFormatting.GOLD + "No Token redeemed. Using " + ChatFormatting.YELLOW + Wrapper.INSTANCE.getMinecraft().getUser().getName() + ChatFormatting.GOLD + " to login!";
        if (MCLeaksHelper.INSTANCE.activeAccount != null) {
            status = ChatFormatting.GREEN + "Token active. Using " + ChatFormatting.AQUA + MCLeaksHelper.INSTANCE.activeAccount.mcname + ChatFormatting.GREEN + " to login!";
        }
        FontHelper.INSTANCE.drawCenteredString(matrices, "Status:", this.width / 2.f, 68, 16777215);
        FontHelper.INSTANCE.drawCenteredString(matrices, status, this.width / 2.f, 78, 16777215);

        FontHelper.INSTANCE.drawWithShadow(matrices, "Token", this.width / 2.f - 100, 115, 10526880);

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
