package me.dustin.jex;

import me.dustin.events.api.EventAPI;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.command.CommandManager;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.file.ModuleFile;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.math.TPSHelper;
import me.dustin.jex.helper.misc.BaritoneHelper;
import me.dustin.jex.helper.misc.Lagometer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.ModuleManager;
import me.dustin.jex.module.impl.combat.Killaura;
import me.dustin.jex.module.impl.player.Freecam;
import me.dustin.jex.option.OptionManager;
import me.dustin.jex.update.UpdateManager;

public enum JexClient {
    INSTANCE;
    private String version = "0.1.5.1";
    private boolean autoSaveModules = false;

    public void initializeClient() {
        System.out.println("Loading Jex Client");
        EventAPI.getInstance().setPrivateOnly(true);

        if (BaritoneHelper.INSTANCE.baritoneExists()) {
            BaritoneHelper.INSTANCE.initBaritoneProcesses();
        }

        ModuleManager.INSTANCE.initializeModuleManager();
        OptionManager.INSTANCE.initializeOptionManager();

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

    @EventListener(events = {EventKeyPressed.class})
    public void keyPress(EventKeyPressed eventKeyPressed) {
        if (eventKeyPressed.getType() == EventKeyPressed.PressType.IN_GAME) {
            ModuleManager.INSTANCE.getModules().values().forEach(module -> {
                if (module.getKey() == eventKeyPressed.getKey()) {
                    module.toggleState();
                    if (JexClient.INSTANCE.isAutoSaveEnabled())
                        ModuleFile.write();
                }
            });
        }
    }

    @EventListener(events = {EventTick.class})
    public void tick(EventTick eventTick) {
        Wrapper.INSTANCE.getWindow().setTitle("Jex Client " + getVersion());
        if (Wrapper.INSTANCE.getLocalPlayer() == null) {
            if (Module.get(Killaura.class).getState())
                Module.get(Killaura.class).setState(false);
            if (Module.get(Freecam.class).getState())
                Module.get(Freecam.class).setState(false);
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
}
