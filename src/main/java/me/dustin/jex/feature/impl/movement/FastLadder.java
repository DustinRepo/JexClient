package me.dustin.jex.feature.impl.movement;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;

@Feature.Manifest(name = "FastLadder", category = Feature.Category.MOVEMENT, description = "Move faster on ladders")
public class FastLadder extends Feature {

    @Op(name = "SkipLadder")
    public boolean skipLadder = true;

    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            if (isOnLadder() && Wrapper.INSTANCE.getLocalPlayer().horizontalCollision) {
                Block footBlock = WorldHelper.INSTANCE.getBlock(Wrapper.INSTANCE.getLocalPlayer().getBlockPos());
                PlayerHelper.INSTANCE.setVelocityY(footBlock == Blocks.AIR && skipLadder ? 0.4f : 0.2873F);
            }
            setSuffix(skipLadder ? "SkipLadder" : "");
        }
    }

    private boolean isOnLadder() {
        Block footBlock = WorldHelper.INSTANCE.getBlock(Wrapper.INSTANCE.getLocalPlayer().getBlockPos());
        Block headBlock = WorldHelper.INSTANCE.getBlock(Wrapper.INSTANCE.getLocalPlayer().getBlockPos().up());
        return footBlock instanceof LadderBlock || headBlock instanceof LadderBlock;
    }
}
