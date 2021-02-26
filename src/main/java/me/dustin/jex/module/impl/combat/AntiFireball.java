package me.dustin.jex.module.impl.combat;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.Hand;

@ModClass(name = "AntiFireball", category = ModCategory.COMBAT, description = "Knock away fireballs from ghasts")
public class AntiFireball extends Module {

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
                if (entity instanceof FireballEntity) {
                    FireballEntity fireballEntity = (FireballEntity) entity;
                    if (fireballEntity.distanceTo(Wrapper.INSTANCE.getLocalPlayer()) <= range) {
                        if (rotate) {
                            float[] rotation = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), fireballEntity);
                            event.setYaw(rotation[0]);
                            event.setYaw(rotation[1]);
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
