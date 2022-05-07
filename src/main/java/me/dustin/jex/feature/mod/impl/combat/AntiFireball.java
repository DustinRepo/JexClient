package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.LargeFireball;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.events.core.annotate.EventPointer;

@Feature.Manifest(category = Feature.Category.COMBAT, description = "Knock away fireballs from ghasts")
public class AntiFireball extends Feature {

    @Op(name = "Range", min = 1, max = 6, inc = 0.1f)
    public float range = 5;

    @Op(name = "Rotate")
    public boolean rotate = true;
    @Op(name = "Swing")
    public boolean swing = true;

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> Wrapper.INSTANCE.getWorld().entitiesForRendering().forEach(entity -> {
        if (entity instanceof LargeFireball fireballEntity) {
            if (fireballEntity.distanceTo(Wrapper.INSTANCE.getLocalPlayer()) <= range) {
                if (rotate) {
                    RotationVector rotation = PlayerHelper.INSTANCE.rotateToEntity(fireballEntity);
                    event.setRotation(rotation);
                }
                if (swing) {
                    Wrapper.INSTANCE.getLocalPlayer().swing(InteractionHand.MAIN_HAND);
                }
                Wrapper.INSTANCE.getMultiPlayerGameMode().attack(Wrapper.INSTANCE.getLocalPlayer(), fireballEntity);
            }
        }
    }), new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
