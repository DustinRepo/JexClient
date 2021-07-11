package me.dustin.jex.helper.math;
/*
 * @Author Dustin
 * 9/28/2019
 */

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventJoinWorld;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.combat.TPSSync;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

import java.util.ArrayList;
import java.util.List;

public enum TPSHelper {

    INSTANCE;
    private List<Long> reports = new ArrayList<>();

    public double getTPS(int averageOfSeconds) {
        if (reports.size() < 2) {
            return 20.0; // we can't compare yet
        }

        long currentTimeMS = reports.get(reports.size() - 1);
        long previousTimeMS = reports.get(reports.size() - averageOfSeconds);

        // on average, how long did it take for 20 ticks to execute? (ideal value: 1 second)
        double longTickTime = Math.max((currentTimeMS - previousTimeMS) / (1000.0 * (averageOfSeconds - 1)), 1.0);
        return 20 / longTickTime;
    }

    public double getAverageTPS() {
       return getTPS(reports.size());
    }

    @EventListener(events = {EventJoinWorld.class, EventPacketReceive.class})
    private void run(Event event) {
        if (event instanceof EventJoinWorld) {
            reports.clear();
        }
        if (event instanceof EventPacketReceive) {
            EventPacketReceive packetReceive = (EventPacketReceive) event;
            if (packetReceive.getPacket() instanceof WorldTimeUpdateS2CPacket) {
                reports.add(System.currentTimeMillis());
                while (reports.size() > ((TPSSync) Feature.get(TPSSync.class)).sampleSize) {
                    reports.remove(0);
                }
            }
        }
    }
}
