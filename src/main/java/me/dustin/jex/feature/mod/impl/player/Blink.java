package me.dustin.jex.feature.mod.impl.player;

import com.mojang.authlib.GameProfile;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.helper.entity.FakePlayerEntity;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.ArrayList;
import java.util.UUID;

@Feature.Manifest(category = Feature.Category.PLAYER, description = "Delay your movements to the server, making it seem like you teleported.")
public class Blink extends Feature {

	@Op(name = "Buffer Packets")
	public boolean bufferPackets = true;
	@OpChild(name = "Send Amount PT", min = 5, max = 50, parent = "Buffer Packets")
	public int amountPT = 25;

	private ArrayList<PlayerMoveC2SPacket> packets = new ArrayList<>();
	public static PlayerEntity playerEntity;
	private boolean stopCatching;

	@EventListener(events = { EventPacketSent.class })
	private void runMethod(EventPacketSent eventPacketSent) {
		if (eventPacketSent.getMode() != EventPacketSent.Mode.PRE)
			return;
		if (Wrapper.INSTANCE.getLocalPlayer() == null || (packets.isEmpty() && stopCatching)) {
			packets.clear();
			this.setState(false);
			return;
		}
		if (!stopCatching && eventPacketSent.getPacket() instanceof PlayerMoveC2SPacket) {
			if (PlayerHelper.INSTANCE.isMoving()) {
				packets.add((PlayerMoveC2SPacket) eventPacketSent.getPacket());
			}
			eventPacketSent.cancel();
		}
	}

	@EventListener(events = { EventPlayerPackets.class })
	private void runPlayerPackets(EventPlayerPackets eventPlayerPackets) {
		if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
			if (stopCatching && !packets.isEmpty()) {
				for (int i = 0; i < amountPT; i++) {
					NetworkHelper.INSTANCE.sendPacket(packets.get(i));
				}
			}
		}
	}

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
