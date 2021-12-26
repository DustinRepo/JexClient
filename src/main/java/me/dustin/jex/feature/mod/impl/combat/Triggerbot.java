package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.FriendHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

@Feature.Manifest(category = Feature.Category.COMBAT, description = "Automatically attack entities you hover over")
public class Triggerbot extends Feature {

    @Op(name = "Player")
    public boolean players = true;
    @Op(name = "Passive")
    public boolean passives = true;
    @Op(name = "Hostile")
    public boolean hostiles = true;
    @Op(name = "Neutral")
    public boolean neutrals = true;

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getMinecraft().crosshairTarget instanceof EntityHitResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();
            if (isValid(entity) && Wrapper.INSTANCE.getLocalPlayer().getAttackCooldownProgress(0) == 1) {
                Wrapper.INSTANCE.getInteractionManager().attackEntity(Wrapper.INSTANCE.getLocalPlayer(), entity);
                Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    private boolean isValid(Entity entity) {
        if (!(entity instanceof LivingEntity))
            return false;
        if (EntityHelper.INSTANCE.isPassiveMob(entity))
            return passives;
        if (EntityHelper.INSTANCE.isNeutralMob(entity))
            return neutrals;
        if (EntityHelper.INSTANCE.isHostileMob(entity))
            return hostiles;
        if (entity instanceof PlayerEntity playerEntity && !FriendHelper.INSTANCE.isFriend(playerEntity))
            return players;
        return false;
    }
}
