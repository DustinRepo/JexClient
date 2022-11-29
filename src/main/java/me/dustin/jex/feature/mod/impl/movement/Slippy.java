package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.player.PlayerHelper;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import me.dustin.jex.event.player.EventPlayerVelocity;

public class Slippy extends Feature {

	public Slippy() {
		super(Category.MOVEMENT, "Climb up walls like a spider.");
	}

	@EventPointer
	private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
		if (Wrapper.INSTANCE.getLocalPlayer().isOnGround().horizontalCollision) {
			Vec3d orig = Wrapper.INSTANCE.getLocalPlayer().getVelocity();
				Wrapper.INSTANCE.getLocalPlayer().setVelocity(0.98F, orig.getY() ,0.98F);	
		}
	}, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
