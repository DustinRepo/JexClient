package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.world.EventBreakBlock;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.Direction;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Prevent the game from creating ghost blocks.")
public class AntiGhostBlock extends Feature {
    @EventPointer
    private final EventListener<EventBreakBlock> eventBreakBlockEventListener = new EventListener<>(event -> NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, event.getPos(), Direction.UP)));
}
