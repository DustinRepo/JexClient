package me.dustin.jex.update;

import me.dustin.jex.helper.misc.Wrapper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public enum Update {
    INSTANCE;
    private String progressText;
    private float progress;

    public void update(String jexVer) {
        String downloadURL = "https://jexclient.com/download/JexClient.jar";
        String mcLoc = Wrapper.INSTANCE.getMinecraft().runDirectory.getAbsolutePath();

        new Thread(() -> {
            try {
                progressText = "Downloading client";
                FileUtils.copyURLToFile(new URL(downloadURL), new File(mcLoc + File.separator + "mods", "JexClient.jar"));
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
