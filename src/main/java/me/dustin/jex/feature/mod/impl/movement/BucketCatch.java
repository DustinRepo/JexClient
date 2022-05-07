package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;

@Feature.Manifest(category = Feature.Category.MOVEMENT, description = "Place a water bucket under yourself when you fall to avoid fall damage. 90% of the time, it works every time")
public class BucketCatch extends Feature {

    @Op(name = "Fall Distance", min = 3, max = 10)
    public int fallDistance = 5;
    @Op(name = "Rotate")
    public boolean rotate = false;

    private boolean placedBucket;
    private boolean click;

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            if (placedBucket && Wrapper.INSTANCE.getLocalPlayer().fallDistance < fallDistance) {
                int bucket = InventoryHelper.INSTANCE.getFromHotbar(Items.BUCKET);
                if (bucket != -1) {
                    InventoryHelper.INSTANCE.setSlot(bucket, true, true);
                    if (rotate)
                        PlayerHelper.INSTANCE.setPitch(90);
                    event.setPitch(90);
                    if (Wrapper.INSTANCE.getLocalPlayer().isInWater()) {
                        click = true;
                        placedBucket = false;
                        return;
                    }
                } else {
                    placedBucket = false;
                }
            }
            if (!placedBucket && Wrapper.INSTANCE.getLocalPlayer().fallDistance >= fallDistance && EntityHelper.INSTANCE.distanceFromGround(Wrapper.INSTANCE.getLocalPlayer()) <= 3.5f && !placedBucket) {
                int waterBucketSlot = InventoryHelper.INSTANCE.getFromHotbar(Items.WATER_BUCKET);
                if (waterBucketSlot != -1) {
                    InventoryHelper.INSTANCE.setSlot(waterBucketSlot, true, true);
                    if (rotate)
                        PlayerHelper.INSTANCE.setPitch(90);
                    event.setPitch(90);
                    BlockPos pos = Wrapper.INSTANCE.getLocalPlayer().blockPosition().offset(0, -3f, 0);
                    if (WorldHelper.INSTANCE.getBlock(pos) != Blocks.AIR) {
                        click = true;
                        placedBucket = true;
                    }
                }
            }
        } else if (click) {
            Wrapper.INSTANCE.getMultiPlayerGameMode().useItem(Wrapper.INSTANCE.getPlayer(), InteractionHand.MAIN_HAND);
            click = false;
        }
    });
}
