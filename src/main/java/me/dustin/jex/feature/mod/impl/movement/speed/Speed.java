package me.dustin.jex.feature.mod.impl.movement.speed;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.speed.impl.NCPSpeed;
import me.dustin.jex.feature.mod.impl.movement.speed.impl.VanillaSpeed;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;

@Feature.Manifest(name = "Speed", category = Feature.Category.MOVEMENT, description = "Sanic gotta go fast.")
public class Speed extends Feature {

    @Op(name = "Mode", all = {"Vanilla", "Floaty"})
    public String mode = "Vanilla";

    @OpChild(name = "Vanilla Speed", min = 0.3f, max = 3, inc = 0.01f, parent = "Mode", dependency = "Vanilla")
    public float vanillaSpeed = 0.6f;

    private String lastMode;

    public Speed() {
        new NCPSpeed();
        new VanillaSpeed();
    }

    @EventListener(events = {EventMove.class, EventPlayerPackets.class, EventPacketSent.class})
    public void run(Event event) {
        if (!mode.equalsIgnoreCase(lastMode) && lastMode != null) {
            FeatureExtension.get(lastMode, this).disable();
            FeatureExtension.get(mode, this).enable();
        }
        FeatureExtension.get(mode, this).pass(event);
        this.setSuffix(mode);
        lastMode = mode;
    }

    @Override
    public void onEnable() {
        FeatureExtension.get(mode, this).enable();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        FeatureExtension.get(mode, this).disable();
        super.onDisable();
    }
}
