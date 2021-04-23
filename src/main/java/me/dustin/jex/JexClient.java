package me.dustin.jex;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dustin.events.api.EventAPI;
import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.command.CommandManager;
import me.dustin.jex.event.misc.EventGameFinishedLoading;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.event.misc.EventScheduleStop;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.file.FeatureFile;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.math.TPSHelper;
import me.dustin.jex.helper.misc.BaritoneHelper;
import me.dustin.jex.helper.misc.Lagometer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.FeatureManager;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.feature.impl.combat.killaura.Killaura;
import me.dustin.jex.feature.impl.misc.Discord;
import me.dustin.jex.feature.impl.misc.Fakelag;
import me.dustin.jex.feature.impl.player.Freecam;
import me.dustin.jex.feature.impl.render.Gui;
import me.dustin.jex.option.OptionManager;
import me.dustin.jex.update.UpdateManager;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.sound.SoundEvents;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public enum JexClient {
    INSTANCE;
    private String version = "0.2.6";
    private boolean autoSaveModules = false;
    private boolean soundOnLaunch = true;

    public void initializeClient() {
        System.out.println("Loading Jex Client");
        EventAPI.getInstance().setPrivateOnly(true);

        if (BaritoneHelper.INSTANCE.baritoneExists()) {
            BaritoneHelper.INSTANCE.initBaritoneProcesses();
        }

        FeatureManager.INSTANCE.initializeFeatureManager();
        OptionManager.INSTANCE.initializeOptionManager();

        //createJson();

        ModFileHelper.INSTANCE.gameBootLoad();
        CommandManager.INSTANCE.init();

        EventAPI.getInstance().register(this);
        EventAPI.getInstance().register(TPSHelper.INSTANCE);
        EventAPI.getInstance().register(Lagometer.INSTANCE);
        EventAPI.getInstance().register(WorldHelper.INSTANCE);
        EventAPI.getInstance().register(PlayerHelper.INSTANCE);
        EventAPI.getInstance().register(ColorHelper.INSTANCE);
        UpdateManager.INSTANCE.checkForUpdate();
        System.out.println("Load finished");
    }

    @EventListener(events = {EventKeyPressed.class, EventTick.class, EventScheduleStop.class, EventGameFinishedLoading.class})
    public void runMethod(Event event) {
        if (event instanceof EventKeyPressed) {
            EventKeyPressed eventKeyPressed = (EventKeyPressed)event;
            if (eventKeyPressed.getType() == EventKeyPressed.PressType.IN_GAME) {
                FeatureManager.INSTANCE.getFeatures().forEach(module -> {
                    if (module.getKey() == eventKeyPressed.getKey()) {
                        module.toggleState();
                        if (JexClient.INSTANCE.isAutoSaveEnabled())
                            FeatureFile.write();
                    }
                });
            }
        } else if (event instanceof EventTick) {
            Wrapper.INSTANCE.getWindow().setTitle("Jex Client " + getVersion());
            if (Wrapper.INSTANCE.getLocalPlayer() == null) {
                if (Feature.get(Killaura.class).getState())
                    Feature.get(Killaura.class).setState(false);
                if (Feature.get(Freecam.class).getState())
                    Feature.get(Freecam.class).setState(false);
                if (Feature.get(Fakelag.class).getState())
                    Feature.get(Fakelag.class).setState(false);
            } else if (Gui.clickgui.guiModule == null) {
                Gui.clickgui.init();
            }
        } else if (event instanceof EventScheduleStop) {
            if (Feature.get(Discord.class).getState()) {
                Feature.get(Discord.class).setState(false);
            }
            ModFileHelper.INSTANCE.closeGame();
        } else if (event instanceof EventGameFinishedLoading && playSoundOnLaunch()) {
            Wrapper.INSTANCE.getMinecraft().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0F));
        }
    }

    public String getVersion() {
        return version;
    }

    public boolean isAutoSaveEnabled() {
        return autoSaveModules;
    }

    public void setAutoSave(boolean autoSave) {
        this.autoSaveModules = autoSave;
    }

    public boolean playSoundOnLaunch() {
        return soundOnLaunch;
    }

    public void setPlaySoundOnLaunch(boolean soundOnLaunch) {
        this.soundOnLaunch = soundOnLaunch;
    }

    private void createJson() {
        JsonObject jsonObject = new JsonObject();
        for (FeatureCategory featureCategory : FeatureCategory.values()) {
            JsonArray categoryArray = new JsonArray();
            for (Feature feature : Feature.getModules(featureCategory)) {
                JsonObject object = new JsonObject();
                object.addProperty("name", feature.getName());
                object.addProperty("description", feature.getDescription());
                object.addProperty("key", (GLFW.glfwGetKeyName(feature.getKey(), 0) == null ? InputUtil.fromKeyCode(feature.getKey(), 0).getTranslationKey().replace("key.keyboard.", "").replace(".", "_") : GLFW.glfwGetKeyName(feature.getKey(), 0).toUpperCase()).toUpperCase().replace("key.keyboard.", "").replace(".", "_"));
                object.addProperty("enabled", feature.getState());
                object.addProperty("visible", feature.isVisible());
                categoryArray.add(object);
            }
            jsonObject.add(featureCategory.name(), categoryArray);
        }

        ArrayList<String> stringList = new ArrayList<>(Arrays.asList(JsonHelper.INSTANCE.prettyGson.toJson(jsonObject).split("\n")));

        try {
            FileHelper.INSTANCE.writeFile(ModFileHelper.INSTANCE.getJexDirectory(), "dev" + File.separator + "mods.json", stringList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
