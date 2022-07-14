package me.dustin.jex.helper.addon;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.render.EventShouldFlipUpsideDown;
import me.dustin.jex.helper.addon.cape.CapeHelper;
import me.dustin.jex.helper.addon.ears.EarsHelper;
import me.dustin.jex.helper.addon.hat.HatHelper;
import me.dustin.jex.event.filters.ServerPacketFilter;
import me.dustin.jex.event.misc.EventSetLevel;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.helper.addon.pegleg.PeglegHelper;
import me.dustin.jex.helper.addon.penis.PenisHelper;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.WebHelper;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public enum AddonHelper {
	INSTANCE;
	public final ArrayList<AddonResponse> responses = new ArrayList<>();
	public final ArrayList<String> requestedUUIds = new ArrayList<>();

	@EventPointer
	private final EventListener<EventPacketReceive> eventPacketReceiveEventListener = new EventListener<>(event -> {
		PlayerSpawnS2CPacket playerSpawnS2CPacket = (PlayerSpawnS2CPacket)event.getPacket();
		PlayerListEntry playerListEntry = Wrapper.INSTANCE.getLocalPlayer().networkHandler.getPlayerListEntry(playerSpawnS2CPacket.getPlayerUuid());
		if (playerListEntry != null && playerListEntry.getGameMode() != null)
			loadAddons(playerSpawnS2CPacket.getPlayerUuid().toString().replace("-", ""));
	}, new ServerPacketFilter(EventPacketReceive.Mode.PRE, PlayerSpawnS2CPacket.class));

	@EventPointer
	private final EventListener<EventSetLevel> eventSetLevelEventListener = new EventListener<>(event -> {
		if (Wrapper.INSTANCE.getLocalPlayer() != null)
			loadAddons(Wrapper.INSTANCE.getLocalPlayer());
	});

	@EventPointer
	private final EventListener<EventShouldFlipUpsideDown> eventShouldFlipUpsideDownEventListener = new EventListener<>(event -> {
		if (event.getLivingEntity() instanceof PlayerEntity playerEntity) {
			AddonResponse response = getResponse(playerEntity.getUuidAsString().replace("-", ""));
			if (response != null && response.upsideDown())
				event.setFlip(true);
		}
	});

	public void loadAddons(PlayerEntity player) {
		if (EntityHelper.INSTANCE.isNPC(player))
			return;
		String s = player.getGameProfile().getId().toString().replace("-", "");
		loadAddons(s);
	}

	private AddonResponse loadAddonsNoThread(String uuid) {
		if (!requestedUUIds.contains(uuid))
			requestedUUIds.add(uuid);
		try {
			String url = "%sinc/profile-info.inc.php?uuid=%s".formatted(JexClient.INSTANCE.getBaseUrl(), uuid);
			String response = WebHelper.INSTANCE.httpRequest(url, null, null, "GET").data();
			JsonObject json = new Gson().fromJson(response, JsonObject.class);
			if (json.has("error")) {
				JexClient.INSTANCE.getLogger().error("Addons: %s: %s".formatted(json.get("error").getAsString(), json.get("errorMessage").getAsString()));
				return null;
			}
			String cape = json.has("cape") ? json.get("cape").getAsString() : "none";
			String hat = json.has("hat") ? json.get("hat").getAsString() : "none";
			String pegleg = json.has("pegleg") ? json.get("pegleg").getAsString() : "none";
			String ears = json.has("ears") ? json.get("ears").getAsString() : "none";
			String penis = json.has("penis") ? json.get("penis").getAsString() : "none";
			boolean donator = json.has("donator") && json.get("donator").getAsBoolean();
			boolean upsideDown = json.has("upsidedown") && json.get("upsidedown").getAsBoolean();
			boolean enchantedcape = json.has("enchantedcape") && json.get("enchantedcape").getAsBoolean();
			boolean enchantedears = json.has("enchantedears") && json.get("enchantedears").getAsBoolean();
			boolean enchantedleg = json.has("enchantedleg") && json.get("enchantedleg").getAsBoolean();
			AddonResponse addonResponse = new AddonResponse(uuid, upsideDown, enchantedcape, enchantedears, enchantedleg, donator);
			responses.add(addonResponse);
			if (!hat.equals("none"))
				HatHelper.INSTANCE.setHat(uuid, hat);

			if (!pegleg.equalsIgnoreCase("none"))
				PeglegHelper.INSTANCE.setPegleg(uuid, pegleg);

			if (!ears.equalsIgnoreCase("none"))
				EarsHelper.INSTANCE.parseEars(ears, uuid);

			if (!penis.equalsIgnoreCase("none"))
				PenisHelper.INSTANCE.parsePenis(penis, uuid);

			if (!cape.equalsIgnoreCase("none"))
				CapeHelper.INSTANCE.parseCape(cape, uuid);
			else
				downloadMCCapes(uuid);
			return addonResponse;
		} catch (Exception e) {
			downloadMCCapes(uuid);
		}
		return null;
	}

	private void loadAddons(String uuid) {
		if (requestedUUIds.contains(uuid))
			return;
		requestedUUIds.add(uuid);
		Thread addonDownload = new Thread(() -> loadAddonsNoThread(uuid));
		addonDownload.setDaemon(true);
		addonDownload.start();
	}

	private void downloadMCCapes(String uuid) {
		try {
			String url = "https://minecraftcapes.net/profile/" + uuid;
			String response = WebHelper.INSTANCE.httpRequest(url, null, null, "GET").data();
			JsonObject json = new Gson().fromJson(response, JsonObject.class);
			JsonObject textures = json.getAsJsonObject("textures");

			String cape = textures.has("cape") ? textures.get("cape").getAsString() : "null";
			String ears = textures.has("ears") ? textures.get("ears").getAsString() : "null";
			if (cape != null && !cape.equals("null") && !cape.isEmpty()) {
				CapeHelper.INSTANCE.parseCape(cape, uuid);
			}
			if (ears != null && !ears.equals("null") && !ears.isEmpty() && !EarsHelper.INSTANCE.hasEars(uuid)) {
				EarsHelper.INSTANCE.parseEars(ears, uuid);
			}
		} catch (Exception e) {}
	}

	public boolean isLinkedToAccount(String uuid) {
		return getResponse(uuid) != null;
	}

	public boolean isDonator(String uuid) {
		if (!hasResquested(uuid))
			loadAddons(uuid);
		AddonResponse response = getResponse(uuid);
		return response != null && response.isDonator();
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

	public void getAddonResponse(String uuid, Consumer<AddonResponse> consumer) {
		if (!requestedUUIds.contains(uuid)) {
			new Thread(() -> {
				consumer.accept(loadAddonsNoThread(uuid));
			}).start();
		}
	}

	public void clearAddons() {
		responses.clear();
		requestedUUIds.clear();
		CapeHelper.INSTANCE.clear();
		HatHelper.INSTANCE.clear();
		EarsHelper.INSTANCE.clear();
		PenisHelper.INSTANCE.clear();
		PeglegHelper.INSTANCE.clear();
	}

	public record AddonResponse(String uuid, boolean upsideDown, boolean enchantedcape, boolean enchantedears, boolean enchantedleg, boolean isDonator) {}
}
