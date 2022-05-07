//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.dustin.jex.gui.minecraft;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import me.dustin.jex.addon.cape.Cape;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.ClientSettingsFile;
import me.dustin.jex.gui.changelog.ChangelogScreen;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.RealmsMainScreen;
import me.dustin.jex.JexClient;
import me.dustin.jex.addon.Addon;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.CustomMainMenu;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.MCAPIHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.update.UpdateManager;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class JexTitleScreen extends Screen {
    public static final CubeMap PANORAMA_CUBE_MAP = new CubeMap(new ResourceLocation("textures/gui/title/background/panorama"));
    private static final ResourceLocation PANORAMA_OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
    private static final ResourceLocation MINECRAFT_TITLE_TEXTURE = new ResourceLocation("textures/gui/title/minecraft.png");
    private static final ResourceLocation JEX_TITLE_TEXTURE = new ResourceLocation("jex", "gui/jex/jex-logo.png");
    private static final ResourceLocation EDITION_TITLE_TEXTURE = new ResourceLocation("textures/gui/title/edition.png");
    public static int background = 0;
    private static ArrayList<Background> backgrounds = new ArrayList<>();
    private final boolean isMinceraft;
    private final PanoramaRenderer backgroundRenderer;
    private final boolean doBackgroundFade;
    @Nullable
    private String splashText;
    private Screen realmsNotificationGui;
    private long backgroundFadeStart;

    private CustomMainMenu customMainMenu;
    private StopWatch stopWatch = new StopWatch();
    private boolean isDonator;

    public JexTitleScreen() {
        this(false);
    }

    public JexTitleScreen(boolean doBackgroundFade) {
        super(Component.translatable("narrator.screen.title"));
        this.backgroundRenderer = new PanoramaRenderer(PANORAMA_CUBE_MAP);
        this.doBackgroundFade = doBackgroundFade;
        this.isMinceraft = (double) (new Random()).nextFloat() < 1.0E-4D;
        customMainMenu = Feature.get(CustomMainMenu.class);

        MCAPIHelper.INSTANCE.downloadPlayerSkin(Wrapper.INSTANCE.getMinecraft().getUser().getGameProfile().getId());
    }

    public void tick() {
        capeYaw+=2;
    }

    public boolean isPauseScreen() {
        return false;
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    protected void init() {
        try {
            loadBackgrounds();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (this.splashText == null) {
            this.splashText = this.minecraft.getSplashManager().getSplash();
        }

        int j = this.height / 4 + 48;

        this.initWidgetsNormal(j);

        if (customMainMenu.customBackground) {
            if (!backgrounds.isEmpty()) {
                this.addRenderableWidget(new Button(this.width - 22, this.height - 22, 20, 20, Component.nullToEmpty(">"), button -> {
                    JexTitleScreen.background += 1;
                    if (JexTitleScreen.background > backgrounds.size() - 1) {
                        JexTitleScreen.background = 0;
                    }
                    ConfigManager.INSTANCE.get(ClientSettingsFile.class).write();
                }));
                this.addRenderableWidget(new Button(this.width - 44, this.height - 22, 20, 20, Component.nullToEmpty("<"), button -> {
                    JexTitleScreen.background -= 1;
                    if (JexTitleScreen.background < 0) {
                        JexTitleScreen.background = backgrounds.size() - 1;
                    }
                    ConfigManager.INSTANCE.get(ClientSettingsFile.class).write();
                }));
            } else {
                this.addRenderableWidget(new Button(this.width - 152, this.height - 22, 150, 20, Component.nullToEmpty("Open Backgrounds Folder"), button -> {
                    Util.getPlatform().openFile(new File(ModFileHelper.INSTANCE.getJexDirectory(), "backgrounds"));
                }));
            }
        }
        if (JexTitleScreen.background < 0) {
            JexTitleScreen.background = backgrounds.size() - 1;
        } else if (JexTitleScreen.background > backgrounds.size() - 1) {
            JexTitleScreen.background = 0;
        }
        this.minecraft.setConnectedToRealms(false);
    }

    private void initWidgetsNormal(int y) {
        JexTitleScreen titleScreen = this;
        this.addRenderableWidget(new Button(2, y, 200, 20, Component.translatable("menu.singleplayer"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new SelectWorldScreen(titleScreen));
        }));
        this.addRenderableWidget(new Button(2, y + 24, 175, 20, Component.translatable("menu.multiplayer"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new JoinMultiplayerScreen(titleScreen));
        }));
        this.addRenderableWidget(new Button(2, y + 24 * 2, 150, 20, Component.translatable("menu.online"), button -> {
            titleScreen.switchToRealms();
        }));
        this.addRenderableWidget(new Button(2, y + 24 * 3, 125, 20, Component.translatable("menu.options"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new OptionsScreen(titleScreen, Wrapper.INSTANCE.getOptions()));
        }));
        this.addRenderableWidget(new Button(2, y + 24 * 4, 100, 20, Component.translatable("menu.quit"), button -> {
            Wrapper.INSTANCE.getMinecraft().stop();
        }));
        this.addRenderableWidget(new Button(2, height - 22, 100, 20, Component.translatable("Changelog"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new ChangelogScreen());
        }));
    }

    private void switchToRealms() {
        this.minecraft.setScreen(new RealmsMainScreen(this));
    }

    float capeYaw = 0;

    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (this.backgroundFadeStart == 0L && this.doBackgroundFade) {
            this.backgroundFadeStart = Util.getMillis();
        }
        if (customMainMenu.scroll && stopWatch.hasPassed(customMainMenu.scrollDelay * 1000L)) {
            background++;
            if (background < 0) {
                background = backgrounds.size() - 1;
            } else if (background > backgrounds.size() - 1) {
                background = 0;
            }
            stopWatch.reset();
        }
        isDonator = Addon.isDonator(Wrapper.INSTANCE.getMinecraft().getUser().getUuid().replace("-", ""));
        int midX = Render2DHelper.INSTANCE.getScaledWidth() / 2;
        float f = this.doBackgroundFade ? (float) (Util.getMillis() - this.backgroundFadeStart) / 1000.0F : 1.0F;
        fill(matrices, 0, 0, this.width, this.height, -1);
        this.backgroundRenderer.render(delta, Mth.clamp(f, 0.0F, 1.0F));
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderTexture(0, PANORAMA_OVERLAY);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.doBackgroundFade ? (float) Mth.ceil(Mth.clamp(f, 0.0F, 1.0F)) : 1.0F);
        blit(matrices, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
        float g = this.doBackgroundFade ? Mth.clamp(f - 1.0F, 0.0F, 1.0F) : 1.0F;
        int l = Mth.ceil(g * 255.0F) << 24;

        if (!JexTitleScreen.backgrounds.isEmpty() && customMainMenu.customBackground) {
            Background currentBackground = backgrounds.get(background);
            Render2DHelper.INSTANCE.bindTexture(currentBackground.identifier);
            GuiComponent.blit(matrices, (int) 0, (int) 0, 0, 0, width, height, width, height);
        }

        if ((l & -67108864) != 0) {
            RenderSystem.setShaderTexture(0, JEX_TITLE_TEXTURE);
            int j1 = this.height / 4 - 10;
            Render2DHelper.INSTANCE.shaderColor(ColorHelper.INSTANCE.getClientColor());
            blit(matrices, 2, (int) j1, 0.0F, 0.0F, 250, 50, 250, 50);

            this.splashText = isMinceraft ? "Minceraft" : "Build " + JexClient.INSTANCE.getVersion().version() + " for MC" + SharedConstants.getCurrentVersion().getName();
            matrices.pushPose();
            float h = 1.8F - Mth.abs(Mth.sin((float)(Util.getMillis() % 1000L) / 1000.0F * 6.2831855F) * 0.1F);
            h = h * 100.0F / (float)(this.font.width(this.splashText) + 32);
            matrices.scale(h, h, h);
            FontHelper.INSTANCE.drawWithShadow(matrices, splashText, 2 / h, (j1 + 44) / h, ColorHelper.INSTANCE.getClientColor());
            matrices.popPose();

            if (UpdateManager.INSTANCE.getStatus() == UpdateManager.Status.OUTDATED || UpdateManager.INSTANCE.getStatus() == UpdateManager.Status.OUTDATED_BOTH) {
                String updateString = "Jex Client is outdated. You can open the Jex Options screen in Options to update to Build " + UpdateManager.INSTANCE.getLatestVersion().version();
                float strWidth = FontHelper.INSTANCE.getStringWidth(updateString);
                Render2DHelper.INSTANCE.fillAndBorder(matrices, (midX) - (strWidth / 2) - 2, -1, (midX) + (strWidth / 2) + 2, 15, ColorHelper.INSTANCE.getClientColor(), 0x80000000, 1);
                FontHelper.INSTANCE.drawCenteredString(matrices, updateString, midX, 2, ColorHelper.INSTANCE.getClientColor());
            }

            if (customMainMenu.customBackground) {
                if (backgrounds.isEmpty()) {
                    String backgroundString = "You don't have any backgrounds yet.";
                    FontHelper.INSTANCE.drawWithShadow(matrices, backgroundString, width - FontHelper.INSTANCE.getStringWidth(backgroundString) - 2, height - 30, -1);
                } else {
                    String backgroundString = "Background: (" + (background + 1) + "/" + backgrounds.size() + ")";
                    FontHelper.INSTANCE.drawWithShadow(matrices, backgroundString, width - FontHelper.INSTANCE.getStringWidth(backgroundString) - 2, height - 30, -1);
                }
            }

            for (net.minecraft.client.gui.components.events.GuiEventListener element : this.children()) {
                AbstractWidget abstractButtonWidget = (AbstractWidget) element;
                abstractButtonWidget.setAlpha(g);
            }
            float top = this.height / 4.f + 45;
            float bottom = top + (24 * 5) + 2;
            float left = -1;
            float right = 205;

            Render2DHelper.INSTANCE.drawFace(matrices, 2, (int)bottom + 2, 4, MCAPIHelper.INSTANCE.getPlayerSkin(Wrapper.INSTANCE.getMinecraft().getUser().getGameProfile().getId()));
            FontHelper.INSTANCE.drawWithShadow(matrices, "\2477Welcome, " + (isDonator ? "\247r" : (Addon.isLinkedToAccount(Wrapper.INSTANCE.getMinecraft().getUser().getUuid().replace("-", "")) ? "\247a" : "\247f")) + Wrapper.INSTANCE.getMinecraft().getUser().getName(), 37, bottom + 2, ColorHelper.INSTANCE.getRainbowColor());
            if (Addon.isLinkedToAccount(Wrapper.INSTANCE.getMinecraft().getUser().getUuid().replace("-", ""))) {
                FontHelper.INSTANCE.drawWithShadow(matrices, "\2477Jex Utility Client", 37, bottom + 12, -1);
                Addon.AddonResponse response = Addon.getResponse(Wrapper.INSTANCE.getMinecraft().getUser().getUuid().replace("-", ""));
                try {
                    if (response.getCape() != null && !response.getCape().isEmpty() && !response.getCape().equalsIgnoreCase("null")) {
                        Render2DHelper.INSTANCE.draw3DCape(matrices, 2, bottom+ 35, new ResourceLocation("assets/jex", "capes/" + Wrapper.INSTANCE.getMinecraft().getUser().getUuid().replace("-", "")), capeYaw, 0);
                    } else {
                        Render2DHelper.INSTANCE.draw3DCape(matrices, 2, bottom+ 35, new ResourceLocation("assets/jex", "cape/jex_cape.png"), capeYaw, 0);
                    }
                }catch (Exception e) {}
            } else {
                if (Cape.capes.containsKey("self")) {
                    Render2DHelper.INSTANCE.draw3DCape(matrices, 2, bottom+ 35, Cape.capes.get("self"), capeYaw, 0);
                }
                FontHelper.INSTANCE.drawWithShadow(matrices, "\2477Account not linked. You can link your account for a free Jex Cape", 37, bottom + 12, -1);
                FontHelper.INSTANCE.drawWithShadow(matrices, "\2477Also join the Discord!", 37, bottom + 22, -1);
            }
            Render2DHelper.INSTANCE.fillAndBorder(matrices, left, top, right, bottom, ColorHelper.INSTANCE.getClientColor(), 0x40000000, 1);
            super.render(matrices, mouseX, mouseY, delta);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void removed() {
        if (this.realmsNotificationGui != null) {
            this.realmsNotificationGui.removed();
        }

    }

    public boolean backgroundExists(String name) {
        for (Background background : JexTitleScreen.backgrounds) {
            if (background.name.equalsIgnoreCase(name))
                return true;
        }
        return false;
    }

    public void loadBackgrounds() throws IOException {
        File backgroundsFolder = new File(ModFileHelper.INSTANCE.getJexDirectory(), "backgrounds");
        if (!backgroundsFolder.exists()) {
            backgroundsFolder.mkdir();
            return;
        }
        for (File file : backgroundsFolder.listFiles()) {
            if (backgroundExists(file.getName().replaceAll("-", "").replaceAll(" ", "").toLowerCase()))
                continue;
            if (!file.isDirectory()) {
                byte[] fileContent = FileUtils.readFileToByteArray(file);
                String encodedString = Base64.encodeBase64String(fileContent);
                try {
                    parseImage(encodedString, file.getName().replaceAll("-", "").replaceAll(" ", "").toLowerCase());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void parseImage(String image, String name) {
        NativeImage image1 = readTexture(image);
        int imageWidth = image1.getWidth();
        int imageHeight = image1.getHeight();

        NativeImage imgNew = new NativeImage(imageWidth, imageHeight, true);
        for (int x = 0; x < image1.getWidth(); x++) {
            for (int y = 0; y < image1.getHeight(); y++) {
                imgNew.setPixelRGBA(x, y, image1.getPixelRGBA(x, y));
            }
        }

        image1.close();
        ResourceLocation id = new ResourceLocation("jex", "background/" + name);
        applyTexture(id, imgNew);
        JexTitleScreen.backgrounds.add(new Background(name, imageWidth, imageHeight, id));
    }

    private NativeImage readTexture(String textureBase64) {
        try {
            byte[] imgBytes = Base64.decodeBase64(textureBase64);
            ByteArrayInputStream bias = new ByteArrayInputStream(imgBytes);
            return NativeImage.read(bias);
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }

    private void applyTexture(ResourceLocation identifier, NativeImage nativeImage) {
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().getTextureManager().register(identifier, new DynamicTexture(nativeImage)));
    }


    public class Background {
        private String name;
        private int width, height;
        private ResourceLocation identifier;

        public Background(String name, int width, int height, ResourceLocation identifier) {
            this.name = name;
            this.width = width;
            this.height = height;
            this.identifier = identifier;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }
}
