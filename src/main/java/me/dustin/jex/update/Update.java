package me.dustin.jex.update;

import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.SharedConstants;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public enum Update {
    INSTANCE;
    private String progressText;
    private float progress;

    public void update(String jexVer) {
        if (SharedConstants.getGameVersion().getName().contains("w") && (UpdateManager.INSTANCE.getStatus() == UpdateManager.Status.OUTDATED_MC || UpdateManager.INSTANCE.getStatus() == UpdateManager.Status.OUTDATED_BOTH)) {
            progressText = "Error. New version for another snapshot, and can not run on this Fabric version";
            return;
        }
        String downloadURL = "https://jexclient.com/download/JexClient" + (SharedConstants.getGameVersion().getName().contains("w") ? "-Snap.jar" : ".jar");
        String mcLoc = Wrapper.INSTANCE.getMinecraft().runDirectory.getAbsolutePath();

        new Thread(() -> {
            try {
                progressText = "Downloading client";
                FileUtils.copyURLToFile(new URL(downloadURL), new File(mcLoc + File.separator + "mods", "JexClient" + (SharedConstants.getGameVersion().getName().contains("w") ? "-Snap.jar" : ".jar")));
                progressText = "Update complete. Closing Minecraft...";
                try {
                    Thread.sleep(3000);
                    Wrapper.INSTANCE.getMinecraft().scheduleStop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {

            }
        }).start();
    }

    public String getProgressText() {
        return progressText;
    }

    public float getProgress() {
        return progress;
    }
}
