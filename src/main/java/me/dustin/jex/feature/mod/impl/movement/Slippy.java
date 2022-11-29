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

public class Slippy extends Feature {
 
 public Slippy() {
   super(Category.MOVEMENT, "Сhanges the block to ice for fast movement");
    }
 
private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
		if (Wrapper.INSTANCE.getLocalPlayer().horizontalCollision) {
			Vec3d orig = Wrapper.INSTANCE.getLocalPlayer().getVelocity();
    Wrapper.INSTANCE.getLocalPlayer().setVelocity(orig.getX(), 0, orig.getZ());
				NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.Full(Wrapper.INSTANCE.getLocalPlayer().getX() + orig.getX() * 2, Wrapper.INSTANCE.getLocalPlayer().getZ() + orig.getZ() * 2, PlayerHelper.INSTANCE.getYaw(), PlayerHelper.INSTANCE.getPitch(), false));
				NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.Full(Wrapper.INSTANCE.getLocalPlayer().getX() + orig.getX(), Wrapper.INSTANCE.getLocalPlayer().getZ() + orig.getZ(), PlayerHelper.INSTANCE.getYaw(), PlayerHelper.INSTANCE.getPitch(), true));
			}
		}
	}, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
