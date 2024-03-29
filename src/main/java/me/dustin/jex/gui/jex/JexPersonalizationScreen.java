package me.dustin.jex.gui.jex;

import me.dustin.jex.helper.addon.cape.CapeHelper;
import me.dustin.jex.helper.addon.hat.HatHelper;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.ClientSettingsFile;
import me.dustin.jex.helper.render.ButtonListener;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.FileBrowser;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;
import java.io.File;

public class JexPersonalizationScreen extends Screen {

    private Screen parent;
    protected JexPersonalizationScreen(Screen parent) {
        super(Text.translatable("jex.personalization"));
        this.parent = parent;
    }
    public static String setCape;
    public static String setHat;
    private static final Identifier hatsPic = new Identifier("jex", "gui/jex/hats.png");
    private static String LAST_PATH = Wrapper.INSTANCE.getMinecraft().runDirectory.getPath();

    private FileBrowser fileBrowser;
    private ButtonWidget setCapeButton;
    private ButtonWidget nextHatButton;
    private ButtonWidget prevHatButton;
    private ButtonWidget setHatButton;
    private int selectedHat = 0;

    private final ButtonListener doubleClickListener = new ButtonListener() {
        @Override
        public void invoke() {
            if (fileBrowser.getSelectedFiles().isEmpty())
                return;
            File cape = fileBrowser.getSelectedFiles().get(0);
            setCape = cape.getPath();
            CapeHelper.INSTANCE.setPersonalCape(cape);
            ConfigManager.INSTANCE.get(ClientSettingsFile.class).write();
        }
    };

    @Override
    protected void init() {
        float midX = width / 2.f;
        setCapeButton = new ButtonWidget((int)midX - 255 + 2, 215, 250, 20, Text.translatable("jex.personalization.set_cape"), button -> {
            if (fileBrowser.getSelectedFiles().isEmpty() || fileBrowser.getSelectedFiles().get(0).isDirectory())
                return;
            File cape = fileBrowser.getSelectedFiles().get(0);
            setCape = cape.getPath();
            CapeHelper.INSTANCE.setPersonalCape(cape);
            ConfigManager.INSTANCE.get(ClientSettingsFile.class).write();
        });
        prevHatButton = new ButtonWidget((int)midX + 8, 137, 40, 20, Text.of("<"), button -> {
            selectedHat--;
            if (selectedHat < -1)
                selectedHat = HatHelper.HatType.values().length - 1;
        });
        nextHatButton = new ButtonWidget((int)midX + 88, 137, 40, 20, Text.of(">"), button -> {
            selectedHat++;
            if (selectedHat > HatHelper.HatType.values().length - 1)
                selectedHat = -1;
        });
        setHatButton = new ButtonWidget((int)midX + 8, 170, 120, 20, Text.translatable("jex.personalization.set_hat"), button -> {
            String uuid = Wrapper.INSTANCE.getMinecraft().getSession().getUuid().replace("-", "");
            if (selectedHat == -1) {
                HatHelper.INSTANCE.clearHat(uuid);
                setHat = "None";
            } else {
                HatHelper.HatType hatType = HatHelper.HatType.values()[selectedHat];
                HatHelper.INSTANCE.setHat(uuid, hatType.name());
                setHat = hatType.name();
            }
            ConfigManager.INSTANCE.get(ClientSettingsFile.class).write();
        });
        fileBrowser = new FileBrowser(LAST_PATH, midX - 255 + 2, 15, 250, 200, doubleClickListener, ".png", ".jpg", ".gif");
        fileBrowser.setMultiSelect(false);
        this.addDrawableChild(setCapeButton);
        this.addDrawableChild(prevHatButton);
        this.addDrawableChild(nextHatButton);
        this.addDrawableChild(setHatButton);
        super.init();
    }
    float yaw = 0;

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        float midX = width / 2.f;
        setCapeButton.active = !fileBrowser.getSelectedFiles().isEmpty() && !fileBrowser.getSelectedFiles().get(0).isDirectory();
        renderBackground(matrices);
        //Cape
        Render2DHelper.INSTANCE.fillAndBorder(matrices, midX - 255 + 2, 1, midX - 255 + 250, 13, 0x90696969, 0x70000000, 1);
        FontHelper.INSTANCE.drawWithShadow(matrices, fileBrowser.getPath(), midX - 255 + 5, 3, -1);
        if (CapeHelper.INSTANCE.hasCape("self"))
            Render2DHelper.INSTANCE.draw3DCape(matrices, midX - 255 - 35, 15, CapeHelper.INSTANCE.getCape("self"), yaw, 0);
        fileBrowser.render(matrices);
        //Hat
        Render2DHelper.INSTANCE.fillAndBorder(matrices, midX + 5, 15, midX + 131, 203, 0x90696969, 0x70000000, 1);
        String hatName;
        if (selectedHat != -1) {
            HatHelper.HatType hatType = HatHelper.HatType.values()[selectedHat];
            hatName = hatType.name().replace("_", " ").toLowerCase();
        } else {
            hatName = "None";
        }
        FontHelper.INSTANCE.drawWithShadow(matrices, Text.translatable("jex.personalization.selected_hat", StringUtils.capitalize(hatName)), midX + 9, 160, -1);

        Render2DHelper.INSTANCE.bindTexture(hatsPic);
        DrawableHelper.drawTexture(matrices, (int)midX + 8, 18, 0, 0, 120, 120, 120, 120);
        HatHelper.HatType hatType = HatHelper.INSTANCE.getHatType(Wrapper.INSTANCE.getMinecraft().getSession().getUuid().replace("-", ""));
        FontHelper.INSTANCE.drawWithShadow(matrices, Text.translatable("jex.personalization.current_hat", hatType == null ? "None" : StringUtils.capitalize(hatType.name().replace("_", " ").toLowerCase())), midX + 9, 192, -1);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        fileBrowser.click();
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        fileBrowser.scroll(amount);
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public void tick() {
        yaw+=2;
        fileBrowser.tick();
        LAST_PATH = fileBrowser.getPath();
        super.tick();
    }
}
