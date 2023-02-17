package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventCurrentItemAttackStrengthDelay;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.math.TPSHelper;
import me.dustin.jex.feature.mod.core.Feature;

public class TPSSync extends Feature {

    public TPSSync() {
        super(Category.COMBAT, "Sync attacks to server TPS to deal more damage on laggy servers.");
    }

    @EventPointer
    private final EventListener<EventCurrentItemAttackStrengthDelay> eventAttackCooldownPerTickEventListener = new EventListener<>(event -> {
        double tps = TPSHelper.INSTANCE.getAverageTPS();
        double value = 20 / tps;
        this.setSuffix(String.format("%.2f", tps));
        event.setValue(value);
    });
}
