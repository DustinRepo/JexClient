package me.dustin.jex.helper.file;

import me.dustin.jex.JexClient;
import me.dustin.jex.helper.file.files.*;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.impl.world.xray.Xray;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;

public enum ModFileHelper {
    INSTANCE;

    private String PATH_NAME = "JexClient";
    private boolean firstLoad;

    public void gameBootLoad() {
        File jexDir = getJexDirectory();
        if (!jexDir.exists()) { //First load.
            firstLoad = true;
            jexDir.mkdirs();
            return;
        }
        File xrayFile = new File(ModFileHelper.INSTANCE.getJexDirectory(), "Xray.json");
        if (!xrayFile.exists()) {
            Xray.firstLoad();
            XrayFile.write();
        }
        ClientSettingsFile.read();
        FeatureFile.read();
        FriendFile.read();
        SearchFile.read();
        WaypointFile.read();
        XrayFile.read();
    }

    public void closeGame() {
        ClientSettingsFile.write();
        HudElementsFile.write();
        if (JexClient.INSTANCE.isAutoSaveEnabled())
            FeatureFile.write();
    }

    public boolean isFirstTimeLoading() {
        return firstLoad;
    }

    public File getJexDirectory() {
        if (Wrapper.INSTANCE.getMinecraft() == null)
            return new File(FabricLoader.getInstance().getGameDir().toString(), PATH_NAME);
        return new File(Wrapper.INSTANCE.getMinecraft().runDirectory, PATH_NAME);
    }

}
