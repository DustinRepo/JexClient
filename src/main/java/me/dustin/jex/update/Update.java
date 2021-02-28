package me.dustin.jex.update;

import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.Download;
import net.minecraft.SharedConstants;

import java.io.File;
import java.net.MalformedURLException;
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
                progressText = "Downloading client";
            Download download = null;
            try {
                download = new Download(new URL(downloadURL), new File(mcLoc + File.separator + "mods", "JexClient" + (SharedConstants.getGameVersion().getName().contains("w") ? "-Snap.jar" : ".jar")));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            if (download == null) {
                progressText = "Error updating.";
                return;
            }

            try {
                while (download.getStatus() == Download.DOWNLOADING) {
                    download.run();
                    progress = download.getProgress() / 100;//idk why this doesn't work even though I have SetEnv no-gzip dont-vary set in .htaccess
                }
                if (download.getStatus() == Download.COMPLETE)
                    progressText = "Update complete. Closing Minecraft...";
                if (download.getStatus() == Download.ERROR)
                    progressText = "Error updating.";
                Thread.sleep(3000);
                Wrapper.INSTANCE.getMinecraft().scheduleStop();
            } catch (InterruptedException e) {
                e.printStackTrace();
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
