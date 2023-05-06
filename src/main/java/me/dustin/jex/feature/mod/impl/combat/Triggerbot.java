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
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.Hand;
import me.dustin.jex.feature.mod.impl.settings.Targets;

public class Triggerbot extends Feature {

    public Property<Boolean> checkpressProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("When-holding-attack")
            .value(true)
            .build();

    public Triggerbot() {
        super(Category.COMBAT);
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getMinecraft().crosshairTarget instanceof EntityHitResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();
            boolean attack = Wrapper.INSTANCE.getOptions().attackKey.isPressed();
            if (checkpressProperty.value()) {
            if (isValid(entity) && attack && Wrapper.INSTANCE.getLocalPlayer().getAttackCooldownProgress(0) == 1) {
                Wrapper.INSTANCE.getClientPlayerInteractionManager().attackEntity(Wrapper.INSTANCE.getLocalPlayer(), entity);
                if (Targets.INSTANCE.swingProperty.value()) {
                Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                }
            }
            }
            else {
                if (isValid(entity) && Wrapper.INSTANCE.getLocalPlayer().getAttackCooldownProgress(0) == 1) {
                Wrapper.INSTANCE.getClientPlayerInteractionManager().attackEntity(Wrapper.INSTANCE.getLocalPlayer(), entity);
                 if (Targets.INSTANCE.swingProperty.value())
                Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                }
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    private boolean isValid(Entity entity) {
        if (!(entity instanceof LivingEntity))
            return Targets.INSTANCE.nolivingProperty.value();
        if (entity.isInvisible())
            return Targets.INSTANCE.invisiblesProperty.value();
        if (entity.hasCustomName())
            return Targets.INSTANCE.nametaggedProperty.value();
        if (EntityHelper.INSTANCE.isPassiveMob(entity))
            return Targets.INSTANCE.passiveProperty.value();
        if (EntityHelper.INSTANCE.isNeutralMob(entity))
            return Targets.INSTANCE.neutralProperty.value();
        if (EntityHelper.INSTANCE.isHostileMob(entity))
            return Targets.INSTANCE.hostileProperty.value();
        if (EntityHelper.INSTANCE.isBossMob(entity))
            return Targets.INSTANCE.bossProperty.value();
        if (entity instanceof PlayerEntity) {
            if (FriendHelper.INSTANCE.isFriend(entity.getName().getString()))
                return Targets.INSTANCE.friendProperty.value();
            return Targets.INSTANCE.playerProperty.value();
        }
        if (Targets.INSTANCE.specificFilterProperty.value()) {
            if (entity instanceof IronGolemEntity)
                return Targets.INSTANCE.ironGolemProperty.value();
            if (entity instanceof ZombifiedPiglinEntity)
                return Targets.INSTANCE.zombiePiglinProperty.value();
            if (entity instanceof PiglinEntity)
                return Targets.INSTANCE.piglinProperty.value();
        }
        return false;
    }
}
