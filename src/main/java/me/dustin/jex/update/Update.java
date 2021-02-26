package me.dustin.jex.update;

import me.dustin.jex.helper.file.FileHelper;
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
    private String downloadURLBase = "https://jexclient.com/download/";
    private String libsURL = "https://jexclient.com/download/libraries.zip";

    public void update(String jexVer) {
        String mcVer = SharedConstants.getGameVersion().getName();
        String downloadURL = downloadURLBase + mcVer + "/" + jexVer + ".zip";
        String mcLoc = Wrapper.INSTANCE.getMinecraft().runDirectory.getAbsolutePath();
        File jexFile = new File(mcLoc, "jex.zip");
        File libsFile = new File(mcLoc, "libs.zip");

        new Thread(() -> {
            try {
                progressText = "Downloading client";
                FileUtils.copyURLToFile(new URL(downloadURL), jexFile);
                progress = 0.45f;
                progressText = "Downloading libraries";
                FileUtils.copyURLToFile(new URL(libsURL), libsFile);
                progress = 0.8f;

                //Extract them
                progressText = "Extracting client and libraries";
                FileHelper.INSTANCE.unzip(libsFile.getAbsolutePath(), mcLoc);
                FileHelper.INSTANCE.unzip(jexFile.getAbsolutePath(), mcLoc);
                progress = 0.9f;

                progressText = "Finished. Cleaning up";
                //Cleanup
                jexFile.delete();
                libsFile.delete();
                progress = 1f;
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
