package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.ChunkPos;

public class AntiVoid extends Feature {

    public final Property<Mode> modeProperty = new Property.PropertyBuilder<Mode>(this.getClass())
            .name("Mode")
            .value(Mode.ANTICHEAT)
            .build();

    public AntiVoid() {
        super(Category.MOVEMENT, "Prevent yourself from falling to the void");
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        setSuffix(modeProperty.value());
        ChunkPos chunkPos = Wrapper.INSTANCE.getPlayer().getChunkPos();
        if (modeProperty.value() == Mode.ANTICHEAT && Wrapper.INSTANCE.getPlayer().getY() <= Wrapper.INSTANCE.getWorld().getChunk(chunkPos.x, chunkPos.z).getBottomY())
            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getPlayer().getX(), Wrapper.INSTANCE.getPlayer().getY() * 2, Wrapper.INSTANCE.getPlayer().getZ(), true));
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventMove> eventMoveEventListener = new EventListener<>(event -> {
        ChunkPos chunkPos = Wrapper.INSTANCE.getPlayer().getChunkPos();
        if (modeProperty.value() == Mode.FLOAT) {
            if (Wrapper.INSTANCE.getPlayer().getY() < Wrapper.INSTANCE.getWorld().getChunk(chunkPos.x, chunkPos.z).getBottomY())
                event.setY(Wrapper.INSTANCE.getWorld().getChunk(chunkPos.x, chunkPos.z).getBottomY() - Wrapper.INSTANCE.getPlayer().getY());
            else if (Wrapper.INSTANCE.getPlayer().getY() == Wrapper.INSTANCE.getWorld().getChunk(chunkPos.x, chunkPos.z).getBottomY() && event.getY() < 0)
                event.setY(0);
        }
    });

    public enum Mode {
        ANTICHEAT, FLOAT
    }
}
