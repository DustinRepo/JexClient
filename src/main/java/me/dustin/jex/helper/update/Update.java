package me.dustin.jex.helper.update;

import me.dustin.jex.JexClient;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.SharedConstants;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public enum Update {
    INSTANCE;
    private String progressText;
    private float progress;

    public void update() {
        if (SharedConstants.getGameVersion().getName().contains("w") && (UpdateManager.INSTANCE.getStatus() == UpdateManager.Status.OUTDATED_MC || UpdateManager.INSTANCE.getStatus() == UpdateManager.Status.OUTDATED_BOTH)) {
            progressText = "Error. New version for another snapshot, and can not run on this Fabric version";
            return;
        }
        String downloadURL = JexClient.INSTANCE.getBaseUrl() + "download/JexClient" + (SharedConstants.getGameVersion().getName().contains("w") ? "-Snap.jar" : ".jar");
        String modsFolder = Wrapper.INSTANCE.getMinecraft().runDirectory.getAbsolutePath() + File.separator + "mods";

        new Thread(() -> {
            progressText = "Downloading Jex";
            try {
                String name = "JexClient" + (SharedConstants.getGameVersion().getName().contains("w") ? "-Snap.jar" : ".jar");
                download(downloadURL, modsFolder + File.separator + name);
                progressText = "Update complete. Closing Minecraft...";
                Thread.sleep(3000);
                Wrapper.INSTANCE.getMinecraft().scheduleStop();
            } catch (IOException | InterruptedException e) {
                progressText = "Error while downloading.";
                e.printStackTrace();
            }
        }).start();
    }


    public void download(String remotePath, String localPath) throws IOException {
        BufferedInputStream in = null;
        FileOutputStream out = null;

        URL url = new URL(remotePath);
        URLConnection conn = url.openConnection();
        int size = conn.getContentLength();

        if (size < 0) {
            JexClient.INSTANCE.getLogger().error("Could not get the file size");
        } else {
            JexClient.INSTANCE.getLogger().error("File size: " + size);
        }

        in = new BufferedInputStream(url.openStream());
        out = new FileOutputStream(localPath);
        byte data[] = new byte[1024];
        int count;
        double sumCount = 0.0;

        while ((count = in.read(data, 0, 1024)) != -1) {
            out.write(data, 0, count);

            sumCount += count;
            if (size > 0) {
                this.progress = (float) (sumCount / size);
            }
        }
        try {
            in.close();
        } catch (IOException e3) {
            e3.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e4) {
            e4.printStackTrace();
        }
    }

    public String getProgressText() {
        return progressText;
    }

    public float getProgress() {
        return progress;
    }
}
