package me.dustin.jex.module.impl.render;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventJoinWorld;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.block.Blocks;
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
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.lwjgl.opengl.GL11.*;

@ModClass(name = "TunnelFinder", category = ModCategory.VISUAL, description = "Find tunnels in the nether that might lead to bases.")
public class TunnelFinder extends Module {

    @Op(name = "Color", isColor = true)
    public int color = new Color(175, 250, 0).getRGB();


    private ConcurrentLinkedQueue<BlockPos> positions = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Chunk> chunksToUpdate = new ConcurrentLinkedQueue<>();

    public TunnelFinder() {
        new Thread(() -> {
            while (true) {
                if (this.getState() && Wrapper.INSTANCE.getWorld() != null)
                    for (Chunk chunk : chunksToUpdate) {
                        searchChunk(chunk);
                    }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
            }
        }).start();
    }

    @EventListener(events = {EventPacketReceive.class, EventRender3D.class, EventJoinWorld.class})
    private void runMethod(Event event) {
        if (event instanceof EventPacketReceive) {
            if (Wrapper.INSTANCE.getWorld() == null)
                return;
            Chunk emptyChunk = null;

            Packet packet = ((EventPacketReceive) event).getPacket();

            if (packet instanceof ChunkDeltaUpdateS2CPacket) {
                ChunkDeltaUpdateS2CPacket chunkDeltaUpdateS2CPacket = (ChunkDeltaUpdateS2CPacket) packet;

                ArrayList<BlockPos> changeBlocks = new ArrayList<>();
                chunkDeltaUpdateS2CPacket.visitUpdates((pos, state) -> {
                    changeBlocks.add(pos);
                });
                if (changeBlocks.isEmpty())
                    return;
                emptyChunk = Wrapper.INSTANCE.getWorld().getChunk(changeBlocks.get(0));
            } else if (packet instanceof BlockUpdateS2CPacket) {
                BlockUpdateS2CPacket blockUpdateS2CPacket = (BlockUpdateS2CPacket) packet;
                emptyChunk = Wrapper.INSTANCE.getWorld().getChunk(((BlockUpdateS2CPacket) packet).getPos());
            } else if (packet instanceof ChunkDataS2CPacket) {
                ChunkDataS2CPacket chunkDataS2CPacket = (ChunkDataS2CPacket) packet;
                emptyChunk = Wrapper.INSTANCE.getWorld().getChunk(chunkDataS2CPacket.getX(), chunkDataS2CPacket.getZ());
            }

            if (emptyChunk != null) {
                int distance = Wrapper.INSTANCE.getOptions().viewDistance;
                if (Wrapper.INSTANCE.getWorld() != null && Wrapper.INSTANCE.getLocalPlayer() != null) {
                    for (int i = -distance; i < distance; i++) {
                        for (int j = -distance; j < distance; j++) {
                            Chunk chunk = Wrapper.INSTANCE.getWorld().getChunk(Wrapper.INSTANCE.getLocalPlayer().chunkX + i, Wrapper.INSTANCE.getLocalPlayer().chunkZ + j);
                            if (chunk != null && !chunksToUpdate.contains(chunk))
                                chunksToUpdate.offer(chunk);
                        }
                    }
                }
            }
        } else if (event instanceof EventRender3D) {
            for (BlockPos pos : positions) {
                if (ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().getPos(), new Vec3d(pos.getX(), pos.getY(), pos.getZ())) > 256) {
                    positions.remove(pos);
                    continue;
                }

                Entity cameraEntity = Wrapper.INSTANCE.getMinecraft().getCameraEntity();
                assert cameraEntity != null;
                Vec3d entityPos = Render3DHelper.INSTANCE.getRenderPosition(new Vec3d(pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f));

                Box box = new Box(entityPos.x - 0.5f, entityPos.y, entityPos.z - 0.5f, entityPos.x + 1 - 0.5f, entityPos.y + 2, entityPos.z + 1 - 0.5f);

                glPushMatrix();
                glEnable(GL_LINE_SMOOTH);
                glDisable(GL_TEXTURE_2D);
                glEnable(GL_CULL_FACE);
                glDisable(GL_DEPTH_TEST);
                glDisable(GL_LIGHTING);
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                Render2DHelper.INSTANCE.glColor(color);
                glLineWidth(1);
                Render3DHelper.INSTANCE.drawOutlineBox(box);

                glColor4f(1, 1, 1, 1);
                glEnable(GL_DEPTH_TEST);
                glEnable(GL_TEXTURE_2D);
                glDisable(GL_LINE_SMOOTH);
                glPopMatrix();

            }
        } else if (event instanceof EventJoinWorld) {
            int distance = Wrapper.INSTANCE.getOptions().viewDistance;
            if (Wrapper.INSTANCE.getWorld() != null && Wrapper.INSTANCE.getLocalPlayer() != null) {
                for (int i = -distance; i < distance; i++) {
                    for (int j = -distance; j < distance; j++) {
                        Chunk chunk = Wrapper.INSTANCE.getWorld().getChunk(Wrapper.INSTANCE.getLocalPlayer().chunkX + i, Wrapper.INSTANCE.getLocalPlayer().chunkZ + j);
                        if (chunk != null && !chunksToUpdate.contains(chunk))
                            chunksToUpdate.offer(chunk);
                    }
                }
            }
        }
    }

    @Override
    public void onEnable() {
        int distance = Wrapper.INSTANCE.getOptions().viewDistance;
        if (Wrapper.INSTANCE.getWorld() != null && Wrapper.INSTANCE.getLocalPlayer() != null) {
            for (int i = -distance; i < distance; i++) {
                for (int j = -distance; j < distance; j++) {
                    Chunk chunk = Wrapper.INSTANCE.getWorld().getChunk(Wrapper.INSTANCE.getLocalPlayer().chunkX + i, Wrapper.INSTANCE.getLocalPlayer().chunkZ + j);
                    if (chunk != null && !chunksToUpdate.contains(chunk))
                        chunksToUpdate.offer(chunk);
                }
            }
        }
        super.onEnable();
    }

    public void searchChunk(Chunk chunk) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < chunk.getHighestNonEmptySectionYOffset() + 16; y++) {
                    BlockPos pos = new BlockPos(x + (chunk.getPos().x * 16), y, z + (chunk.getPos().z * 16));
                    if ((Wrapper.INSTANCE.getWorld().getBlockState(pos).getBlock() == Blocks.AIR) && (Wrapper.INSTANCE.getWorld().getBlockState(pos.up(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.down(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.up(2)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.north(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.south(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.up(1).north(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.up(1).south(1)).getBlock() == Blocks.AIR) && (Wrapper.INSTANCE.getWorld().getBlockState(pos.west(1)).getBlock() == Blocks.AIR) && (Wrapper.INSTANCE.getWorld().getBlockState(pos.west(1).up(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.west(1).down(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.west(1).up(2)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.west(1).north(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.west(1).south(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.west(1).up(1).north(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.west(1).up(1).south(1)).getBlock() == Blocks.AIR) || (Wrapper.INSTANCE.getWorld().getBlockState(pos).getBlock() == Blocks.AIR) && (Wrapper.INSTANCE.getWorld().getBlockState(pos.up(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.down(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.up(2)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.west(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.east(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.up(1).west(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.up(1).east(1)).getBlock() == Blocks.AIR) && (Wrapper.INSTANCE.getWorld().getBlockState(pos.north(1)).getBlock() == Blocks.AIR) && (Wrapper.INSTANCE.getWorld().getBlockState(pos.north(1).up(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.north(1).down(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.north(1).up(2)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.north(1).west(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.north(1).east(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.north(1).up(1).west(1)).getBlock() == Blocks.AIR) && !(Wrapper.INSTANCE.getWorld().getBlockState(pos.north(1).up(1).east(1)).getBlock() == Blocks.AIR)) {
                        if (!this.positions.contains(pos))
                            this.positions.offer(pos);
                    }
                }
            }
        }
        chunksToUpdate.remove(chunk);
    }


}
