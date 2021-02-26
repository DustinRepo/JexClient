package me.dustin.jex.module.impl.world;

import io.netty.util.internal.ConcurrentSet;
import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventJoinWorld;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;

@ModClass(name = "NewChunks", category = ModCategory.WORLD, description = "Shows newly generated chunks on old Spigot servers.")
public class NewChunks extends Module {

    @Op(name = "Chunk Color", isColor = true)
    public int chunkColor = 0xffff0000;
    private ConcurrentSet<Chunk> newChunks = new ConcurrentSet<>();

    @EventListener(events = {EventRender3D.class, EventPacketReceive.class, EventJoinWorld.class})
    public void run(Event event) {
        if (event.equals(EventJoinWorld.class))
            newChunks.clear();
        if (event.equals(EventPacketReceive.class)) {
            if (((EventPacketReceive) event).getPacket() instanceof ChunkDataS2CPacket) {
                ChunkDataS2CPacket chunkData = (ChunkDataS2CPacket) ((EventPacketReceive) event).getPacket();
                if (Wrapper.INSTANCE.getWorld() == null)
                    return;
                Chunk chunk = Wrapper.INSTANCE.getWorld().getChunk(chunkData.getX(), chunkData.getZ());
                if (!chunkData.isFullChunk()) {
                    if (!newChunks.contains(chunk))
                    newChunks.add(chunk);
                }
            }
        }
        if (event.equals(EventRender3D.class)) {
            newChunks.forEach(chunk -> {
                if (!Wrapper.INSTANCE.getWorld().getChunkManager().isChunkLoaded(chunk.getPos().x, chunk.getPos().z))
                    return;
                Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(chunk.getPos().x * 16, 0, chunk.getPos().z * 16, ((EventRender3D) event).getPartialTicks());
                Box bb = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 16, renderPos.y + 0.1f, renderPos.z + 16);
                Render3DHelper.INSTANCE.drawBox(bb, chunkColor);
            });
        }
    }

}
