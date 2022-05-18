package me.dustin.jex.helper.math;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ServerPacketFilter;
import me.dustin.jex.event.misc.EventSetLevel;
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

    @EventPointer
    private final EventListener<EventSetLevel> eventJoinWorldEventListener = new EventListener<>(event -> {
        reports.clear();
    });

    @EventPointer
    private final EventListener<EventPacketReceive> eventPacketReceiveEventListener = new EventListener<>(event -> {
        reports.add(System.currentTimeMillis());
        while (reports.size() > Feature.get(TPSSync.class).sampleSize) {
            reports.remove(0);
        }
    }, new ServerPacketFilter(EventPacketReceive.Mode.PRE, WorldTimeUpdateS2CPacket.class));
}
