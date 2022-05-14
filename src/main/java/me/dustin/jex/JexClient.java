package me.dustin.jex;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dustin.events.core.EventListener;
import me.dustin.jex.event.filters.KeyPressFilter;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.*;
import me.dustin.jex.feature.keybind.Keybind;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.core.FeatureManager;
import me.dustin.jex.feature.mod.impl.combat.killaura.KillAura;
import me.dustin.jex.feature.mod.impl.misc.Fakelag;
import me.dustin.jex.feature.mod.impl.movement.Step;
import me.dustin.jex.feature.mod.impl.player.Freecam;
import me.dustin.jex.feature.mod.impl.player.Jesus;
import me.dustin.jex.feature.mod.impl.render.CustomFont;
import me.dustin.jex.gui.changelog.changelog.JexChangelog;
import me.dustin.jex.gui.waypoints.WaypointScreen;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.math.TPSHelper;
import me.dustin.jex.helper.baritone.BaritoneHelper;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.Lagometer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.ConnectedServerHelper;
import me.dustin.jex.helper.network.ProxyHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.EntityPositionHelper;
import me.dustin.jex.helper.update.JexVersion;
import me.dustin.jex.helper.world.PathingHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.option.OptionManager;
import me.dustin.jex.helper.update.UpdateManager;
import me.dustin.events.EventManager;
import me.dustin.events.core.annotate.EventPointer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.SharedConstants;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.sounds.SoundEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.*;

public enum JexClient {
    INSTANCE;
    private boolean autoSaveModules = false;
    private boolean soundOnLaunch = true;
    private final Logger logger = LogManager.getFormatterLogger("Jex");
    private JexVersion version;
    private final String baseUrl = "https://google.com/";

    private static boolean loadedOnce = false;

    public void initializeClient() {
        if (loadedOnce)
            return;
        getLogger().info("Loading Jex Client");

        if (BaritoneHelper.INSTANCE.baritoneExists()) {
            getLogger().info("Creating Baritone processes");
            BaritoneHelper.INSTANCE.initBaritoneProcesses();
        }

        getLogger().info("Initializing Features");
        FeatureManager.INSTANCE.initializeFeatureManager();
        getLogger().info("Initializing Options");
        OptionManager.INSTANCE.initializeOptionManager();
        getLogger().info("Reading Config Files");
        ModFileHelper.INSTANCE.gameBootLoad();

        EventManager.register(this);
        EventManager.register(TPSHelper.INSTANCE);
        EventManager.register(Lagometer.INSTANCE);
        EventManager.register(ProxyHelper.INSTANCE);
        EventManager.register(WorldHelper.INSTANCE);
        EventManager.register(PlayerHelper.INSTANCE);
        EventManager.register(InventoryHelper.INSTANCE);
        EventManager.register(ColorHelper.INSTANCE);
        EventManager.register(EntityPositionHelper.INSTANCE);
        EventManager.register(PathingHelper.INSTANCE);
        EventManager.register(ConnectedServerHelper.INSTANCE);
        getLogger().info("Checking for update");
        UpdateManager.INSTANCE.checkForUpdate();
        CustomFont.INSTANCE.loadFont();
        JexChangelog.loadChangelogList();
        getLogger().info("Jex load finished.");
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            getLogger().info("Creating mods.json for website.");
            createJson();

            //for the entity id lists made for ChatBot
            /*int i = 0;
            ArrayList<String> l = new ArrayList<>();
            for (EntityType<?> entityType : Registry.ENTITY_TYPE) {
                l.add(i + "=" + ((TranslatableText)entityType.getName()).getKey());
                i++;
            }
            FileHelper.INSTANCE.writeFile(new File(ModFileHelper.INSTANCE.getJexDirectory(), SharedConstants.getGameVersion().getName() + "_entity_ids.txt"), l);*/
            createVersionsJson();
        }
        loadedOnce = true;
    }

    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
        //TODO: create an event for this in the setTitle method to avoid it flashing the title and make it optional
        //Wrapper.INSTANCE.getWindow().setTitle("Jex Client " + getVersion().version());
        if (Wrapper.INSTANCE.getLocalPlayer() == null) {
            if (Feature.getState(KillAura.class))
                Feature.get(KillAura.class).setState(false);
            if (Feature.getState(Freecam.class))
                Feature.get(Freecam.class).setState(false);
            if (Feature.getState(Fakelag.class))
                Feature.get(Fakelag.class).setState(false);
        }
        if (BaritoneHelper.INSTANCE.baritoneExists()) {
            BaritoneHelper.INSTANCE.setAssumeStep(Feature.getState(Step.class));
            BaritoneHelper.INSTANCE.setAssumeJesus(Feature.getState(Jesus.class));
        }
    }, new TickFilter(EventTick.Mode.PRE));

    @EventPointer
    private final EventListener<EventKeyPressed> eventKeyPressedEventListener = new EventListener<>(event -> {
        if (event.getKey() == GLFW.GLFW_KEY_INSERT)
            Wrapper.INSTANCE.getMinecraft().setScreen(new WaypointScreen());
        Keybind.get(event.getKey()).forEach(Keybind::execute);
    }, new KeyPressFilter(EventKeyPressed.PressType.IN_GAME));

    @EventPointer
    private final EventListener<EventStop> eventScheduleStopEventListener = new EventListener<>(event -> {
        ModFileHelper.INSTANCE.closeGame();
    });

    @EventPointer
    private final EventListener<EventGameFinishedLoading> eventGameFinishedLoadingEventListener = new EventListener<>(event -> {
        if (playSoundOnLaunch())
            Wrapper.INSTANCE.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.0F));
    });

    public ModContainer getModContainer() {
        return FabricLoader.getInstance().getModContainer("jex").orElse(null);
    }

    public JexVersion getVersion() {
        if (version == null) {
            String v;
            if (this.getModContainer().getMetadata().getVersion().getFriendlyString().equals("${version}")) {
                v = "0.0.0-unknown";
            } else {
                v = this.getModContainer().getMetadata().getVersion().getFriendlyString();
            }
            version = new JexVersion(v);
        }
        return version;
    }

    public Logger getLogger() {
        return logger;
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

    public String getBaseUrl() {
        return baseUrl;
    }

    private void createJson() {
        JsonObject jsonObject = new JsonObject();
        for (Feature.Category featureCategory : Feature.Category.values()) {
            JsonArray categoryArray = new JsonArray();
            for (Feature feature : Feature.getModules(featureCategory)) {
                JsonObject object = new JsonObject();
                object.addProperty("name", feature.getName());
                object.addProperty("description", feature.getDescription());
                object.addProperty("key", KeyboardHelper.INSTANCE.getKeyName(feature.getKey()));
                object.addProperty("enabled", feature.getState());
                object.addProperty("visible", feature.isVisible());
                categoryArray.add(object);
            }
            jsonObject.add(featureCategory.name(), categoryArray);
        }

        ArrayList<String> stringList = new ArrayList<>(Arrays.asList(JsonHelper.INSTANCE.prettyGson.toJson(jsonObject).split("\n")));

        try {
            FileHelper.INSTANCE.writeFile(new File(ModFileHelper.INSTANCE.getJexDirectory() + File.separator + "dev", "mods.json"), stringList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createVersionsJson() {
        JsonObject jsonObject = new JsonObject();
        JsonObject protocolObject = new JsonObject();
        JsonObject packets = new JsonObject();
        JsonArray c2s = new JsonArray();
        JsonArray s2c = new JsonArray();

        protocolObject.addProperty("name", SharedConstants.getCurrentVersion().getName());
        protocolObject.addProperty("protocol_id", SharedConstants.getProtocolVersion());

        String[] c2sPackets = new String[150];
        String[] s2cPackets = new String[150];
        ConnectionProtocol.PLAY.getPacketsByIds(PacketFlow.SERVERBOUND).forEach((integer, aClass) -> c2sPackets[integer] = aClass.getSimpleName().replace("Serverbound", "").replace("Packet", ""));
        ConnectionProtocol.PLAY.getPacketsByIds(PacketFlow.CLIENTBOUND).forEach((integer, aClass) -> s2cPackets[integer] = aClass.getSimpleName().replace("Clientbound", "").replace("Packet", ""));
        for (String c2sPacket : c2sPackets) {
            if (c2sPacket == null)
                break;
            c2s.add(c2sPacket);
        }
        for (String s2cPacket : s2cPackets) {
            if (s2cPacket == null)
                break;
            s2c.add(s2cPacket);
        }
        packets.add("c2s", c2s);
        packets.add("s2c", s2c);
        protocolObject.add("packets", packets);
        jsonObject.add("839", protocolObject);

        FileHelper.INSTANCE.writeFile(new File(ModFileHelper.INSTANCE.getJexDirectory(), SharedConstants.getCurrentVersion().getName() + "_packetIds.json"), List.of(JsonHelper.INSTANCE.prettyGson.toJson(jsonObject).split("\n")));
    }
}
