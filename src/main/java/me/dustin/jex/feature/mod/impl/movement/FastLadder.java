package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;

@Feature.Manifest(category = Feature.Category.MOVEMENT, description = "Move faster on ladders")
public class FastLadder extends Feature {

    @Op(name = "SkipLadder")
    public boolean skipLadder = true;

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (isOnLadder() && Wrapper.INSTANCE.getLocalPlayer().horizontalCollision) {
            Block footBlock = WorldHelper.INSTANCE.getBlock(Wrapper.INSTANCE.getLocalPlayer().getBlockPos());
            PlayerHelper.INSTANCE.setVelocityY(footBlock == Blocks.AIR && skipLadder ? 0.4f : 0.2873F);
        }
        setSuffix(skipLadder ? "SkipLadder" : "");
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    private boolean isOnLadder() {
        Block footBlock = WorldHelper.INSTANCE.getBlock(Wrapper.INSTANCE.getLocalPlayer().getBlockPos());
        Block headBlock = WorldHelper.INSTANCE.getBlock(Wrapper.INSTANCE.getLocalPlayer().getBlockPos().up());
        return footBlock instanceof LadderBlock || headBlock instanceof LadderBlock;
    }
}
