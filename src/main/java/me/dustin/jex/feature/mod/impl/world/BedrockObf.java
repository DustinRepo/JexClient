package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ServerPacketFilter;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import me.dustin.jex.feature.mod.core.Feature;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Rearranges Bedrock at the bottom of the world and top of the Nether to avoid seed searching.")
public class BedrockObf extends Feature {

    private final ConcurrentLinkedQueue<ChunkAccess> chunksToUpdate = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<ChunkAccess> obfuscatedChunks = new ConcurrentLinkedQueue<>();
    private final Random random = new Random();
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
            for (ChunkAccess chunk : obfuscatedChunks) {
                assert Wrapper.INSTANCE.getWorld() != null;
                if (!Wrapper.INSTANCE.getWorld().getChunkSource().hasChunk(chunk.getPos().x, chunk.getPos().z))
                    obfuscatedChunks.remove(chunk);
            }
            int distance = Wrapper.INSTANCE.getOptions().renderDistance().get();
            if (Wrapper.INSTANCE.getWorld() != null && Wrapper.INSTANCE.getLocalPlayer() != null) {
                for (int i = -distance; i < distance; i++) {
                    for (int j = -distance; j < distance; j++) {
                        ChunkAccess chunk = Wrapper.INSTANCE.getWorld().getChunk(Wrapper.INSTANCE.getLocalPlayer().chunkPosition().x + i, Wrapper.INSTANCE.getLocalPlayer().chunkPosition().z + j);
                        if (chunk != null && !chunksToUpdate.contains(chunk) && !obfuscatedChunks.contains(chunk) && Wrapper.INSTANCE.getWorld().getChunkSource().hasChunk(chunk.getPos().x, chunk.getPos().z)) {
                            chunksToUpdate.offer(chunk);
                        }
                    }
                }
            }
        }
    }, new ServerPacketFilter(EventPacketReceive.Mode.PRE, ClientboundSectionBlocksUpdatePacket.class, ClientboundLevelChunkWithLightPacket.class, ClientboundBlockUpdatePacket.class));

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

    private void searchChunk(ChunkAccess chunk) {
        if (obfuscatedChunks.contains(chunk))
            return;
        boolean isNether = WorldHelper.INSTANCE.getDimensionID() != null && WorldHelper.INSTANCE.getDimensionID().getPath().contains("nether");
        BlockState replaceState = isNether ? Blocks.NETHERRACK.defaultBlockState() : Blocks.DEEPSLATE.defaultBlockState();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = chunk.getMinBuildHeight(); y < 5; y++) {
                    BlockPos blockPos = new BlockPos(x + (chunk.getPos().x * 16), y, z + (chunk.getPos().z * 16));
                    Block block = WorldHelper.INSTANCE.getBlock(blockPos);
                    if (block == Blocks.BEDROCK || block == Blocks.STONE || block == Blocks.NETHERRACK) {
                        Wrapper.INSTANCE.getWorld().setBlockAndUpdate(blockPos, random.nextBoolean() ? Blocks.BEDROCK.defaultBlockState() : replaceState);
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
                            Wrapper.INSTANCE.getWorld().setBlockAndUpdate(blockPos, random.nextBoolean() ? Blocks.BEDROCK.defaultBlockState() : replaceState);
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
