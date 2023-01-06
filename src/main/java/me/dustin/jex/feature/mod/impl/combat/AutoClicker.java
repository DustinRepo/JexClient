package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.FriendHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

public class AttackClicker extends Feature {

    public AttackClicker() {
        super(Category.COMBAT, "Automatically click ");
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
             boolean attack = Wrapper.INSTANCE.getOptions().attackKey.isPressed();
            if (attack && Wrapper.INSTANCE.getLocalPlayer().getAttackCooldownProgress(0) == 1) {
                Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
            }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
