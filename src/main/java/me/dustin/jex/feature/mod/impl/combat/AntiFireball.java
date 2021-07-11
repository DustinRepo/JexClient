package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.math.RotationVector;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.Hand;

@Feature.Manifest(name = "AntiFireball", category = Feature.Category.COMBAT, description = "Knock away fireballs from ghasts")
public class AntiFireball extends Feature {

    @Op(name = "Range", min = 1, max = 6, inc = 0.1f)
    public float range = 5;

    @Op(name = "Rotate")
    public boolean rotate = true;
    @Op(name = "Swing")
    public boolean swing = true;

    @EventListener(events = {EventPlayerPackets.class})
    private void run(EventPlayerPackets event) {
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
                if (entity instanceof FireballEntity fireballEntity) {
                    if (fireballEntity.distanceTo(Wrapper.INSTANCE.getLocalPlayer()) <= range) {
                        if (rotate) {
                            RotationVector rotation = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), fireballEntity);
                            event.setRotation(rotation);
                        }
                        if (swing) {
                            Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                        }
                        Wrapper.INSTANCE.getInteractionManager().attackEntity(Wrapper.INSTANCE.getLocalPlayer(), fireballEntity);
                    }
                }
            });
        }
    }
}
