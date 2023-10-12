package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ServerPacketFilter;
import me.dustin.jex.event.misc.EventSetLevel;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import net.minecraft.block.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import me.dustin.jex.feature.mod.core.Feature;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TunnelFinder extends Feature {

    public final Property<Color> colorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Color")
            .value(new Color(175, 250, 0))
            .build();

    private final ConcurrentLinkedQueue<BlockPos> positions = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Chunk> chunksToUpdate = new ConcurrentLinkedQueue<>();

    private Thread thread;

    public TunnelFinder() {
        super(Category.VISUAL);
    }

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
            int distance = Wrapper.INSTANCE.getOptions().getViewDistance().getValue();
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
    }, new ServerPacketFilter(EventPacketReceive.Mode.PRE, ChunkDeltaUpdateS2CPacket.class, ChunkDataS2CPacket.class, BlockUpdateS2CPacket.class));

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        for (BlockPos pos : positions) {
            if (ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().getPos(), new Vec3d(pos.getX(), pos.getY(), pos.getZ())) > 256) {
                positions.remove(pos);
                continue;
            }
            Vec3d entityPos = Render3DHelper.INSTANCE.getRenderPosition(new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
            Box box = new Box(entityPos.x, entityPos.y, entityPos.z, entityPos.x + 1, entityPos.y + 2, entityPos.z + 1);
            Render3DHelper.INSTANCE.drawBoxOutline(event.getPoseStack(), box, colorProperty.value().getRGB());
        }
    });

    @EventPointer
    private final EventListener<EventSetLevel> eventJoinWorldEventListener = new EventListener<>(event -> {
        positions.clear();
        int distance = Wrapper.INSTANCE.getOptions().getViewDistance().getValue();
        if (Wrapper.INSTANCE.getWorld() != null && Wrapper.INSTANCE.getLocalPlayer() != null) {
            for (int i = -distance; i < distance; i++) {
                for (int j = -distance; j < distance; j++) {
                    Chunk chunk = Wrapper.INSTANCE.getWorld().getChunk(Wrapper.INSTANCE.getLocalPlayer().getChunkPos().x + i, Wrapper.INSTANCE.getLocalPlayer().getChunkPos().z + j);
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
                    for (Chunk chunk : chunksToUpdate) {
                        searchChunk(chunk);
                    }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                }
            }
        })).start();
        int distance = Wrapper.INSTANCE.getOptions().getViewDistance().getValue();
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

    @Override
    public void onDisable() {
        positions.clear();
        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
        }
        super.onDisable();
    }

    public void searchChunk(Chunk chunk) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = chunk.getBottomY(); y < chunk.getHighestNonEmptySectionYOffset() + 16; y++) {
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
