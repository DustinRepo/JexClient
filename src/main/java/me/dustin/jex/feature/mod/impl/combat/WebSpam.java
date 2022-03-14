package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
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

@Feature.Manifest(category = Feature.Category.COMBAT, description = "Spam webs at players feet to slow them down")
public class WebSpam extends Feature {

    @Op(name = "Delay (MS)", max = 1000, inc = 10)
    public int delay = 50;
    @Op(name = "Range", min = 2, max = 6)
    public int range = 5;
    @Op(name = "Friends")
    public boolean friends;

    private final StopWatch stopWatch = new StopWatch();

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (!stopWatch.hasPassed(delay))
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
            if (entity instanceof PlayerEntity playerEntity && playerEntity != Wrapper.INSTANCE.getPlayer() && (friends || !FriendHelper.INSTANCE.isFriend(playerEntity))) {
                if (playerEntity.distanceTo(Wrapper.INSTANCE.getPlayer()) <= range && WorldHelper.INSTANCE.getBlock(playerEntity.getBlockPos()) == Blocks.AIR && PlayerHelper.INSTANCE.canPlaceHere(playerEntity.getBlockPos())) {
                    InventoryHelper.INSTANCE.setSlot(hotbarWeb, true, true);
                    PlayerHelper.INSTANCE.placeBlockInPos(playerEntity.getBlockPos(), Hand.MAIN_HAND, false);
                    stopWatch.reset();
                    if (delay != 0)
                        return;
                }
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

}
