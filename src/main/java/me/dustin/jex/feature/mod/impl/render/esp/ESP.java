package me.dustin.jex.feature.mod.impl.render.esp;

import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.misc.EventSetLevel;
import me.dustin.jex.event.render.*;
import me.dustin.jex.feature.mod.core.FeatureExtension;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.impl.render.esp.impl.OutlineBox;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.FriendHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.esp.impl.BoxESP;
import me.dustin.jex.feature.mod.impl.render.esp.impl.ShaderESP;
import me.dustin.jex.feature.mod.impl.render.esp.impl.TwoDeeESP;
import java.awt.*;

public class ESP extends Feature {
    public static ESP INSTANCE;

    public final Property<Mode> modeProperty = new Property.PropertyBuilder<Mode>(this.getClass())
            .name("Mode")
            .value(Mode.SHADER)
            .build();
    public final Property<Boolean> playerProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Player")
            .value(true)
            .build();
    public final Property<Boolean> colorOnDistanceProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Color on Distance")
            .description("Change the color from green (far away) to red (close).")
            .value(false)
            .parent(playerProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Color> playerColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Player Color")
            .value(Color.RED)
            .parent(playerProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Color> friendColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Friend Color")
            .value(Color.BLUE)
            .parent(playerProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> neutralProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Neutral")
            .value(true)
            .build();
    public final Property<Color> neutralColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Neutral Color")
            .value(Color.PINK)
            .parent(neutralProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> bossProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Boss")
            .value(true)
            .build();
    public final Property<Color> bossColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Boss Color")
            .value(Color.RED.darker())
            .parent(bossProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> hostileProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Hostile")
            .value(true)
            .build();
    public final Property<Color> hostileColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Hostile Color")
            .value(Color.ORANGE)
            .parent(hostileProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> passiveProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Passive")
            .value(true)
            .build();
    public final Property<Color> passiveColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Passive Color")
            .value(Color.GREEN)
            .parent(passiveProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Color> petColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Pets Color")
            .value(Color.BLUE)
            .parent(passiveColorProperty)
            .build();
    public final Property<Boolean> itemProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Item")
            .value(true)
            .build();
    public final Property<Color> itemColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Item Color")
            .value(Color.WHITE)
            .parent(itemProperty)
            .depends(parent -> (boolean) parent.value())
            .build();

    private Mode lastMode;

    public ESP() {
        super(Category.VISUAL, "Mark entities/players through walls");
        new ShaderESP();
        new BoxESP();
        new OutlineBox();
        new TwoDeeESP();
        INSTANCE = this;
    }

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> sendEvent(event));
    @EventPointer
    private final EventListener<EventRender2D> eventRender2DEventListener = new EventListener<>(event -> sendEvent(event));
    @EventPointer
    private final EventListener<EventRender2DNoScale> eventRender2DNoScaleEventListener = new EventListener<>(event -> sendEvent(event));
    @EventPointer
    private final EventListener<EventTeamColor> eventOutlineColorEventListener = new EventListener<>(event -> sendEvent(event));
    @EventPointer
    private final EventListener<EventSetLevel> eventJoinWorldEventListener = new EventListener<>(event -> sendEvent(event));
    @EventPointer
    private final EventListener<EventHasOutline> eventHasOutlineEventListener = new EventListener<>(event -> sendEvent(event));

    private void sendEvent(Event event) {
        if (lastMode != null && modeProperty.value() != lastMode) {
            FeatureExtension.get(lastMode, this).disable();
            FeatureExtension.get(modeProperty.value(), this).enable();
        }
        FeatureExtension.get(modeProperty.value(), this).pass(event);
        this.setSuffix(modeProperty.value());
        lastMode = modeProperty.value();
    }

    @Override
    public void onEnable() {
        FeatureExtension.get(modeProperty.value(), this).enable();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        FeatureExtension.get(modeProperty.value(), this).disable();
        super.onDisable();
    }

    public boolean isValid(Entity entity) {
        if (entity == null)
            return false;
        if (entity instanceof ItemEntity)
            return itemProperty.value();
        if (!(entity instanceof LivingEntity livingEntity))
            return false;
        if (livingEntity == Wrapper.INSTANCE.getLocalPlayer())
            return false;
        if (livingEntity instanceof PlayerEntity && EntityHelper.INSTANCE.isNPC((PlayerEntity) livingEntity))
            return false;
        if (livingEntity instanceof PlayerEntity)
            return playerProperty.value();
        if (EntityHelper.INSTANCE.isNeutralMob(entity))
            return neutralProperty.value();
        if (EntityHelper.INSTANCE.isBossMob(entity))
            return bossProperty.value();
        if (EntityHelper.INSTANCE.isHostileMob(entity))
            return hostileProperty.value();
        if (EntityHelper.INSTANCE.isPassiveMob(entity))
            return passiveProperty.value();
        return false;
    }

    public int getColor(Entity entity) {
        if (entity instanceof ItemEntity)
            return itemColorProperty.value().getRGB();
        if (FriendHelper.INSTANCE.isFriend(entity.getName().getString()))
            return friendColorProperty.value().getRGB();
        if (entity instanceof PlayerEntity) {
            if (colorOnDistanceProperty.value()) {
                return ColorHelper.INSTANCE.redGreenShift(entity.distanceTo(Wrapper.INSTANCE.getLocalPlayer()) / 64);
            }
            return playerColorProperty.value().getRGB();
        }

        if (EntityHelper.INSTANCE.isPassiveMob(entity))
            if (EntityHelper.INSTANCE.doesPlayerOwn(entity))
                return petColorProperty.value().getRGB();
            else
                return passiveColorProperty.value().getRGB();
        if (EntityHelper.INSTANCE.isBossMob(entity))
            return bossColorProperty.value().getRGB();
        if (EntityHelper.INSTANCE.isHostileMob(entity))
            return hostileColorProperty.value().getRGB();
        if (EntityHelper.INSTANCE.isNeutralMob(entity))
            return neutralColorProperty.value().getRGB();
        return -1;
    }

    public enum Mode {
        SHADER, TWO_DEE, BOX_OUTLINE, BOX
    }
}
