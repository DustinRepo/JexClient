package me.dustin.jex.helper.update;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.dustin.jex.JexClient;
import me.dustin.jex.helper.network.WebHelper;
import net.minecraft.SharedConstants;

import java.io.IOException;
import java.net.URL;

public enum UpdateManager {
    INSTANCE;
    private Status status;
    private String latestVersion;
    private String latestMCVersion;
    private String latestSnapshotVersion;

    public void checkForUpdate() {
        new Thread(() -> {
            try {
                URL url = new URL("https://jexclient.com/includes/version.inc.php");
                String response = WebHelper.INSTANCE.readURL(url);

                JsonObject updateResponse = new Gson().fromJson(response, JsonObject.class);
                latestMCVersion = updateResponse.get("mcVersion").getAsString();
                latestVersion = updateResponse.get("version").getAsString();
                latestSnapshotVersion = updateResponse.get("snapVersion").getAsString();

                boolean isCurrentlySnapshot = SharedConstants.getGameVersion().getName().contains("w");
                boolean isVersionSame = JexClient.INSTANCE.getVersion().equalsIgnoreCase(latestVersion);
                boolean isMCVersionSame = SharedConstants.getGameVersion().getName().equalsIgnoreCase(isCurrentlySnapshot ? latestSnapshotVersion : latestMCVersion);
                if (isVersionSame && isMCVersionSame)
                    status = Status.UP_TO_DATE;
                if (isVersionSame && !isMCVersionSame)
                    status = Status.OUTDATED_MC;
                if (isMCVersionSame && !isVersionSame)
                    status = Status.OUTDATED;
                if (!isMCVersionSame && !isVersionSame)
                    status = Status.OUTDATED_BOTH;
            } catch (IOException e) {
                status = Status.ERROR;
            }
        }).start();
    }

    public Status getStatus() {
        return status;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getLatestMCVersion() {
        return latestMCVersion;
    }

    public static enum Status {
        OUTDATED, OUTDATED_MC, OUTDATED_BOTH, UP_TO_DATE, ERROR;
    }
}
