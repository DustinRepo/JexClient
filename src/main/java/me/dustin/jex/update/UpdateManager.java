package me.dustin.jex.update;

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

    public void checkForUpdate() {
        try {

            URL url = new URL("https://jexclient.com/includes/version.php");
            String response = WebHelper.INSTANCE.readURL(url);

            JsonObject updateResponse = new Gson().fromJson(response, JsonObject.class);
            latestMCVersion = updateResponse.get("mcVersion").getAsString();
            latestVersion = updateResponse.get("version").getAsString();

            boolean isCurrentlySnapshot = SharedConstants.getGameVersion().getName().contains("w");
            boolean isLatestSnapshot = latestMCVersion.contains("w");
            if (!isCurrentlySnapshot && isLatestSnapshot) {
                url = new URL("https://jexclient.com/includes/get-versions.php?version=" + SharedConstants.getGameVersion().getName());
                response = WebHelper.INSTANCE.readURL(url);
                latestMCVersion = SharedConstants.getGameVersion().getName();
                if (response.contains(","))
                    latestVersion = response.split(",")[0];
                else
                    latestVersion = response;
            }
            boolean isVersionSame = JexClient.INSTANCE.getVersion().equalsIgnoreCase(latestVersion);
            boolean isMCVersionSame = SharedConstants.getGameVersion().getName().equalsIgnoreCase(latestMCVersion);
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
