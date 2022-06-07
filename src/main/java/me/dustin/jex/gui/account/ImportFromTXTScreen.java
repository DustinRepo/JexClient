package me.dustin.jex.gui.account;

import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.AltFile;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.FileBrowser;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.io.File;

public class ImportFromTXTScreen extends Screen {
    protected ImportFromTXTScreen() {
        super(Text.translatable("jex.account.import.txt"));
    }

    private FileBrowser fileBrowser;
    private ButtonWidget cancelButton;
    private ButtonWidget importButton;
    private String message;

    @Override
    protected void init() {
        fileBrowser = new FileBrowser(ModFileHelper.INSTANCE.getJexDirectory().getPath(), width / 2.f - 150, height / 2.f - 150, 300, 300, null, "txt");
        importButton = new ButtonWidget(width / 2 - 150, height / 2 + 152, 300, 20, Text.translatable("jex.account.import"), button -> {
            File file = fileBrowser.getSelectedFiles().get(0);
            ConfigManager.INSTANCE.get(AltFile.class).importFromTXT(file);
            message = Formatting.GREEN + Text.translatable("jex.account.import.txt.success", file.getName()).getString();
        });
        cancelButton = new ButtonWidget(width / 2 - 150, height / 2 + 174, 300, 20, Text.translatable("jex.button.cancel"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new AccountManagerScreen());
        });
        message = Formatting.AQUA + Text.translatable("jex.account.import.supports").getString();
        this.addDrawableChild(importButton);
        this.addDrawableChild(cancelButton);
        super.init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
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
