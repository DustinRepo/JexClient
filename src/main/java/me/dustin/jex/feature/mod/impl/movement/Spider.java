package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.player.PlayerHelper;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import me.dustin.jex.feature.option.annotate.Op;

@Feature.Manifest(category = Feature.Category.MOVEMENT, description = "Climb up walls like a spider.")
public class Spider extends Feature {

	@Op(name = "Mode", all = { "Vanilla", "NCP" })
	public String mode = "Vanilla";

	@EventPointer
	private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
		if (Wrapper.INSTANCE.getLocalPlayer().horizontalCollision) {
			Vec3d orig = Wrapper.INSTANCE.getLocalPlayer().getVelocity();
			if (mode.equalsIgnoreCase("Vanilla")) {
				Wrapper.INSTANCE.getLocalPlayer().setVelocity(orig.getX(), 0.3, orig.getZ());
			} else {
				Wrapper.INSTANCE.getLocalPlayer().setVelocity(orig.getX(), 0, orig.getZ());
				NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.Full(Wrapper.INSTANCE.getLocalPlayer().getX() + orig.getX() * 2, Wrapper.INSTANCE.getLocalPlayer().getY() + (Wrapper.INSTANCE.getOptions().sneakKey.isPressed() ? 0 : 0.0624), Wrapper.INSTANCE.getLocalPlayer().getZ() + orig.getZ() * 2, PlayerHelper.INSTANCE.getYaw(), PlayerHelper.INSTANCE.getPitch(), false));
				NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.Full(Wrapper.INSTANCE.getLocalPlayer().getX() + orig.getX(), -1337 + Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ() + orig.getZ(), PlayerHelper.INSTANCE.getYaw(), PlayerHelper.INSTANCE.getPitch(), true));
			}
		}
	}, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
