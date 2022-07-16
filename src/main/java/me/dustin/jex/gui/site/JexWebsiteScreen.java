package me.dustin.jex.gui.site;

import me.dustin.jex.JexClient;
import me.dustin.jex.helper.addon.AddonHelper;
import me.dustin.jex.helper.addon.pegleg.PeglegHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.jexsite.JexSiteHelper;
import me.dustin.jex.helper.network.MCAPIHelper;
import me.dustin.jex.helper.render.ButtonListener;
import me.dustin.jex.helper.render.FileBrowser;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.UUID;
import java.util.function.Consumer;

public class JexWebsiteScreen extends Screen {
    public String message;
    private final Screen parent;
    private final UUID uuid;
    private String mcName;

    private boolean rotatePlayer;

    private double lastMouseX;
    private float yaw;

    private FileBrowser capeFileBrowser;
    private ButtonWidget connectAccountButton;

    private Consumer<Void> task;

    public JexWebsiteScreen(Screen parent) {
        this(parent, "");
    }

    public JexWebsiteScreen(Screen parent, String message) {
        super(Text.translatable("jex.site.edit"));
        this.parent = parent;
        this.message = message;
        this.uuid = JexSiteHelper.INSTANCE.getUser().uuid().equals("NONE") ? null : UUID.fromString(JexSiteHelper.INSTANCE.getUser().uuid().replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
        if (this.uuid != null)
            this.mcName = MCAPIHelper.INSTANCE.getNameFromUUID(uuid);
        lastMouseX = MouseHelper.INSTANCE.getMouseX_D();
    }

    @Override
    protected void init() {
        if (this.uuid != null) {
            /*capeFileBrowser = new FileBrowser(Wrapper.INSTANCE.getMinecraft().runDirectory.getPath(), 2, 50, 200, 200, new ButtonListener() {
                @Override
                public void invoke() {
                    try {
                        byte[] fileContent = Files.readAllBytes(capeFileBrowser.getSelectedFiles().get(0).toPath());
                        String s = JexSiteHelper.INSTANCE.pushSetting("cape", Base64.getEncoder().encodeToString(fileContent));
                        if (s.isEmpty()) {
                            AddonHelper.INSTANCE.clearAddons();
                            AddonHelper.INSTANCE.getResponse(uuid.toString().replace("-", ""));
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, ".png", ".jpg", ".jpeg", JexSiteHelper.INSTANCE.getUser().donator() ? ".gif" : "");*/
        } else {
            this.addDrawableChild(this.connectAccountButton = new ButtonWidget(width / 2 - 100, 50, 200, 20, Text.translatable("jex.site.connect"), button -> {
               JexSiteHelper.INSTANCE.connectAndLinkAccount();
            }));
        }
        super.init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        FontHelper.INSTANCE.drawCenteredString(matrices, Text.translatable("jex.site.edit"), width / 2.f, 2, ColorHelper.INSTANCE.getClientColor());
        FontHelper.INSTANCE.drawCenteredString(matrices, Text.translatable("jex.site.show_name", JexSiteHelper.INSTANCE.getUser().name()), width / 2.f, 20, -1);
        if (uuid != null) {
            FontHelper.INSTANCE.drawCenteredString(matrices, Text.translatable("jex.site.mc_name", mcName), width / 2.f, 30, -1);
            FontHelper.INSTANCE.drawCenteredString(matrices, Text.translatable("jex.site.coming_soon"), width / 2.f, 45, -1);
            String uuidString = uuid.toString().replace("-", "");
            PeglegHelper.INSTANCE.setCurrentRender(uuid);
            Render2DHelper.INSTANCE.renderPlayerIn3D(MCAPIHelper.INSTANCE.getPlayerSkin(uuid), uuidString, width / 2.f, 80, yaw, 75);

            //FontHelper.INSTANCE.drawWithShadow(matrices, Text.translatable("jex.site.cape", mcName), 2, 45, -1);
            if (capeFileBrowser != null)
                capeFileBrowser.render(matrices);
        } else {
            FontHelper.INSTANCE.drawCenteredString(matrices, Text.translatable("jex.site.no_mc"), width / 2.f, 30, -1);
        }
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        if (task != null) {
            task.accept(null);
            task = null;
            return;
        }
        if (rotatePlayer) {
            if (!MouseHelper.INSTANCE.isMouseButtonDown(0)) {
                rotatePlayer = false;
                return;
            }
            double yawChange = lastMouseX - MouseHelper.INSTANCE.getMouseX_D();
            this.yaw += yawChange;
        }
        lastMouseX = MouseHelper.INSTANCE.getMouseX_D();
        if (capeFileBrowser != null)
            capeFileBrowser.tick();
        super.tick();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (capeFileBrowser != null)
            capeFileBrowser.scroll(amount);
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        rotatePlayer = true;
        if (capeFileBrowser != null)
            capeFileBrowser.click();
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void setTask(Consumer<Void> task) {
        this.task = task;
    }
}
