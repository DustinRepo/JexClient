package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventAttackCooldownPerTick;
import me.dustin.jex.helper.math.TPSHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;

@Feature.Manifest(name = "TPSSync", category = Feature.Category.COMBAT, description = "Sync attacks to server TPS to deal more damage on laggy servers.")
public class TPSSync extends Feature {

    @Op(name = "Sample Size", min = 2, max = 100)
    public int sampleSize = 15;

    @EventListener(events = {EventAttackCooldownPerTick.class})
    public void runEvent(EventAttackCooldownPerTick event) {
        double tps = TPSHelper.INSTANCE.getAverageTPS();
        double value = 20 / tps;
        this.setSuffix(String.format("%.2f", tps));
        event.setValue(value);
    }
}
