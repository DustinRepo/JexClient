package me.dustin.jex.helper.addon;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.JexClient;
import me.dustin.jex.helper.addon.cape.CapeHelper;
import me.dustin.jex.helper.addon.hat.HatHelper;
import me.dustin.jex.event.filters.ServerPacketFilter;
import me.dustin.jex.event.misc.EventSetLevel;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.WebHelper;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public enum AddonHelper {
	INSTANCE;
	public static final ArrayList<AddonResponse> responses = new ArrayList<>();
	public static final ArrayList<String> requestedUUIds = new ArrayList<>();

	@EventPointer
	private final EventListener<EventPacketReceive> eventPacketReceiveEventListener = new EventListener<>(event -> {
		PlayerSpawnS2CPacket playerSpawnS2CPacket = (PlayerSpawnS2CPacket)event.getPacket();
		PlayerListEntry playerListEntry = Wrapper.INSTANCE.getLocalPlayer().networkHandler.getPlayerListEntry(playerSpawnS2CPacket.getPlayerUuid());
		if (playerListEntry != null && playerListEntry.getGameMode() != null)
			loadAddons(playerSpawnS2CPacket.getPlayerUuid().toString().replace("-", ""));
	}, new ServerPacketFilter(EventPacketReceive.Mode.PRE, PlayerSpawnS2CPacket.class));

	@EventPointer
	private final EventListener<EventSetLevel> eventSetLevelEventListener = new EventListener<>(event -> {
		loadAddons(Wrapper.INSTANCE.getLocalPlayer());
	});

	public void loadAddons(PlayerEntity player) {
		if (EntityHelper.INSTANCE.isNPC(player))
			return;
		String s = player.getGameProfile().getId().toString().replace("-", "");
		loadAddons(s);
	}

	private void loadAddons(String uuid) {
		if (requestedUUIds.contains(uuid))
			return;
		requestedUUIds.add(uuid);
		Thread addonDownload = new Thread(() -> {
			try {
				String url = "%sincludes/loadprofile.inc.php?uuid=%s".formatted(JexClient.INSTANCE.getBaseUrl(), uuid);
				String response = WebHelper.INSTANCE.httpRequest(url, null, null, "GET").data();
				JsonObject json = new Gson().fromJson(response, JsonObject.class);
				String cape = json.get("cape").getAsString();
				String hat = json.get("hat").getAsString();
				boolean linkedToAccount = json.get("linkedToAccount").getAsBoolean();
				AddonResponse addonResponse = new AddonResponse(uuid, cape, hat, linkedToAccount);
				responses.add(addonResponse);
				if (cape != null && !cape.equals("null") && !cape.isEmpty())
					CapeHelper.INSTANCE.parseCape(cape, uuid);
				else
					downloadMCCapes(uuid);

				if (hat != null && !hat.equals("null") && !hat.equals("none"))
					HatHelper.INSTANCE.setHat(uuid, hat);
			} catch (Exception e) {
				downloadMCCapes(uuid);
			}
		});
		addonDownload.setDaemon(true);
		addonDownload.start();
	}

	private void downloadMCCapes(String uuid) {
		try {
			String url = "https://minecraftcapes.net/profile/" + uuid;
			String response = WebHelper.INSTANCE.httpRequest(url, null, null, "GET").data();
			JsonObject json = new Gson().fromJson(response, JsonObject.class);
			JsonObject textures = json.getAsJsonObject("textures");

			String cape = textures.get("cape").getAsString();
			if (cape != null && !cape.equals("null") && !cape.isEmpty()) {
				CapeHelper.INSTANCE.parseCape(cape, uuid);
			}
		} catch (Exception e) {}
	}

	public boolean isLinkedToAccount(String uuid) {
		AddonResponse response = getResponse(uuid);
		if (response != null) {
			return response.linkedToAccount();
		}
		return false;
	}

	public boolean isDonator(String uuid) {
		if (!hasResquested(uuid))
			loadAddons(uuid);
		return HatHelper.INSTANCE.hasHat(uuid) && CapeHelper.INSTANCE.hasCape(uuid);
	}

	private boolean hasResquested(String uuid) {
		return requestedUUIds.contains(uuid);
	}

	public AddonResponse getResponse(String uuid) {
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

	public void clearAddons() {
		responses.clear();
		requestedUUIds.clear();
		CapeHelper.INSTANCE.clear();
		HatHelper.hatPlayers.clear();
	}

	public record AddonResponse(String uuid, String cape, String hat, boolean linkedToAccount) {}
}
