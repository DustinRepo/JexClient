package me.dustin.jex.gui.jex;

import me.dustin.jex.addon.cape.Cape;
import me.dustin.jex.addon.hat.Hat;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.ClientSettingsFile;
import me.dustin.jex.helper.render.ButtonListener;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.FileBrowser;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.File;

public class JexPersonalizationScreen extends Screen {

    private Screen parent;
    protected JexPersonalizationScreen(Screen parent) {
        super(Component.nullToEmpty("Personalization"));
        this.parent = parent;
    }
    public static String setCape;
    public static String setHat;
    private static final ResourceLocation hatsPic = new ResourceLocation("jex", "gui/jex/hats.png");
    private static String LAST_PATH = Wrapper.INSTANCE.getMinecraft().gameDirectory.getPath();

    private FileBrowser fileBrowser;
    private Button setCapeButton;
    private Button nextHatButton;
    private Button prevHatButton;
    private Button setHatButton;
    private int selectedHat = 0;

    private final ButtonListener doubleClickListener = new ButtonListener() {
        @Override
        public void invoke() {
            if (fileBrowser.getSelectedFiles().isEmpty())
                return;
            File cape = fileBrowser.getSelectedFiles().get(0);
            setCape = cape.getPath();
            Cape.setPersonalCape(cape);
            ConfigManager.INSTANCE.get(ClientSettingsFile.class).write();
        }
    };

    @Override
    protected void init() {
        float midX = width / 2.f;
        setCapeButton = new Button((int)midX - 255 + 2, 215, 250, 20, Component.nullToEmpty("Set Cape"), button -> {
            if (fileBrowser.getSelectedFiles().isEmpty() || fileBrowser.getSelectedFiles().get(0).isDirectory())
                return;
            File cape = fileBrowser.getSelectedFiles().get(0);
            setCape = cape.getPath();
            Cape.setPersonalCape(cape);
            ConfigManager.INSTANCE.get(ClientSettingsFile.class).write();
        });
        prevHatButton = new Button((int)midX + 8, 137, 40, 20, Component.nullToEmpty("<"), button -> {
            selectedHat--;
            if (selectedHat < -1)
                selectedHat = Hat.HatType.values().length - 1;
        });
        nextHatButton = new Button((int)midX + 88, 137, 40, 20, Component.nullToEmpty(">"), button -> {
            selectedHat++;
            if (selectedHat > Hat.HatType.values().length - 1)
                selectedHat = -1;
        });
        setHatButton = new Button((int)midX + 8, 170, 120, 20, Component.nullToEmpty("Set Hat"), button -> {
            String uuid = Wrapper.INSTANCE.getMinecraft().getUser().getUuid().replace("-", "");
            if (selectedHat == -1) {
                Hat.clearHat(uuid);
                setHat = "None";
            } else {
                Hat.HatType hatType = Hat.HatType.values()[selectedHat];
                Hat.setHat(uuid, hatType.name());
                setHat = hatType.name();
            }
            ConfigManager.INSTANCE.get(ClientSettingsFile.class).write();
        });
        fileBrowser = new FileBrowser(LAST_PATH, midX - 255 + 2, 15, 250, 200, doubleClickListener, ".png", ".jpg");
        fileBrowser.setMultiSelect(false);
        this.addRenderableWidget(setCapeButton);
        this.addRenderableWidget(prevHatButton);
        this.addRenderableWidget(nextHatButton);
        this.addRenderableWidget(setHatButton);
        super.init();
    }
    float yaw = 0;

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        float midX = width / 2.f;
        setCapeButton.active = !fileBrowser.getSelectedFiles().isEmpty() && !fileBrowser.getSelectedFiles().get(0).isDirectory();
        renderBackground(matrices);
        //Cape
        Render2DHelper.INSTANCE.fillAndBorder(matrices, midX - 255 + 2, 1, midX - 255 + 250, 13, 0x90696969, 0x70000000, 1);
        FontHelper.INSTANCE.drawWithShadow(matrices, fileBrowser.getPath(), midX - 255 + 5, 3, -1);
        if (Cape.capes.containsKey("self"))
            Render2DHelper.INSTANCE.draw3DCape(matrices, midX - 255 - 35, 15, Cape.capes.get("self"), yaw, 0);
        fileBrowser.render(matrices);
        //Hat
        Render2DHelper.INSTANCE.fillAndBorder(matrices, midX + 5, 15, midX + 131, 203, 0x90696969, 0x70000000, 1);
        String hatName;
        if (selectedHat != -1) {
            Hat.HatType hatType = Hat.HatType.values()[selectedHat];
            hatName = hatType.name().replace("_", " ").toLowerCase();
        } else {
            hatName = "None";
        }
        FontHelper.INSTANCE.drawWithShadow(matrices, "Selected Hat: " + StringUtils.capitalize(hatName), midX + 9, 160, -1);

        Render2DHelper.INSTANCE.bindTexture(hatsPic);
        GuiComponent.blit(matrices, (int)midX + 8, 18, 0, 0, 120, 120, 120, 120);
        Hat.HatInfo hatInfo = Hat.getInfo(Wrapper.INSTANCE.getMinecraft().getUser().getUuid().replace("-", ""));
        FontHelper.INSTANCE.drawWithShadow(matrices, "Current Hat: " + (hatInfo == null ? "None" : StringUtils.capitalize(hatInfo.type.name().replace("_", " ").toLowerCase())), midX + 9, 192, -1);
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
