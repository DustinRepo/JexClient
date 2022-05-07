package me.dustin.jex.gui.account;

import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.AltFile;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.FileBrowser;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.File;

public class ImportFromTXTScreen extends Screen {
    protected ImportFromTXTScreen() {
        super(Component.nullToEmpty("Import From TXT"));
    }

    private FileBrowser fileBrowser;
    private Button cancelButton;
    private Button importButton;
    private String message;

    @Override
    protected void init() {
        fileBrowser = new FileBrowser(ModFileHelper.INSTANCE.getJexDirectory().getPath(), width / 2.f - 150, height / 2.f - 150, 300, 300, null, "txt");
        importButton = new Button(width / 2 - 150, height / 2 + 152, 300, 20, Component.nullToEmpty("Import"), button -> {
            File file = fileBrowser.getSelectedFiles().get(0);
            ConfigManager.INSTANCE.get(AltFile.class).importFromTXT(file);
            message = ChatFormatting.GREEN + "Successfully imported " + file.getName();
        });
        cancelButton = new Button(width / 2 - 150, height / 2 + 174, 300, 20, Component.nullToEmpty("Cancel"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new AccountManagerScreen());
        });
        message = ChatFormatting.AQUA + "Supports username:email:password or email:password";
        this.addRenderableWidget(importButton);
        this.addRenderableWidget(cancelButton);
        super.init();
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        fileBrowser.render(matrices);
        FontHelper.INSTANCE.drawCenteredString(matrices, message, width / 2.f, height / 2.f - 175, -1);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        fileBrowser.click();
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void tick() {
        fileBrowser.tick();
        importButton.active = !fileBrowser.getSelectedFiles().isEmpty();
        super.tick();
    }
}
