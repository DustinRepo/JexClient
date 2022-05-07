package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ServerPacketFilter;
import me.dustin.jex.event.misc.EventSetLevel;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.event.world.EventLoadChunk;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Attempts to determine which chunks are newly generated.")
public class NewChunks extends Feature {

    @Op(name = "New Chunk Color", isColor = true)
    public int newChunkColor = 0xffff0000;
    private final ArrayList<ChunkAccess> newChunks = new ArrayList<>();
    private final ArrayList<ChunkAccess> oldChunks = new ArrayList<>();

    @EventPointer
    private final EventListener<EventLoadChunk> eventLoadChunkEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getWorld() == null)
            return;
        LevelChunk chunk = event.getWorldChunk();
        if (chunk != null)
            new Thread(() -> {
            for (int x = 0; x < 16; x++)
                for (int y = chunk.getMinBuildHeight(); y < chunk.getHighestSectionPosition(); y++)
                    for (int z = 0; z < 16; z++) {
                        FluidState fluidState = WorldHelper.INSTANCE.getFluidState(new BlockPos(chunk.getPos().getBlockAt(x, y, z)));
                        if (fluidState != null && !fluidState.isEmpty()) {
                            if (!fluidState.isSource()) {
                                oldChunks.add(chunk);
                                return;
                            }
                        }
                    }
            }).start();
    });

    @EventPointer
    private final EventListener<EventPacketReceive> eventPacketReceiveEventListener = new EventListener<>(event -> {
        ClientboundBlockUpdatePacket blockUpdateS2CPacket = (ClientboundBlockUpdatePacket)event.getPacket();
        ChunkAccess chunk = Wrapper.INSTANCE.getWorld().getChunk(blockUpdateS2CPacket.getPos());
        if (blockUpdateS2CPacket.getBlockState().getFluidState().isEmpty() || blockUpdateS2CPacket.getBlockState().getFluidState().isSource())
            return;
        for (Direction dir : Direction.values()) {
            if (dir == Direction.DOWN) continue;
            if (!WorldHelper.INSTANCE.getFluidState(blockUpdateS2CPacket.getPos().relative(dir)).isEmpty() && !oldChunks.contains(chunk) && !newChunks.contains(chunk)) {
                newChunks.add(chunk);
                return;
            }
        }
    }, new ServerPacketFilter(EventPacketReceive.Mode.POST, ClientboundBlockUpdatePacket.class));

    @EventPointer
    private final EventListener<EventSetLevel> eventJoinWorldEventListener = new EventListener<>(event -> {
        newChunks.clear();
        oldChunks.clear();
    });

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        newChunks.forEach(chunk -> {
            if (!Wrapper.INSTANCE.getWorld().getChunkSource().hasChunk(chunk.getPos().x, chunk.getPos().z))
                return;
            Vec3 renderPos = Render3DHelper.INSTANCE.getRenderPosition(chunk.getPos().getMinBlockX(), chunk.getMinBuildHeight(), chunk.getPos().getMinBlockZ());
            AABB bb = new AABB(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 16, renderPos.y + 0.1f, renderPos.z + 16);
            Render3DHelper.INSTANCE.drawBox(event.getPoseStack(), bb, newChunkColor);
        });
    });

    @Override
    public void onDisable() {
        newChunks.clear();
        oldChunks.clear();
        super.onDisable();
    }
}
