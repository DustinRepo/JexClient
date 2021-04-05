package me.dustin.jex.module.impl.movement;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

@ModClass(name = "Spider", category = ModCategory.MOVEMENT, description = "Climb up walls like a spider.")
public class Spider extends Module {

    @Op(name = "Mode", all = {"Vanilla", "NCP"})
    public String mode = "Vanilla";

    @EventListener(events = {EventPlayerPackets.class})
    public void run(EventPlayerPackets event) {
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            if (Wrapper.INSTANCE.getLocalPlayer().horizontalCollision) {
                Vec3d orig = Wrapper.INSTANCE.getLocalPlayer().getVelocity();
                if (mode.equalsIgnoreCase("Vanilla")) {
                    Wrapper.INSTANCE.getLocalPlayer().setVelocity(orig.getX(), 0.3, orig.getZ());
                } else {
                    Wrapper.INSTANCE.getLocalPlayer().setVelocity(orig.getX(), 0, orig.getZ());
                    NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.Full(Wrapper.INSTANCE.getLocalPlayer().getX() + orig.getX() * 2, Wrapper.INSTANCE.getLocalPlayer().getY() + (Wrapper.INSTANCE.getOptions().keySneak.isPressed() ? 0 : 0.0624), Wrapper.INSTANCE.getLocalPlayer().getZ() + orig.getZ() * 2, Wrapper.INSTANCE.getLocalPlayer().yaw, Wrapper.INSTANCE.getLocalPlayer().pitch, false));
                    NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.Full(Wrapper.INSTANCE.getLocalPlayer().getX() + orig.getX(), -1337 + Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ() + orig.getZ(), Wrapper.INSTANCE.getLocalPlayer().yaw, Wrapper.INSTANCE.getLocalPlayer().pitch, true));
                }
            }
        }
    }
}
