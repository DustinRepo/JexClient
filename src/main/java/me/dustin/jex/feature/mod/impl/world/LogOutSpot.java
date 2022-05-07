package me.dustin.jex.feature.mod.impl.world;

import java.util.ArrayList;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ServerPacketFilter;
import me.dustin.jex.event.misc.EventSetLevel;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.core.Feature.Category;
import me.dustin.jex.feature.mod.core.Feature.Manifest;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.entity.FakePlayerEntity;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket.PlayerUpdate;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;

@Manifest(category = Category.WORLD, description = "Shows you where players have logged out at")
public class LogOutSpot extends Feature {
	
	private final ArrayList<PlayerData> logOutList = new ArrayList<>();
	private final ArrayList<FakePlayerEntity> fakePlayers = new ArrayList<>();

	@EventPointer
	private final EventListener<EventSetLevel> eventJoinWorldEventListener = new EventListener<>(event -> {
		logOutList.clear();
	});

	@EventPointer
	private final EventListener<EventPacketReceive> eventPacketReceiveEventListener = new EventListener<>(event -> {
		ClientboundPlayerInfoPacket playerLists2CPacket = (ClientboundPlayerInfoPacket) event.getPacket();
		if (Wrapper.INSTANCE.getWorld() == null || Wrapper.INSTANCE.getLocalPlayer() == null)
			return;
		if (playerLists2CPacket.getAction() == ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER) {
			for (PlayerUpdate entry : playerLists2CPacket.getEntries()) {
				int id = 69420 + ClientMathHelper.INSTANCE.getRandom(200);
				UUID uuid = entry.getProfile().getId();
				Player player = Wrapper.INSTANCE.getWorld().getPlayerByUUID(uuid);
				if (player == null || player.getName().getString().isEmpty() || EntityHelper.INSTANCE.isNPC(player))
					return;
				PlayerData playerInfo = new PlayerData(player.getName().getString(), uuid, id);
				String name = "LOG: \247b" + player.getName().getString();
				logOutList.add(playerInfo);
				ChatHelper.INSTANCE.addClientMessage(player.getName().getString() + " logged out");
				FakePlayerEntity fakePlayer = new FakePlayerEntity(Wrapper.INSTANCE.getWorld(), new GameProfile(UUID.randomUUID(), name));
				fakePlayer.restoreFrom(player);
				fakePlayer.copyPosition(player);
				Wrapper.INSTANCE.getWorld().putNonPlayerEntity(id, fakePlayer);
				fakePlayers.add(fakePlayer);
			}
		} else if (playerLists2CPacket.getAction() == ClientboundPlayerInfoPacket.Action.ADD_PLAYER) {
			for (PlayerUpdate entry : playerLists2CPacket.getEntries()) {
				for (int i = 0; i < logOutList.size(); i++) {
					PlayerData data = logOutList.get(i);
					if (data.uuid().compareTo(entry.getProfile().getId()) == 0) {
						for (int j = 0; j < fakePlayers.size(); j++) {
							FakePlayerEntity fakePlayer = fakePlayers.get(j);
							if (fakePlayer.getName().getString().contains(data.name())) {
								fakePlayer.setPosRaw(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
								Wrapper.INSTANCE.getWorld().removeEntity(fakePlayer.getId(), RemovalReason.DISCARDED);
								fakePlayers.remove(j);
							}
						}
						logOutList.remove(i);
					}
				}
			}
		}
	}, new ServerPacketFilter(EventPacketReceive.Mode.PRE, ClientboundPlayerInfoPacket.class));

	@Override
	public void onDisable() {
		fakePlayers.forEach(fakePlayer -> {
			fakePlayer.setPosRaw(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
			Wrapper.INSTANCE.getWorld().removeEntity(fakePlayer.getId(), RemovalReason.DISCARDED);
		});
		fakePlayers.clear();
		logOutList.clear();
		super.onDisable();
	}
	
	public record PlayerData (String name, UUID uuid, int entityID) {}
}
