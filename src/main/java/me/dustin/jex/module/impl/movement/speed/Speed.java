package me.dustin.jex.module.impl.movement.speed;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.extension.ModuleExtension;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.module.impl.movement.speed.impl.NCPSpeed;
import me.dustin.jex.module.impl.movement.speed.impl.VanillaSpeed;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;

@ModClass(name = "Speed", category = ModCategory.MOVEMENT, description = "Sanic gotta go fast.")
public class Speed extends Module {

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
            ModuleExtension.get(lastMode, this).disable();
            ModuleExtension.get(mode, this).enable();
        }
        ModuleExtension.get(mode, this).pass(event);
        this.setSuffix(mode);
        lastMode = mode;
    }

    @Override
    public void onEnable() {
        ModuleExtension.get(mode, this).enable();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        ModuleExtension.get(mode, this).disable();
        super.onDisable();
    }
}
