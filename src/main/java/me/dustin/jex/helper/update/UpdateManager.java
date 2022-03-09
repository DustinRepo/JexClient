package me.dustin.jex.helper.update;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.dustin.jex.JexClient;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.WebHelper;
import net.minecraft.SharedConstants;

import java.io.IOException;
import java.net.URL;

public enum UpdateManager {
    INSTANCE;
    private Status status;
    private JexVersion latestVersion;
    private String latestMCVersion;
    private String latestSnapshotVersion;

    public void checkForUpdate() {
        new Thread(() -> {
            try {
                //lots of changes as I temporarily use GitHub for these things
                //URL url = new URL(JexClient.INSTANCE.getBaseUrl() + "includes/version.inc.php");
                String url = "https://raw.githubusercontent.com/DustinRepo/JexClient/main/JexChangelog.txt";
                String response = WebHelper.INSTANCE.httpRequest(url, null, null, "GET").data();
                latestVersion = new JexVersion(response.split("\n")[0]);
                JexClient.INSTANCE.getLogger().info(latestVersion.version());

                /*JsonObject updateResponse = new Gson().fromJson(response, JsonObject.class);
                latestMCVersion = updateResponse.get("mcVersion").getAsString();
                latestVersion = new JexVersion(updateResponse.get("version").getAsString());
                latestSnapshotVersion = updateResponse.get("snapVersion").getAsString();*/

                boolean isCurrentlySnapshot = SharedConstants.getGameVersion().getName().contains("w");
                boolean isVersionSame = JexClient.INSTANCE.getVersion().version().equalsIgnoreCase(latestVersion.version());
                boolean isMCVersionSame = true;//SharedConstants.getGameVersion().getName().equalsIgnoreCase(isCurrentlySnapshot ? latestSnapshotVersion : latestMCVersion);
                if (isVersionSame && isMCVersionSame)
                    status = Status.UP_TO_DATE;
                if (isVersionSame && !isMCVersionSame)
                    status = Status.OUTDATED_MC;
                if (isMCVersionSame && !isVersionSame) {
                    if (latestVersion.compareTo(JexClient.INSTANCE.getVersion()) > 0)
                        status = Status.OUTDATED;
                    else
                        status = Status.BETA;
                }
                if (!isMCVersionSame && !isVersionSame)
                    status = Status.OUTDATED_BOTH;
            } catch (Exception e) {
                status = Status.ERROR;
            }
        }).start();
    }

    public Status getStatus() {
        return status;
    }

    public JexVersion getLatestVersion() {
        return latestVersion;
    }

    public String getLatestMCVersion() {
        return latestMCVersion;
    }

    public static enum Status {
        OUTDATED, OUTDATED_MC, OUTDATED_BOTH, UP_TO_DATE, BETA, ERROR
    }
}
