package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.mod.core.Feature;

public class DepthStrider extends Feature {

    public final Property<Integer> levelProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Level")
            .value(3)
            .min(1)
            .max(20)
            .build();
    public final Property<Boolean> allowSprintingProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Allow Sprinting")
            .value(false)
            .build();

    public DepthStrider() {
        super(Category.MOVEMENT, "Move through water like you have Depth Strider boots on");
    }

    @EventPointer
    private final EventListener<EventMove> eventMoveEventListener = new EventListener<>(event -> {
        int enchLevel = levelProperty.value();
        if (InventoryHelper.INSTANCE.getDepthStriderLevel() > enchLevel)
            enchLevel = InventoryHelper.INSTANCE.getDepthStriderLevel();
        if (WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getLocalPlayer())) {
            PlayerHelper.INSTANCE.setMoveSpeed(event, PlayerHelper.INSTANCE.getWaterSpeed(enchLevel, allowSprintingProperty.value()));
        }
    });
}
