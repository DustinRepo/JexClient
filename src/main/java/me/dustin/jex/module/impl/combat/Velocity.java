package me.dustin.jex.module.impl.combat;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.player.EventExplosionVelocity;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.load.impl.IEntityVelocityUpdateS2CPacket;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;

@ModClass(name = "Velocity", category = ModCategory.COMBAT, description = "Remove all knockback from the player.")
public class Velocity extends Module {

    @Op(name = "Percent", max = 300)
    public int percent = 0;

    @EventListener(events = {EventPacketReceive.class, EventExplosionVelocity.class})
    public void run(Event event) {
        float perc = percent / 100.0f;
        if (event instanceof EventPacketReceive) {
            EventPacketReceive packetReceive = (EventPacketReceive) event;
            if (packetReceive.getPacket() instanceof EntityVelocityUpdateS2CPacket) {
                try {
                    EntityVelocityUpdateS2CPacket entityVelocityUpdateS2CPacket = (EntityVelocityUpdateS2CPacket) ((EventPacketReceive) event).getPacket();
                    IEntityVelocityUpdateS2CPacket iEntityVelocityUpdateS2CPacket = (IEntityVelocityUpdateS2CPacket)entityVelocityUpdateS2CPacket;
                    if (Wrapper.INSTANCE.getWorld().getEntityById(entityVelocityUpdateS2CPacket.getId()) == Wrapper.INSTANCE.getLocalPlayer()) {
                        if (percent == 0)
                            event.cancel();
                        else {
                            iEntityVelocityUpdateS2CPacket.setVelocityX((int)(iEntityVelocityUpdateS2CPacket.getVelocityX() * perc));
                            iEntityVelocityUpdateS2CPacket.setVelocityY((int)(iEntityVelocityUpdateS2CPacket.getVelocityY() * perc));
                            iEntityVelocityUpdateS2CPacket.setVelocityZ((int)(iEntityVelocityUpdateS2CPacket.getVelocityZ() * perc));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (event instanceof EventExplosionVelocity) {
            if (percent == 0)
                event.cancel();
            else {
                EventExplosionVelocity eventExplosionVelocity = (EventExplosionVelocity)event;
                eventExplosionVelocity.setMultX(perc);
                eventExplosionVelocity.setMultY(perc);
                eventExplosionVelocity.setMultZ(perc);
            }
        }
    }


}
