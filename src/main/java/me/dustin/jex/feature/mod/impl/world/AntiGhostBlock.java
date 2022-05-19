package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.world.EventBreakBlock;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.Direction;
import me.dustin.jex.feature.mod.core.Feature;

public class AntiGhostBlock extends Feature {

    public AntiGhostBlock() {
        super(Category.WORLD, "Prevent the game from creating ghost blocks.");
    }

    @EventPointer
    private final EventListener<EventBreakBlock> eventBreakBlockEventListener = new EventListener<>(event -> NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, event.getPos(), Direction.UP)));
}
