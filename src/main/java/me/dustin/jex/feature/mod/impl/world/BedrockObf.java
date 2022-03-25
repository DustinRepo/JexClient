package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ServerPacketFilter;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Rearranges Bedrock at the bottom of the world and top of the Nether to avoid seed searching.")
public class BedrockObf extends Feature {

    private final ConcurrentLinkedQueue<Chunk> chunksToUpdate = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Chunk> obfuscatedChunks = new ConcurrentLinkedQueue<>();
    private final Random random = new Random();
    private Thread thread;

    @EventPointer
    private final EventListener<EventPacketReceive> eventPacketReceiveEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getWorld() == null)
            return;
        Chunk emptyChunk = null;

        Packet<?> packet = event.getPacket();

        if (packet instanceof ChunkDeltaUpdateS2CPacket chunkDeltaUpdateS2CPacket) {

            ArrayList<BlockPos> changeBlocks = new ArrayList<>();
            chunkDeltaUpdateS2CPacket.visitUpdates((pos, state) -> {
                changeBlocks.add(pos);
            });
            if (changeBlocks.isEmpty())
                return;
            emptyChunk = Wrapper.INSTANCE.getWorld().getChunk(changeBlocks.get(0));
        } else if (packet instanceof BlockUpdateS2CPacket) {
            emptyChunk = Wrapper.INSTANCE.getWorld().getChunk(((BlockUpdateS2CPacket) packet).getPos());
        } else if (packet instanceof ChunkDataS2CPacket chunkDataS2CPacket) {
            emptyChunk = Wrapper.INSTANCE.getWorld().getChunk(chunkDataS2CPacket.getX(), chunkDataS2CPacket.getZ());
        }

        if (emptyChunk != null) {
            for (Chunk chunk : obfuscatedChunks) {
                assert Wrapper.INSTANCE.getWorld() != null;
                if (!Wrapper.INSTANCE.getWorld().getChunkManager().isChunkLoaded(chunk.getPos().x, chunk.getPos().z))
                    obfuscatedChunks.remove(chunk);
            }
            int distance = Wrapper.INSTANCE.getOptions().getViewDistance();
            if (Wrapper.INSTANCE.getWorld() != null && Wrapper.INSTANCE.getLocalPlayer() != null) {
                for (int i = -distance; i < distance; i++) {
                    for (int j = -distance; j < distance; j++) {
                        Chunk chunk = Wrapper.INSTANCE.getWorld().getChunk(Wrapper.INSTANCE.getLocalPlayer().getChunkPos().x + i, Wrapper.INSTANCE.getLocalPlayer().getChunkPos().z + j);
                        if (chunk != null && !chunksToUpdate.contains(chunk) && !obfuscatedChunks.contains(chunk) && Wrapper.INSTANCE.getWorld().getChunkManager().isChunkLoaded(chunk.getPos().x, chunk.getPos().z)) {
                            chunksToUpdate.offer(chunk);
                        }
                    }
                }
            }
        }
    }, new ServerPacketFilter(EventPacketReceive.Mode.PRE, ChunkDeltaUpdateS2CPacket.class, ChunkDataS2CPacket.class, BlockUpdateS2CPacket.class));

    @Override
    public void onEnable() {
        (thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (this.getState() && Wrapper.INSTANCE.getWorld() != null)
                    for (Chunk chunk : chunksToUpdate) {
                        searchChunk(chunk);
                    }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                }
            }
        })).start();
        int distance = Wrapper.INSTANCE.getOptions().getViewDistance();
        if (Wrapper.INSTANCE.getWorld() != null && Wrapper.INSTANCE.getLocalPlayer() != null) {
            for (int i = -distance; i < distance; i++) {
                for (int j = -distance; j < distance; j++) {
                    Chunk chunk = Wrapper.INSTANCE.getWorld().getChunk(Wrapper.INSTANCE.getLocalPlayer().getChunkPos().x + i, Wrapper.INSTANCE.getLocalPlayer().getChunkPos().z + j);
                    if (chunk != null && !chunksToUpdate.contains(chunk))
                        chunksToUpdate.offer(chunk);
                }
            }
        }
        super.onEnable();
    }

    private void searchChunk(Chunk chunk) {
        if (obfuscatedChunks.contains(chunk))
            return;
        boolean isNether = WorldHelper.INSTANCE.getDimensionID() != null && WorldHelper.INSTANCE.getDimensionID().getPath().contains("nether");
        BlockState replaceState = isNether ? Blocks.NETHERRACK.getDefaultState() : Blocks.DEEPSLATE.getDefaultState();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = chunk.getBottomY(); y < 5; y++) {
                    BlockPos blockPos = new BlockPos(x + (chunk.getPos().x * 16), y, z + (chunk.getPos().z * 16));
                    Block block = WorldHelper.INSTANCE.getBlock(blockPos);
                    if (block == Blocks.BEDROCK || block == Blocks.STONE || block == Blocks.NETHERRACK) {
                        Wrapper.INSTANCE.getWorld().setBlockState(blockPos, random.nextBoolean() ? Blocks.BEDROCK.getDefaultState() : replaceState);
                    }
                }
            }
        }
        if (isNether) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 123; y < 127; y++) {
                        BlockPos blockPos = new BlockPos(x + (chunk.getPos().x * 16), y, z + (chunk.getPos().z * 16));
                        Block block = WorldHelper.INSTANCE.getBlock(blockPos);
                        if (block == Blocks.BEDROCK || block == Blocks.STONE || block == Blocks.NETHERRACK) {
                            Wrapper.INSTANCE.getWorld().setBlockState(blockPos, random.nextBoolean() ? Blocks.BEDROCK.getDefaultState() : replaceState);
                        }
                    }
                }
            }
        }
        chunksToUpdate.remove(chunk);
        obfuscatedChunks.offer(chunk);
    }

    @Override
    public void onDisable() {
        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
        }
        this.chunksToUpdate.clear();
        this.obfuscatedChunks.clear();
        super.onDisable();
    }
}
