package me.dustin.jex.feature.mod.impl.world;

import java.util.ArrayList;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ServerPacketFilter;
import me.dustin.jex.event.misc.EventJoinWorld;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.core.Feature.Category;
import me.dustin.jex.feature.mod.core.Feature.Manifest;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.entity.FakePlayerEntity;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Entry;

@Manifest(category = Category.WORLD, description = "Shows you where players have logged out at")
public class LogOutSpot extends Feature {
	
	private final ArrayList<PlayerData> logOutList = new ArrayList<>();
	private final ArrayList<FakePlayerEntity> fakePlayers = new ArrayList<>();

	@EventPointer
	private final EventListener<EventJoinWorld> eventJoinWorldEventListener = new EventListener<>(event -> {
		logOutList.clear();
	});

	@EventPointer
	private final EventListener<EventPacketReceive> eventPacketReceiveEventListener = new EventListener<>(event -> {
		PlayerListS2CPacket playerLists2CPacket = (PlayerListS2CPacket) event.getPacket();
		if (Wrapper.INSTANCE.getWorld() == null || Wrapper.INSTANCE.getLocalPlayer() == null)
			return;
		if (playerLists2CPacket.getAction() == PlayerListS2CPacket.Action.REMOVE_PLAYER) {
			for (Entry entry : playerLists2CPacket.getEntries()) {
				int id = 69420 + ClientMathHelper.INSTANCE.getRandom(200);
				UUID uuid = entry.getProfile().getId();
				PlayerEntity player = Wrapper.INSTANCE.getWorld().getPlayerByUuid(uuid);
				if (player == null || player.getName().getString().isEmpty() || EntityHelper.INSTANCE.isNPC(player))
					return;
				PlayerData playerInfo = new PlayerData(player.getName().asString(), uuid, id);
				String name = "LOG: \247b" + player.getName().asString();
				logOutList.add(playerInfo);
				ChatHelper.INSTANCE.addClientMessage(player.getName().asString() + " logged out");
				FakePlayerEntity fakePlayer = new FakePlayerEntity(Wrapper.INSTANCE.getWorld(), new GameProfile(UUID.randomUUID(), name));
				fakePlayer.copyFrom(player);
				fakePlayer.copyPositionAndRotation(player);
				Wrapper.INSTANCE.getWorld().addEntity(id, fakePlayer);
				fakePlayers.add(fakePlayer);
			}
		} else if (playerLists2CPacket.getAction() == PlayerListS2CPacket.Action.ADD_PLAYER) {
			for (Entry entry : playerLists2CPacket.getEntries()) {
				for (int i = 0; i < logOutList.size(); i++) {
					PlayerData data = logOutList.get(i);
					if (data.uuid().compareTo(entry.getProfile().getId()) == 0) {
						for (int j = 0; j < fakePlayers.size(); j++) {
							FakePlayerEntity fakePlayer = fakePlayers.get(j);
							if (fakePlayer.getName().asString().contains(data.name())) {
								fakePlayer.setPos(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
								Wrapper.INSTANCE.getWorld().removeEntity(fakePlayer.getId(), RemovalReason.DISCARDED);
								fakePlayers.remove(j);
							}
						}
						logOutList.remove(i);
					}
				}
			}
		}
	}, new ServerPacketFilter(EventPacketReceive.Mode.PRE, PlayerListS2CPacket.class));

	@Override
	public void onDisable() {
		fakePlayers.forEach(fakePlayer -> {
			fakePlayer.setPos(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
			Wrapper.INSTANCE.getWorld().removeEntity(fakePlayer.getId(), RemovalReason.DISCARDED);
		});
		fakePlayers.clear();
		logOutList.clear();
		super.onDisable();
	}
	
	public record PlayerData (String name, UUID uuid, int entityID) {}
}
