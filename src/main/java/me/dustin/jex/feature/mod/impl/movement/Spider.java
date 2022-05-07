package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.player.PlayerHelper;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;
import me.dustin.jex.feature.option.annotate.Op;

@Feature.Manifest(category = Feature.Category.MOVEMENT, description = "Climb up walls like a spider.")
public class Spider extends Feature {

	@Op(name = "Mode", all = { "Vanilla", "NCP" })
	public String mode = "Vanilla";

	@EventPointer
	private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
		if (Wrapper.INSTANCE.getLocalPlayer().horizontalCollision) {
			Vec3 orig = Wrapper.INSTANCE.getLocalPlayer().getDeltaMovement();
			if (mode.equalsIgnoreCase("Vanilla")) {
				Wrapper.INSTANCE.getLocalPlayer().setDeltaMovement(orig.x(), 0.3, orig.z());
			} else {
				Wrapper.INSTANCE.getLocalPlayer().setDeltaMovement(orig.x(), 0, orig.z());
				NetworkHelper.INSTANCE.sendPacket(new ServerboundMovePlayerPacket.PosRot(Wrapper.INSTANCE.getLocalPlayer().getX() + orig.x() * 2, Wrapper.INSTANCE.getLocalPlayer().getY() + (Wrapper.INSTANCE.getOptions().keyShift.isDown() ? 0 : 0.0624), Wrapper.INSTANCE.getLocalPlayer().getZ() + orig.z() * 2, PlayerHelper.INSTANCE.getYaw(), PlayerHelper.INSTANCE.getPitch(), false));
				NetworkHelper.INSTANCE.sendPacket(new ServerboundMovePlayerPacket.PosRot(Wrapper.INSTANCE.getLocalPlayer().getX() + orig.x(), -1337 + Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ() + orig.z(), PlayerHelper.INSTANCE.getYaw(), PlayerHelper.INSTANCE.getPitch(), true));
			}
		}
	}, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
