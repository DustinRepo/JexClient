package me.dustin.jex.gui.jex;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.addon.Addon;
import me.dustin.jex.feature.command.CommandManagerJex;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.Gui;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.ClientSettingsFile;
import me.dustin.jex.gui.changelog.ChangelogScreen;
import me.dustin.jex.gui.jex.selection.SearchSelectScreen;
import me.dustin.jex.gui.jex.selection.XraySelectScreen;
import me.dustin.jex.gui.jex.selection.AutoDropSelectScreen;
import me.dustin.jex.gui.plugin.JexPluginScreen;
import me.dustin.jex.gui.waypoints.WaypointScreen;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.update.Update;
import me.dustin.jex.helper.update.UpdateManager;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class JexOptionsScreen extends Screen {

    private EditBox prefixField;
    private Button setPrefixButton;
    private Button clickGuiButton;
    private Button downloadInstallerButton;
    private Button xrayButton;
    private Button searchButton;
    private Button autoDropButton;
    private Button waypointScreenButton;
    private Button reloadAddonsButton;
    private Button pluginManagerButton;
    private Button changelogButton;
    private Button personalSettingsButton;
    private static StopWatch stopWatch = new StopWatch();
    private boolean updating = false;
    public JexOptionsScreen() {
        super(Component.literal("Jex Client"));
    }

    @Override
    protected void init() {
        int centerX = Render2DHelper.INSTANCE.getScaledWidth() / 2;
        int centerY = Render2DHelper.INSTANCE.getScaledHeight() / 2;
        int topY = centerY - 100;
        prefixField = new EditBox(Wrapper.INSTANCE.getTextRenderer(), centerX - 55, topY, 50, 20, Component.literal(CommandManagerJex.INSTANCE.getPrefix()));
        prefixField.setMaxLength(1);
        prefixField.setValue(CommandManagerJex.INSTANCE.getPrefix());
        prefixField.setVisible(true);
        setPrefixButton = new Button(centerX + 1, topY, 54, 20, Component.literal("Set Prefix"), button -> {
            CommandManagerJex.INSTANCE.setPrefix(prefixField.getValue());
            ConfigManager.INSTANCE.get(ClientSettingsFile.class).write();
        });
        //left
        downloadInstallerButton = new Button(centerX - 230, topY + 25, 150, 20, Component.literal("Update Jex to " + (UpdateManager.INSTANCE.getLatestVersion() != null ? UpdateManager.INSTANCE.getLatestVersion().version() : "null")), button -> {
            Update.INSTANCE.update();
            updating = true;
        });
        changelogButton = new Button(centerX - 230, topY + 50, 150, 20, Component.literal("Changelog"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new ChangelogScreen());
        });
        personalSettingsButton = new Button(centerX - 230, topY + 75, 150, 20, Component.literal("Personal Cosmetics"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new JexPersonalizationScreen(this));
        });
        downloadInstallerButton.active = UpdateManager.INSTANCE.getStatus() == UpdateManager.Status.OUTDATED || UpdateManager.INSTANCE.getStatus() == UpdateManager.Status.OUTDATED_BOTH;


        //middle
        waypointScreenButton = new Button(centerX - 75, topY + 25, 150, 20, Component.literal("Waypoint Screen"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new WaypointScreen());
        });
        clickGuiButton = new Button(centerX - 75, topY + 50, 150, 20, Component.literal("Open ClickGUI"), button -> {
            Feature.get(Gui.class).setState(true);
        });
        reloadAddonsButton = new Button(centerX - 75, topY + 75, 150, 20, Component.literal("Reload Capes and Hats"), button -> {
            Addon.clearAddons();
            if (Wrapper.INSTANCE.getWorld() != null) {
                Wrapper.INSTANCE.getWorld().entitiesForRendering().forEach(entity -> {
                    if (entity instanceof Player playerEntity) {
                        Addon.loadAddons(playerEntity);
                    }
                });
            }
            stopWatch.reset();
        });
        pluginManagerButton = new Button(centerX - 75, topY + 100, 150, 20, Component.literal("Plugin Manager"), button -> Wrapper.INSTANCE.getMinecraft().setScreen(new JexPluginScreen(this)));

        //right
        xrayButton = new Button(centerX + 80, topY + 25, 150, 20, Component.literal("Xray Block Selection"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new XraySelectScreen());
        });
        searchButton = new Button(centerX + 80, topY + 50, 150, 20, Component.literal("Search Block Selection"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new SearchSelectScreen());
        });
        autoDropButton = new Button(centerX + 80, topY + 75, 150, 20, Component.literal("AutoDrop Selection"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new AutoDropSelectScreen());
        });

        this.addRenderableWidget(setPrefixButton);
        this.addRenderableWidget(clickGuiButton);
        this.addRenderableWidget(downloadInstallerButton);
        this.addRenderableWidget(xrayButton);
        this.addRenderableWidget(searchButton);
        this.addRenderableWidget(autoDropButton);
        this.addRenderableWidget(reloadAddonsButton);
        this.addRenderableWidget(pluginManagerButton);
        this.addRenderableWidget(waypointScreenButton);
        this.addRenderableWidget(changelogButton);
        this.addRenderableWidget(personalSettingsButton);
        this.addWidget(prefixField);
        super.init();
    }

    @Override
    public void tick() {
        prefixField.tick();
        super.tick();
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        prefixField.render(matrices, mouseX, mouseY, delta);
        setPrefixButton.active = !prefixField.getValue().isEmpty();
        if (!stopWatch.hasPassed(30 * 1000)) {
            reloadAddonsButton.setMessage(Component.literal("Reload Capes and Hats (" + ( 30 - ((stopWatch.getCurrentMS() - stopWatch.getLastMS()) / 1000)) + ")"));
            reloadAddonsButton.active = false;
        } else {
            reloadAddonsButton.setMessage(Component.literal("Reload Capes and Hats"));
            reloadAddonsButton.active = true;
        }
        if (updating) {
            int topY = (height / 2) - 100;
            FontHelper.INSTANCE.drawCenteredString(matrices, Update.INSTANCE.getProgressText() + " \247f" + ClientMathHelper.INSTANCE.roundToPlace(Update.INSTANCE.getProgress() * 100.0F, 2) + "%", width / 2, topY - 20, ColorHelper.INSTANCE.getClientColor());
            float leftX = (width / 2) - 100;
            float pos = 200 * Update.INSTANCE.getProgress();
            Render2DHelper.INSTANCE.fill(matrices, leftX, topY - 10, leftX + 200, topY - 8, 0xff000000);
            Render2DHelper.INSTANCE.fill(matrices, leftX, topY - 10, leftX + pos, topY - 8, ColorHelper.INSTANCE.getClientColor());
        }
        super.render(matrices, mouseX, mouseY, delta);
    }
}
