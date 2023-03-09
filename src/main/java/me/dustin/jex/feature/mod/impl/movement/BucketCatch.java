package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import me.dustin.jex.feature.mod.core.Feature;

public class BucketCatch extends Feature {

    public final Property<Double> fallDistanceProperty = new Property.PropertyBuilder<Double>(this.getClass())
            .name("Fall Distance")
            .value(5D)
            .min(3)
            .max(10)
            .inc(0.5f)
            .build();
    public final Property<Boolean> rotateProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Rotate")
            .value(false)
            .build();

    private boolean placedBucket;
    private boolean click;

    public BucketCatch() {
        super(Category.MOVEMENT);
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            if (placedBucket && Wrapper.INSTANCE.getLocalPlayer().fallDistance < fallDistanceProperty.value()) {
                int bucket = InventoryHelper.INSTANCE.getFromHotbar(Items.BUCKET);
                if (bucket != -1) {
                    InventoryHelper.INSTANCE.setSlot(bucket, true, true);
                    if (rotateProperty.value())
                        PlayerHelper.INSTANCE.setPitch(90);
                    event.setPitch(90);
                    if (Wrapper.INSTANCE.getLocalPlayer().isTouchingWater()) {
                        click = true;
                        placedBucket = false;
                        return;
                    }
                } else {
                    placedBucket = false;
                }
            }
            if (!placedBucket && Wrapper.INSTANCE.getLocalPlayer().fallDistance >= fallDistanceProperty.value() && EntityHelper.INSTANCE.distanceFromGround(Wrapper.INSTANCE.getLocalPlayer()) <= 3.5f && !placedBucket) {
                int waterBucketSlot = InventoryHelper.INSTANCE.getFromHotbar(Items.WATER_BUCKET);
                if (waterBucketSlot != -1) {
                    InventoryHelper.INSTANCE.setSlot(waterBucketSlot, true, true);
                    if (rotateProperty.value())
                        PlayerHelper.INSTANCE.setPitch(90);
                    event.setPitch(90);
                    BlockPos pos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().add(0, -3f, 0);
                    if (WorldHelper.INSTANCE.getBlock(pos) != Blocks.AIR) {
                        click = true;
                        placedBucket = true;
                    }
                }
            }
        } else if (click) {
            Wrapper.INSTANCE.getClientPlayerInteractionManager().interactItem(Wrapper.INSTANCE.getPlayer(), Hand.MAIN_HAND);
            click = false;
        }
    });
}
