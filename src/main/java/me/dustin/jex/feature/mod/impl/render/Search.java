package me.dustin.jex.feature.mod.impl.render;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventJoinWorld;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.option.annotate.OpChild;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.Render3DHelper.BoxStorage;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.*;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;

import java.awt.*;
import java.io.File;
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
    private ConcurrentLinkedQueue<Chunk> chunksToUpdate = new ConcurrentLinkedQueue<>();

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
                    for (Chunk chunk : chunksToUpdate) {
                        searchChunk(chunk);
                    }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                }
            }
        })).start();
        if (Wrapper.INSTANCE.getWorld() != null) {
            int distance = Wrapper.INSTANCE.getOptions().viewDistance;
            if (Wrapper.INSTANCE.getLocalPlayer() != null) {
                for (int i = -distance; i < distance; i++) {
                    for (int j = -distance; j < distance; j++) {
                        Chunk chunk = Wrapper.INSTANCE.getWorld().getChunk(Wrapper.INSTANCE.getLocalPlayer().getChunkPos().x + i, Wrapper.INSTANCE.getLocalPlayer().getChunkPos().z + j);
                        if (chunk != null && !chunksToUpdate.contains(chunk)) {
                            chunksToUpdate.offer(chunk);
                        }
                    }
                }
            }
        }
        super.onEnable();
    }

    @EventListener(events = {EventRender3D.class, EventRender3D.EventRender3DNoBob.class, EventPacketReceive.class, EventJoinWorld.class})
    private void runMethod(Event event) {
        if (event instanceof EventPacketReceive eventPacketReceive) {
            if (eventPacketReceive.getMode() != EventPacketReceive.Mode.PRE)
                return;
            if (Wrapper.INSTANCE.getWorld() == null)
                return;
            Chunk emptyChunk = null;

            Packet<?> packet = eventPacketReceive.getPacket();

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
                int distance = Wrapper.INSTANCE.getOptions().viewDistance;
                if (Wrapper.INSTANCE.getWorld() != null && Wrapper.INSTANCE.getLocalPlayer() != null) {
                    for (int i = -distance; i < distance; i++) {
                        for (int j = -distance; j < distance; j++) {
                            Chunk chunk = Wrapper.INSTANCE.getWorld().getChunk(Wrapper.INSTANCE.getLocalPlayer().getChunkPos().x + i, Wrapper.INSTANCE.getLocalPlayer().getChunkPos().z + j);
                            if (chunk != null && !chunksToUpdate.contains(chunk))
                                chunksToUpdate.offer(chunk);
                        }
                    }
                }
            }
        } else if (event instanceof EventRender3D.EventRender3DNoBob) {
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
                Vec3d entityPos = Render3DHelper.INSTANCE.getRenderPosition(new Vec3d(pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f));

                Color color1 = ColorHelper.INSTANCE.getColor(blocks.get(block));

                Render3DHelper.INSTANCE.setup3DRender(true);
                RenderSystem.setShader(GameRenderer::getPositionColorShader);

                Vec3d eyes = new Vec3d(0, 0, 1).rotateX(-(float) Math.toRadians(PlayerHelper.INSTANCE.getPitch())).rotateY(-(float) Math.toRadians(PlayerHelper.INSTANCE.getYaw()));

                BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
                bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
                bufferBuilder.vertex(eyes.x, eyes.y, eyes.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                bufferBuilder.vertex(entityPos.x, entityPos.y, entityPos.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                bufferBuilder.end();
                BufferRenderer.draw(bufferBuilder);

                Render3DHelper.INSTANCE.end3DRender();
            }
        } else if (event instanceof EventRender3D eventRender3D) {
            ArrayList<BoxStorage> boxList = new ArrayList<>();
            for (BlockPos pos : worldBlocks.keySet()) {
            	Block block = worldBlocks.get(pos);
                if (WorldHelper.INSTANCE.getBlock(pos) != block) {
                    worldBlocks.remove(pos);
                    continue;
                }
                Entity cameraEntity = Wrapper.INSTANCE.getMinecraft().getCameraEntity();
                assert cameraEntity != null;
                if (limitRange && ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().getPos(), ClientMathHelper.INSTANCE.getVec(pos)) > range)
                    continue;
                Vec3d entityPos = Render3DHelper.INSTANCE.getRenderPosition(new Vec3d(pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f));
                Box box = new Box(entityPos.x - 0.5f, entityPos.y, entityPos.z - 0.5f, entityPos.x + 1 - 0.5f, entityPos.y + 1, entityPos.z + 1 - 0.5f);
                BoxStorage boxStorage = new BoxStorage(box, blocks.get(block));
                boxList.add(boxStorage);
            }
            Render3DHelper.INSTANCE.drawList(eventRender3D.getMatrixStack(), boxList);
        } else if (event instanceof EventJoinWorld) {
            int distance = Wrapper.INSTANCE.getOptions().viewDistance;
            if (Wrapper.INSTANCE.getWorld() != null && Wrapper.INSTANCE.getLocalPlayer() != null) {
                for (int i = -distance; i < distance; i++) {
                    for (int j = -distance; j < distance; j++) {
                        Chunk chunk = Wrapper.INSTANCE.getWorld().getChunk(Wrapper.INSTANCE.getLocalPlayer().getChunkPos().x + i, Wrapper.INSTANCE.getLocalPlayer().getChunkPos().z + j);
                        if (chunk != null && !chunksToUpdate.contains(chunk))
                            chunksToUpdate.offer(chunk);
                    }
                }
            }
        }
    }

    public void searchChunk(Chunk chunk) {
        try {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = chunk.getBottomY(); y < chunk.getHighestNonEmptySectionYOffset() + 16; y++) {
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
