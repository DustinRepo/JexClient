package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.misc.EventSetLevel;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.event.world.EventSpawnEntity;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Objects;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Notify you when a slime spawns and mark the chunk it spawned in as a slime chunk. Good for finding Slime Chunks on servers without the seed.")
public class SlimeSpawnMarker extends Feature {

    @Op(name = "Notify player")
    public boolean notifyPlayer = true;
    @Op(name = "Mark SlimeChunks")
    public boolean markSlimeChunks = true;
    @OpChild(name = "Write to file", parent = "Mark SlimeChunks")
    public boolean writeToFile = true;
    @OpChild(name = "Show SlimeChunks", parent = "Mark SlimeChunks")
    public boolean showSlimeChunks = true;
    @OpChild(name = "Chunk Color", isColor = true, parent = "Show SlimeChunks")
    public int chunkColor = new Color(0, 255, 38).getRGB();

    private final ArrayList<ChunkPos> chunkPositions = new ArrayList<>();
    private final File chunksFile = new File(ModFileHelper.INSTANCE.getJexDirectory(), "slimes.txt");

    @EventPointer
    private final EventListener<EventSpawnEntity> eventSpawnEntityEventListener = new EventListener<>(event -> {
        if (event.getEntity() instanceof SlimeEntity) {
            if (event.getEntity().getY() > 40)
                return;
            if (notifyPlayer)
                ChatHelper.INSTANCE.addClientMessage("A Slime has spawned in chunk: \247b" + event.getEntity().getChunkPos().x + " " + event.getEntity().getChunkPos().z);
            if (markSlimeChunks)
                if (!chunkPositions.contains(event.getEntity().getChunkPos())) {
                    chunkPositions.add(event.getEntity().getChunkPos());
                    if (writeToFile) {
                        try {
                            String server = Wrapper.INSTANCE.getMinecraft().isIntegratedServerRunning() ? "SP world" : Objects.requireNonNull(Wrapper.INSTANCE.getMinecraft().getCurrentServerEntry()).address;
                            String s = server + ":" + event.getEntity().getChunkPos().x + ":" + event.getEntity().getChunkPos().z + "\n";
                            FileWriter fileWritter = new FileWriter(chunksFile, true);
                            BufferedWriter bw = new BufferedWriter(fileWritter);
                            bw.write(s);
                            bw.close();
                        } catch (Exception e) {
                            ChatHelper.INSTANCE.addClientMessage("Could not write to slimes.txt");
                        }
                    }
                }
        }
    });

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        if (!markSlimeChunks)
            return;
        chunkPositions.forEach(chunkPos -> {
            if (Wrapper.INSTANCE.getWorld().getChunkManager().isChunkLoaded(chunkPos.x, chunkPos.z)) {
                Vec3d renderVec = Render3DHelper.INSTANCE.getRenderPosition(chunkPos.x * 16, -64, chunkPos.z * 16);
                Box box = new Box(renderVec.getX(), renderVec.getY(), renderVec.getZ(), renderVec.getX() + 16, renderVec.getY() + 64 + 40, renderVec.getZ() + 16);
                Render3DHelper.INSTANCE.drawBox(((EventRender3D) event).getPoseStack(), box, chunkColor);
            }
        });
    });

    @EventPointer
    private final EventListener<EventSetLevel> eventJoinWorldEventListener = new EventListener<>(event -> {
        chunkPositions.clear();
    });

    @Override
    public void onEnable() {
        if (!chunksFile.exists())
            FileHelper.INSTANCE.createFile(ModFileHelper.INSTANCE.getJexDirectory(), "slimes.txt");
        super.onEnable();
    }
}
