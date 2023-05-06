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
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public class DepthStrider extends Feature {

    public final Property<Integer> levelProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Level")
            .value(3)
            .min(1)
            .max(3)
            .inc(1)
            .build();
    public final Property<Boolean> allowSprintingProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Allow Sprinting")
            .value(false)
            .build();

    public DepthStrider() {
        super(Category.MOVEMENT);
    }

    @EventPointer
    private final EventListener<EventMove> eventMoveEventListener = new EventListener<>(event -> {
        int enchLevel = levelProperty.value();
      Block block = WorldHelper.INSTANCE.getBlockBelowEntity(Wrapper.INSTANCE.getLocalPlayer(), 0.7f);
        if (InventoryHelper.INSTANCE.getSoulSpeedLevel() > enchLevel)
            enchLevel = InventoryHelper.INSTANCE.getSoulSpeedLevel();
        if (block == Blocks.SOUL_SAND) {
            PlayerHelper.INSTANCE.setMoveSpeed(event, PlayerHelper.INSTANCE.getSoulSandSpeed(enchLevel, allowSprintingProperty.value()));
        }
    });
}
