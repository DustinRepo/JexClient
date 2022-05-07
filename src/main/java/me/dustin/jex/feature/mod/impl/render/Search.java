package me.dustin.jex.feature.mod.impl.render;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ServerPacketFilter;
import me.dustin.jex.event.misc.EventSetLevel;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.option.annotate.OpChild;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Search for a specific block. use \".help search\"")
public class Search extends Feature {

    private static ConcurrentMap<Block, Integer> blocks = Maps.newConcurrentMap();
    private static ConcurrentMap<BlockPos, Block> worldBlocks = Maps.newConcurrentMap();
    @Op(name = "Tracers")
    public boolean tracers;
    @Op(name = "Limit Range")
    public boolean limitRange = false;
    @OpChild(name = "Range", min = 10, max = 100, parent = "Limit Range")
    public int range = 25;

    private Thread thread;
    private final ConcurrentLinkedQueue<ChunkAccess> chunksToUpdate = new ConcurrentLinkedQueue<>();

    public static void firstLoad() {
        blocks.put(Blocks.DIAMOND_ORE, new Color(0, 150, 255).getRGB());
        blocks.put(Blocks.NETHER_PORTAL, new Color(106, 0, 255).getRGB());
    }

    public static ConcurrentMap<Block, Integer> getBlocks() {
        return blocks;
    }

    @Override
    public void onEnable() {
        (thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (Wrapper.INSTANCE.getWorld() != null)
                    for (ChunkAccess chunk : chunksToUpdate) {
                        searchChunk(chunk);
                    }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                }
            }
        })).start();
        if (Wrapper.INSTANCE.getWorld() != null) {
            int distance = Wrapper.INSTANCE.getOptions().renderDistance().get();
            if (Wrapper.INSTANCE.getLocalPlayer() != null) {
                for (int i = -distance; i < distance; i++) {
                    for (int j = -distance; j < distance; j++) {
                        ChunkAccess chunk = Wrapper.INSTANCE.getWorld().getChunk(Wrapper.INSTANCE.getLocalPlayer().chunkPosition().x + i, Wrapper.INSTANCE.getLocalPlayer().chunkPosition().z + j);
                        if (chunk != null && !chunksToUpdate.contains(chunk)) {
                            chunksToUpdate.offer(chunk);
                        }
                    }
                }
            }
        }
        super.onEnable();
    }

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        if (blocks == null || blocks.isEmpty())
            return;
        ArrayList<Render3DHelper.BoxStorage> boxList = new ArrayList<>();
        for (BlockPos pos : worldBlocks.keySet()) {
            Block block = worldBlocks.get(pos);
            if (WorldHelper.INSTANCE.getBlock(pos) != block) {
                worldBlocks.remove(pos);
                continue;
            }
            Entity cameraEntity = Wrapper.INSTANCE.getMinecraft().getCameraEntity();
            assert cameraEntity != null;
            if (limitRange && ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().position(), ClientMathHelper.INSTANCE.getVec(pos)) > range)
                continue;
            Vec3 entityPos = Render3DHelper.INSTANCE.getRenderPosition(new Vec3(pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f));
            AABB box = new AABB(entityPos.x - 0.5f, entityPos.y, entityPos.z - 0.5f, entityPos.x + 1 - 0.5f, entityPos.y + 1, entityPos.z + 1 - 0.5f);
            Render3DHelper.BoxStorage boxStorage = new Render3DHelper.BoxStorage(box, blocks.get(block));
            boxList.add(boxStorage);
        }
        Render3DHelper.INSTANCE.drawList(event.getPoseStack(), boxList, true);
    });

    @EventPointer
    private final EventListener<EventRender3D.EventRender3DNoBob> eventRender3DNoBobEventListener = new EventListener<>(event -> {
        if (!tracers)
            return;
        for (BlockPos pos : worldBlocks.keySet()) {
            Block block = worldBlocks.get(pos);
            if (!blocks.containsKey(block) || WorldHelper.INSTANCE.getBlock(pos) != block) {
                worldBlocks.remove(pos);
                continue;
            }
            Entity cameraEntity = Wrapper.INSTANCE.getMinecraft().getCameraEntity();
            assert cameraEntity != null;
            Vec3 entityPos = Render3DHelper.INSTANCE.getRenderPosition(new Vec3(pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f));

            Color color1 = ColorHelper.INSTANCE.getColor(blocks.get(block));

            Render3DHelper.INSTANCE.setup3DRender(true);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            Vec3 eyes = new Vec3(0, 0, 1).xRot(-(float) Math.toRadians(PlayerHelper.INSTANCE.getPitch())).yRot(-(float) Math.toRadians(PlayerHelper.INSTANCE.getYaw()));

            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            bufferBuilder.vertex(eyes.x, eyes.y, eyes.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
            bufferBuilder.vertex(entityPos.x, entityPos.y, entityPos.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
            bufferBuilder.clear();
            BufferUploader.drawWithShader(bufferBuilder.end());

            Render3DHelper.INSTANCE.end3DRender();
        }
    });

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
    private final EventListener<EventSetLevel> eventJoinWorldEventListener = new EventListener<>(event -> {
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

    public void searchChunk(ChunkAccess chunk) {
        try {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = chunk.getMinBuildHeight(); y < chunk.getHighestSectionPosition() + 16; y++) {
                        BlockPos blockPos = new BlockPos(x + (chunk.getPos().x * 16), y, z + (chunk.getPos().z * 16));
                        Block block = WorldHelper.INSTANCE.getBlock(blockPos);
                        if (block != null && blocks != null && blocks.containsKey(block)) {
                            worldBlocks.put(blockPos, block);
                        } else worldBlocks.remove(blockPos);
                    }
                }
            }
        }catch (Exception ignored){}
        chunksToUpdate.remove(chunk);
    }

    @Override
    public void onDisable() {
        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
        }
        this.chunksToUpdate.clear();
        worldBlocks.clear();
        super.onDisable();
    }
}
