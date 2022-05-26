package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.FriendHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class WebSpam extends Feature {

    public final Property<Long> delayProperty = new Property.PropertyBuilder<Long>(this.getClass())
            .name("Delay (MS)")
            .value(50L)
            .max(1000)
            .inc(10)
            .build();
    public final Property<Integer> rangeProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Range")
            .value(5)
            .min(2)
            .max(6)
            .build();
    public final Property<Boolean> friendsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Friends")
            .description("Whether or not to web your friends.")
            .value(false)
            .build();

    private final StopWatch stopWatch = new StopWatch();

    public WebSpam() {
        super(Category.COMBAT, "Spam webs at players feet to slow them down");
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (!stopWatch.hasPassed(delayProperty.value()))
            return;
        int hotbarWeb = InventoryHelper.INSTANCE.getFromHotbar(Items.COBWEB);
        int invWeb = InventoryHelper.INSTANCE.getFromInv(Items.COBWEB);
        if (hotbarWeb == -1 && invWeb == -1)
            return;
        if (hotbarWeb == -1) {
            InventoryHelper.INSTANCE.swapToHotbar(invWeb, 8);
            hotbarWeb = 8;
        }
        for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
            if (entity instanceof PlayerEntity playerEntity && playerEntity != Wrapper.INSTANCE.getPlayer() && (friendsProperty.value() || !FriendHelper.INSTANCE.isFriend(playerEntity))) {
                if (playerEntity.distanceTo(Wrapper.INSTANCE.getPlayer()) <= rangeProperty.value() && WorldHelper.INSTANCE.getBlock(playerEntity.getBlockPos()) == Blocks.AIR && PlayerHelper.INSTANCE.canPlaceHere(playerEntity.getBlockPos())) {
                    InventoryHelper.INSTANCE.setSlot(hotbarWeb, true, true);
                    PlayerHelper.INSTANCE.placeBlockInPos(playerEntity.getBlockPos(), Hand.MAIN_HAND, false);
                    stopWatch.reset();
                    if (delayProperty.value() != 0)
                        return;
                }
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

}
