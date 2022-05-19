package me.dustin.jex.feature.mod.impl.player;

import com.mojang.authlib.GameProfile;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.helper.entity.FakePlayerEntity;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import java.util.ArrayList;
import java.util.UUID;

public class Blink extends Feature {

	@Op(name = "Buffer Packets")
	public boolean bufferPackets = true;
	@OpChild(name = "Send Amount PT", min = 5, max = 50, parent = "Buffer Packets")
	public int amountPT = 25;

	private final ArrayList<PlayerMoveC2SPacket> packets = new ArrayList<>();
	public static PlayerEntity playerEntity;
	private boolean stopCatching;

	public Blink() {
		super(Category.PLAYER, "Delay your movements to the server, making it seem like you teleported.");
	}

	@EventPointer
	private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
		if (Wrapper.INSTANCE.getLocalPlayer() == null || (packets.isEmpty() && stopCatching)) {
			packets.clear();
			this.setState(false);
			return;
		}
		if (!stopCatching) {
			if (PlayerHelper.INSTANCE.isMoving()) {
				packets.add((PlayerMoveC2SPacket) event.getPacket());
			}
			event.cancel();
		}
	}, new ClientPacketFilter(EventPacketSent.Mode.PRE, PlayerMoveC2SPacket.class));

	@EventPointer
	private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
		if (stopCatching && !packets.isEmpty()) {
			for (int i = 0; i < amountPT; i++) {
				NetworkHelper.INSTANCE.sendPacket(packets.get(i));
			}
		}
	}, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

	@Override
	public void onEnable() {
		stopCatching = false;
		if (Wrapper.INSTANCE.getLocalPlayer() != null) {
			playerEntity = new FakePlayerEntity(Wrapper.INSTANCE.getWorld(), new GameProfile(UUID.randomUUID(), Wrapper.INSTANCE.getMinecraft().getSession().getUsername()));
			playerEntity.copyFrom(Wrapper.INSTANCE.getLocalPlayer());
			playerEntity.copyPositionAndRotation(Wrapper.INSTANCE.getLocalPlayer());
			Wrapper.INSTANCE.getWorld().addEntity(42069, playerEntity);
		}
		super.onEnable();
	}

	@Override
	public void onDisable() {
		stopCatching = true;
		if (!bufferPackets || packets.isEmpty())
			super.onDisable();
		if (!bufferPackets)
			packets.forEach(NetworkHelper.INSTANCE::sendPacket);
		packets.clear();
		if (playerEntity != null) {
			playerEntity.setPos(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
			if (Wrapper.INSTANCE.getWorld() != null)
				Wrapper.INSTANCE.getWorld().removeEntity(playerEntity.getId(), Entity.RemovalReason.DISCARDED);
			playerEntity = null;
		}
	}
}
