package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ServerPacketFilter;
import me.dustin.jex.event.misc.EventSetLevel;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.event.world.EventLoadChunk;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

import java.awt.*;
import java.util.ArrayList;

public class NewChunks extends Feature {

    public final Property<Color> newChunkColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("New Chunk Color")
            .value(Color.RED)
            .build();
    private final ArrayList<Chunk> newChunks = new ArrayList<>();
    private final ArrayList<Chunk> oldChunks = new ArrayList<>();

    public NewChunks() {
        super(Category.WORLD, "Attempts to determine which chunks are newly generated.");
    }

    @EventPointer
    private final EventListener<EventLoadChunk> eventLoadChunkEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getWorld() == null)
            return;
        WorldChunk chunk = event.getWorldChunk();
        if (chunk != null)
            new Thread(() -> {
            for (int x = 0; x < 16; x++)
            for (int z = 0; z < 16; z++)
            for (int y = chunk.getBottomY(); y < chunk.getHighestNonEmptySectionYOffset(); y++) {
            FluidState fluidState = WorldHelper.INSTANCE.getFluidState(new BlockPos(chunk.getPos().getBlockPos(x, y, z)));
            if (fluidState != null && !fluidState.isEmpty()) {
            if (!fluidState.isStill()) {
            oldChunks.add(chunk);
            return;
               }
            }
        }
    }).start();
});

    @EventPointer
    private final EventListener<EventPacketReceive> eventPacketReceiveEventListener = new EventListener<>(event -> {
        BlockUpdateS2CPacket blockUpdateS2CPacket = (BlockUpdateS2CPacket)event.getPacket();
        Chunk chunk = Wrapper.INSTANCE.getWorld().getChunk(blockUpdateS2CPacket.getPos());
        if (blockUpdateS2CPacket.getState().getFluidState().isEmpty() || blockUpdateS2CPacket.getState().getFluidState().isStill())
            return;
        for (Direction dir : Direction.values()) {
            if (dir == Direction.DOWN) continue;
            if (!WorldHelper.INSTANCE.getFluidState(blockUpdateS2CPacket.getPos().offset(dir)).isEmpty() && !oldChunks.contains(chunk) && !newChunks.contains(chunk)) {
                newChunks.add(chunk);
                return;
            }
        }
    }, new ServerPacketFilter(EventPacketReceive.Mode.POST, BlockUpdateS2CPacket.class));

    @EventPointer
    private final EventListener<EventSetLevel> eventJoinWorldEventListener = new EventListener<>(event -> {
        newChunks.clear();
        oldChunks.clear();
    });

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        newChunks.forEach(chunk -> {
            if (!Wrapper.INSTANCE.getWorld().getChunkManager().isChunkLoaded(chunk.getPos().x, chunk.getPos().z))
                return;
            Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(chunk.getPos().getStartX(), chunk.getBottomY(), chunk.getPos().getStartZ());
            Box bb = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 16, renderPos.y + 0.1f, renderPos.z + 16);
            Render3DHelper.INSTANCE.drawBox(event.getPoseStack(), bb, newChunkColorProperty.value().getRGB());
        });
    });

    @Override
    public void onDisable() {
        newChunks.clear();
        oldChunks.clear();
        super.onDisable();
    }
}
