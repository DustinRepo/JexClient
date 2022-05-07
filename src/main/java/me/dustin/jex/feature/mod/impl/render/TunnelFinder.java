package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ServerPacketFilter;
import me.dustin.jex.event.misc.EventSetLevel;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Find tunnels in the nether that might lead to bases.")
public class TunnelFinder extends Feature {

    @Op(name = "Color", isColor = true)
    public int color = new Color(175, 250, 0).getRGB();


    private final ConcurrentLinkedQueue<BlockPos> positions = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<ChunkAccess> chunksToUpdate = new ConcurrentLinkedQueue<>();

    private Thread thread;

    @EventPointer
    private final EventListener<EventPacketReceive> eventPacketReceiveEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getWorld() == null)
            return;
        ChunkAccess emptyChunk = null;

        Packet<?> packet = event.getPacket();

        if (packet instanceof ClientboundSectionBlocksUpdatePacket chunkDeltaUpdateS2CPacket) {

            ArrayList<BlockPos> changeBlocks = new ArrayList<>();
            chunkDeltaUpdateS2CPacket.runUpdates((pos, state) -> {
                changeBlocks.add(pos);
            });
            if (changeBlocks.isEmpty())
                return;
            emptyChunk = Wrapper.INSTANCE.getWorld().getChunk(changeBlocks.get(0));
        } else if (packet instanceof ClientboundBlockUpdatePacket) {
            emptyChunk = Wrapper.INSTANCE.getWorld().getChunk(((ClientboundBlockUpdatePacket) packet).getPos());
        } else if (packet instanceof ClientboundLevelChunkWithLightPacket chunkDataS2CPacket) {
            emptyChunk = Wrapper.INSTANCE.getWorld().getChunk(chunkDataS2CPacket.getX(), chunkDataS2CPacket.getZ());
        }

        if (emptyChunk != null) {
            int distance = Wrapper.INSTANCE.getOptions().renderDistance().get();
            if (Wrapper.INSTANCE.getWorld() != null && Wrapper.INSTANCE.getLocalPlayer() != null) {
                for (int i = -distance; i < distance; i++) {
                    for (int j = -distance; j < distance; j++) {
                        ChunkAccess chunk = Wrapper.INSTANCE.getWorld().getChunk(Wrapper.INSTANCE.getLocalPlayer().chunkPosition().x + i, Wrapper.INSTANCE.getLocalPlayer().chunkPosition().z + j);
                        if (chunk != null && !chunksToUpdate.contains(chunk))
                            chunksToUpdate.offer(chunk);
                    }
                }
            }
        }
    }, new ServerPacketFilter(EventPacketReceive.Mode.PRE, ClientboundSectionBlocksUpdatePacket.class, ClientboundLevelChunkWithLightPacket.class, ClientboundBlockUpdatePacket.class));

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        for (BlockPos pos : positions) {
            if (ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().position(), new Vec3(pos.getX(), pos.getY(), pos.getZ())) > 256) {
                positions.remove(pos);
                continue;
            }
            Vec3 entityPos = Render3DHelper.INSTANCE.getRenderPosition(new Vec3(pos.getX(), pos.getY(), pos.getZ()));
            AABB box = new AABB(entityPos.x, entityPos.y, entityPos.z, entityPos.x + 1, entityPos.y + 2, entityPos.z + 1);
            Render3DHelper.INSTANCE.drawBoxOutline(event.getPoseStack(), box, color);
        }
    });

    @EventPointer
    private final EventListener<EventSetLevel> eventJoinWorldEventListener = new EventListener<>(event -> {
        positions.clear();
        int distance = Wrapper.INSTANCE.getOptions().renderDistance().get();
        if (Wrapper.INSTANCE.getWorld() != null && Wrapper.INSTANCE.getLocalPlayer() != null) {
            for (int i = -distance; i < distance; i++) {
                for (int j = -distance; j < distance; j++) {
                    ChunkAccess chunk = Wrapper.INSTANCE.getWorld().getChunk(Wrapper.INSTANCE.getLocalPlayer().chunkPosition().x + i, Wrapper.INSTANCE.getLocalPlayer().chunkPosition().z + j);
                    if (chunk != null && !chunksToUpdate.contains(chunk))
                        chunksToUpdate.offer(chunk);
                }
            }
        }
    });

    @Override
    public void onEnable() {
        (thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (this.getState() && Wrapper.INSTANCE.getWorld() != null)
                    for (ChunkAccess chunk : chunksToUpdate) {
                        searchChunk(chunk);
                    }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                }
            }
        })).start();
        int distance = Wrapper.INSTANCE.getOptions().renderDistance().get();
        if (Wrapper.INSTANCE.getWorld() != null && Wrapper.INSTANCE.getLocalPlayer() != null) {
            for (int i = -distance; i < distance; i++) {
                for (int j = -distance; j < distance; j++) {
                    ChunkAccess chunk = Wrapper.INSTANCE.getWorld().getChunk(Wrapper.INSTANCE.getLocalPlayer().chunkPosition().x + i, Wrapper.INSTANCE.getLocalPlayer().chunkPosition().z + j);
                    if (chunk != null && !chunksToUpdate.contains(chunk))
                        chunksToUpdate.offer(chunk);
                }
            }
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        positions.clear();
        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
        }
        super.onDisable();
    }

    public void searchChunk(ChunkAccess chunk) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = chunk.getMinBuildHeight(); y < chunk.getHighestSectionPosition() + 16; y++) {
                    BlockPos pos = new BlockPos(x + (chunk.getPos().x * 16), y, z + (chunk.getPos().z * 16));
                    if ((Wrapper.INSTANCE.getWorld().getBlockState(pos).getBlock() == Blocks.AIR) && (Wrapper.INSTANCE.getWorld().getBlockState(pos.above(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.below(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.above(2)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.north(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.south(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.above(1).north(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.above(1).south(1)).getBlock() == Blocks.AIR) && (Wrapper.INSTANCE.getWorld().getBlockState(pos.west(1)).getBlock() == Blocks.AIR) && (Wrapper.INSTANCE.getWorld().getBlockState(pos.west(1).above(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.west(1).below(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.west(1).above(2)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.west(1).north(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.west(1).south(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.west(1).above(1).north(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.west(1).above(1).south(1)).getBlock() == Blocks.AIR) || (Wrapper.INSTANCE.getWorld().getBlockState(pos).getBlock() == Blocks.AIR) && (Wrapper.INSTANCE.getWorld().getBlockState(pos.above(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.below(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.above(2)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.west(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.east(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.above(1).west(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.above(1).east(1)).getBlock() == Blocks.AIR) && (Wrapper.INSTANCE.getWorld().getBlockState(pos.north(1)).getBlock() == Blocks.AIR) && (Wrapper.INSTANCE.getWorld().getBlockState(pos.north(1).above(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.north(1).below(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.north(1).above(2)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.north(1).west(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.north(1).east(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.north(1).above(1).west(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.north(1).above(1).east(1)).getBlock() == Blocks.AIR)) {
                        if (!this.positions.contains(pos))
                            this.positions.offer(pos);
                    }
                }
            }
        }
        chunksToUpdate.remove(chunk);
    }


}
