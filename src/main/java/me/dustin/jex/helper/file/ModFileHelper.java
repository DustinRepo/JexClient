package me.dustin.jex.helper.file;

import me.dustin.jex.feature.mod.impl.render.Search;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.SearchFile;
import me.dustin.jex.file.impl.XrayFile;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.impl.world.xray.Xray;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;

public enum ModFileHelper {
    INSTANCE;

    private final String PATH_NAME = "JexClient";
    private boolean firstLoad;

    public void gameBootLoad() {
        ConfigManager.INSTANCE.init();

        File jexDir = getJexDirectory();
        if (!jexDir.exists()) { //First load.
            firstLoad = true;
            jexDir.mkdirs();

            Xray.firstLoad();
            Search.firstLoad();
            ConfigManager.INSTANCE.get(XrayFile.class).write();
            ConfigManager.INSTANCE.get(SearchFile.class).write();
            return;
        }
        ConfigManager.INSTANCE.getConfigFiles().forEach(configFile -> {
            if (configFile.doesReadOnBoot())
                configFile.read();
        });
    }

    public void closeGame() {
        ConfigManager.INSTANCE.saveAll();
    }

    public boolean isFirstTimeLoading() {
        return firstLoad;
    }

    public File getJexDirectory() {
        if (Wrapper.INSTANCE.getMinecraft() == null)
            return new File(FabricLoader.getInstance().getGameDir().toString(), PATH_NAME);
        return new File(Wrapper.INSTANCE.getMinecraft().gameDirectory, PATH_NAME);
    }

}
