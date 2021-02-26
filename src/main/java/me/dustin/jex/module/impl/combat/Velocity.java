package me.dustin.jex.module.impl.combat;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.player.EventExplosionVelocity;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;

@ModClass(name = "Velocity", category = ModCategory.COMBAT, description = "Remove all knockback from the player.")
public class Velocity extends Module {

    @EventListener(events = {EventPacketReceive.class, EventExplosionVelocity.class})
    public void run(Event event) {
        if (event instanceof EventPacketReceive) {
            EventPacketReceive packetReceive = (EventPacketReceive) event;
            if (packetReceive.getPacket() instanceof EntityVelocityUpdateS2CPacket) {
                try {
                    EntityVelocityUpdateS2CPacket entityVelocityUpdateS2CPacket = (EntityVelocityUpdateS2CPacket) ((EventPacketReceive) event).getPacket();
                    if (Wrapper.INSTANCE.getWorld().getEntityById(entityVelocityUpdateS2CPacket.getId()) == Wrapper.INSTANCE.getLocalPlayer())
                        event.setCancelled(true);
                } catch (Exception e) {

                }
            }
        }
        if (event instanceof EventExplosionVelocity) {
            event.cancel();
        }
    }


}
