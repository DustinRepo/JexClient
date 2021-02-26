package me.dustin.jex.module.impl.misc;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.minecraft.client.render.entity.PlayerModelPart;

import java.util.ArrayList;
import java.util.Random;

@ModClass(name = "SkinBlink", category = ModCategory.MISC, description = "Make your skin flash your layers on and off")
public class SkinBlink extends Module {

    @Op(name = "Mode", all = {"Random", "Full Flash", "Custom"})
    public String mode = "Random";
    @OpChild(name = "Head", parent = "Mode", dependency = "Custom")
    public boolean head = true;
    @OpChild(name = "Jacket", parent = "Mode", dependency = "Custom")
    public boolean jacket = true;
    @OpChild(name = "Cape", parent = "Mode", dependency = "Custom")
    public boolean cape = true;
    @OpChild(name = "Left Arm", parent = "Mode", dependency = "Custom")
    public boolean leftArm = true;
    @OpChild(name = "Left Leg", parent = "Mode", dependency = "Custom")
    public boolean leftLeg = true;
    @OpChild(name = "Right Arm", parent = "Mode", dependency = "Custom")
    public boolean rightArm = true;
    @OpChild(name = "Right Leg", parent = "Mode", dependency = "Custom")
    public boolean rightLeg = true;

    @Op(name = "Delay (MS)", min = 50, max = 5000)
    public int delay = 250;

    Random random = new Random();

    private ArrayList<PlayerModelPart> savedEnabled = new ArrayList<>();
    private Timer timer = new Timer();

    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            if (!timer.hasPassed(delay))
                return;
            switch (mode) {
                case "Random":
                    for (PlayerModelPart value : PlayerModelPart.values()) {
                        Wrapper.INSTANCE.getOptions().setPlayerModelPart(value, random.nextBoolean());
                    }
                    break;
                case "Full Flash":
                    boolean on = false;
                    if (Wrapper.INSTANCE.getOptions().getEnabledPlayerModelParts().isEmpty())
                        on = true;
                    for (PlayerModelPart value : PlayerModelPart.values()) {
                        Wrapper.INSTANCE.getOptions().setPlayerModelPart(value, on);
                    }
                    break;
                case "Custom":
                    if (head)
                        Wrapper.INSTANCE.getOptions().togglePlayerModelPart(PlayerModelPart.HAT);
                    if (cape)
                        Wrapper.INSTANCE.getOptions().togglePlayerModelPart(PlayerModelPart.CAPE);
                    if (jacket)
                        Wrapper.INSTANCE.getOptions().togglePlayerModelPart(PlayerModelPart.JACKET);
                    if (leftArm)
                        Wrapper.INSTANCE.getOptions().togglePlayerModelPart(PlayerModelPart.LEFT_SLEEVE);
                    if (leftLeg)
                        Wrapper.INSTANCE.getOptions().togglePlayerModelPart(PlayerModelPart.LEFT_PANTS_LEG);
                    if (rightArm)
                        Wrapper.INSTANCE.getOptions().togglePlayerModelPart(PlayerModelPart.RIGHT_SLEEVE);
                    if (rightLeg)
                        Wrapper.INSTANCE.getOptions().togglePlayerModelPart(PlayerModelPart.RIGHT_PANTS_LEG);
                    break;
            }
            timer.reset();
            Wrapper.INSTANCE.getOptions().onPlayerModelPartChange();
        }
    }

    @Override
    public void onEnable() {
        if (Wrapper.INSTANCE.getOptions() != null) {
            savedEnabled.clear();
            savedEnabled.addAll(Wrapper.INSTANCE.getOptions().getEnabledPlayerModelParts());
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (Wrapper.INSTANCE.getLocalPlayer() != null) {
            for (PlayerModelPart value : PlayerModelPart.values()) {
                Wrapper.INSTANCE.getOptions().setPlayerModelPart(value, savedEnabled.contains(value));
            }
        }
        super.onDisable();
    }
}
