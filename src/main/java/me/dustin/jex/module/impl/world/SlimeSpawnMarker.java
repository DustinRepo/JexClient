package me.dustin.jex.module.impl.world;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventJoinWorld;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.event.world.EventSpawnEntity;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;

@ModClass(name = "SlimeSpawnMarker", category = ModCategory.WORLD, description = "Notify you when a slime spawns and mark the chunk it spawned in as a slime chunk. Good for finding Slime Chunks on servers without the seed.")
public class SlimeSpawnMarker extends Module {

    @Op(name = "Notify player")
    public boolean notifyPlayer = true;
    @Op(name = "Mark SlimeChunks")
    public boolean markSlimeChunks = true;
    @OpChild(name = "Show SlimeChunks", parent = "Mark SlimeChunks")
    public boolean showSlimeChunks = true;
    @OpChild(name = "Chunk Color", isColor = true, parent = "Show SlimeChunks")
    public int chunkColor = new Color(0, 255, 38).getRGB();

    private ArrayList<ChunkPos> chunkPositions = new ArrayList<>();

    @EventListener(events = {EventSpawnEntity.class, EventRender3D.class, EventJoinWorld.class})
    private void runMethod(Event event) {
        if (event instanceof EventJoinWorld) {
            chunkPositions.clear();
        }
        if (event instanceof EventSpawnEntity) {
            EventSpawnEntity eventSpawnEntity = (EventSpawnEntity)event;
            if (eventSpawnEntity.getEntity() instanceof SlimeEntity) {
                if (eventSpawnEntity.getEntity().getY() > 40)
                    return;
                ChunkPos chunkPos = new ChunkPos(eventSpawnEntity.getEntity().getBlockPos());
                if (notifyPlayer)
                    ChatHelper.INSTANCE.addClientMessage("A Slime has spawned in chunk: \247b" + chunkPos.x + " " + chunkPos.z);
                if (markSlimeChunks)
                if (!chunkPositions.contains(chunkPos))
                    chunkPositions.add(chunkPos);
            }
        }
        if (event instanceof EventRender3D) {
            if (!markSlimeChunks)
                return;
            chunkPositions.forEach(chunkPos -> {
                if (Wrapper.INSTANCE.getWorld().getChunkManager().isChunkLoaded(chunkPos.x, chunkPos.z)) {
                    Vec3d renderVec = Render3DHelper.INSTANCE.getRenderPosition(chunkPos.x * 16, 0, chunkPos.z * 16);
                    Box box = new Box(renderVec.getX(), renderVec.getY(), renderVec.getZ(), renderVec.getX() + 16, renderVec.getY() + 40, renderVec.getZ() + 16);
                    Render3DHelper.INSTANCE.drawBox(box, chunkColor);
                }
            });
        }
    }
}
