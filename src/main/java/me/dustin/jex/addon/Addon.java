package me.dustin.jex.addon;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.dustin.jex.JexClient;
import me.dustin.jex.addon.cape.Cape;
import me.dustin.jex.addon.hat.Hat;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.network.WebHelper;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class Addon {

	public static ArrayList<AddonResponse> responses = new ArrayList<>();

	public static ArrayList<String> requestedUUIds = new ArrayList<>();

	public static void loadAddons(AbstractClientPlayerEntity player) {
		if (EntityHelper.INSTANCE.isNPC(player))
			return;
		String s = player.getGameProfile().getId().toString().replace("-", "");
		loadAddons(s);
	}

	public static void loadAddons(PlayerEntity player) {
		if (EntityHelper.INSTANCE.isNPC(player))
			return;
		String s = player.getGameProfile().getId().toString().replace("-", "");
		loadAddons(s);
	}

	public static void loadAddons(String uuid) {
		if (requestedUUIds.contains(uuid))
			return;
		requestedUUIds.add(uuid);
		Thread addonDownload = new Thread(() -> {
			try {
				String url = JexClient.INSTANCE.getBaseUrl() + "includes/loadprofile.inc.php?uuid=" + uuid;
				String response = WebHelper.INSTANCE.httpRequest(url, null, null, "GET").data();
				JsonObject json = new Gson().fromJson(response, JsonObject.class);
				String cape = json.get("cape").getAsString();
				String hat = json.get("hat").getAsString();
				boolean linkedToAccount = json.get("linkedToAccount").getAsBoolean();
				AddonResponse addonResponse = new AddonResponse(uuid, cape, hat, linkedToAccount);
				responses.add(addonResponse);
				if (linkedToAccount) {
					if (cape != null && !cape.equals("null") && !cape.isEmpty()) {
						Cape.parseCape(cape, uuid);
					}

					if (hat != null && !hat.equals("null") && !hat.equals("none")) {
						Hat.setHat(uuid, hat);
					}
				}
			} catch (Exception e) {}
		});
		addonDownload.setDaemon(true);
		addonDownload.start();
	}

	public static boolean isLinkedToAccount(String uuid) {
		AddonResponse response = getResponse(uuid);
		if (response != null) {
			return response.linkedToAccount();
		}
		return false;
	}

	public static boolean isDonator(String uuid) {
		if (!hasResquested(uuid))
			loadAddons(uuid);
		return Hat.hasHat(uuid) || Cape.hasCape(uuid);
	}

	private static boolean hasResquested(String uuid) {
		return requestedUUIds.contains(uuid);
	}

	public static AddonResponse getResponse(String uuid) {
		if (!requestedUUIds.contains(uuid)) {
			loadAddons(uuid);
			return null;
		}
		try {
			for (AddonResponse response : responses) {
				if (response != null && response.uuid() != null && response.uuid().equalsIgnoreCase(uuid))
					return response;
			}
		} catch (ConcurrentModificationException e) {}
		return null;
	}

	public static void clearAddons() {
		responses.clear();
		requestedUUIds.clear();
		Cape.clear();
		Hat.hatPlayers.clear();
	}

	public record AddonResponse(String uuid, String cape, String hat, boolean linkedToAccount) {}
}
