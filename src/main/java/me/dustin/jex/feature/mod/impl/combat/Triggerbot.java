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
import net.minecraft.util.hit.EntityHitResult;

public class Triggerbot extends Feature {

    public Property<Boolean> playersProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Player")
            .value(true)
            .build();
    public final Property<Boolean> bossProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Boss")
            .value(true)
            .build();
    public Property<Boolean> hostilesProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Hostile")
            .value(true)
            .build();
    public Property<Boolean> passivesProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Passive")
            .value(true)
            .build();
    public Property<Boolean> neutralsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Neutral")
            .value(true)
            .build();
    public final Property<Boolean> nolivingProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("NoLiving")
            .value(true)
            .build();
    public Property<Boolean> checkpressProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("When-holding-attack")
            .value(true)
            .build();
    public final Property<Boolean> swingProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Swing")
            .value(true)
            .build();

    public Triggerbot() {
        super(Category.COMBAT, "Automatically attack entities you hover over");
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getMinecraft().crosshairTarget instanceof EntityHitResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();
            boolean attack = Wrapper.INSTANCE.getOptions().attackKey.isPressed();
            if (checkpressProperty.value()) {
            if (isValid(entity) && attack && Wrapper.INSTANCE.getLocalPlayer().getAttackCooldownProgress(0) == 1) {
                Wrapper.INSTANCE.getClientPlayerInteractionManager().attackEntity(Wrapper.INSTANCE.getLocalPlayer(), entity);
                if (swingProperty.value()) 
                Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
            }
            }
            else {
                if (isValid(entity) && Wrapper.INSTANCE.getLocalPlayer().getAttackCooldownProgress(0) == 1) {
                Wrapper.INSTANCE.getClientPlayerInteractionManager().attackEntity(Wrapper.INSTANCE.getLocalPlayer(), entity);
                 if (swingProperty.value())
                Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                }
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    private boolean isValid(Entity entity) {
        if (!(entity instanceof LivingEntity))
            return nolivingProperty.value();
        if (EntityHelper.INSTANCE.isPassiveMob(entity))
            return passivesProperty.value();
        if (EntityHelper.INSTANCE.isNeutralMob(entity))
            return neutralsProperty.value();
        if (EntityHelper.INSTANCE.isHostileMob(entity))
            return hostilesProperty.value();
        if (EntityHelper.INSTANCE.isBossMob(entity))
            return bossProperty.value();
        if (entity instanceof PlayerEntity playerEntity && !FriendHelper.INSTANCE.isFriend(playerEntity))
            return playersProperty.value();
        return false;
    }
}
