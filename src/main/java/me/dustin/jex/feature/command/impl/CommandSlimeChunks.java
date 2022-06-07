package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.events.EventManager;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.command.core.arguments.MessageArgumentType;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.helper.world.SeedHelper;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;

@Cmd(name = "slimechunks", description = "Show slime chunks with a given seed", syntax = ".slimechunk <seed>")
public class CommandSlimeChunks extends Command {
    private long seed;

    @Override
    public void registerCommand() {
        dispatcher.register(literal(this.name).executes(context -> {EventManager.unregister(this); return 1;}).then(argument("seed", MessageArgumentType.message()).executes(this)));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        seed = SeedHelper.INSTANCE.getSeed(MessageArgumentType.getMessage(context, "seed").getString()).getAsLong();
        EventManager.register(this);
        return 1;
    }

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        ArrayList<Render3DHelper.BoxStorage> slimeChunks = new ArrayList<>();
       int renderDistance = Wrapper.INSTANCE.getOptions().getViewDistance().getValue();
       int divBy2 = renderDistance / 2 + 1;
        for (int x = -divBy2; x < divBy2; x++) {
            for (int z = -divBy2; z < divBy2; z++) {
                ChunkPos chunkPos = new ChunkPos(Wrapper.INSTANCE.getLocalPlayer().getChunkPos().x + x, Wrapper.INSTANCE.getLocalPlayer().getChunkPos().z + z);
                if (WorldHelper.INSTANCE.isSlimeChunk(seed, chunkPos.x, chunkPos.z)) {
                    Vec3d renderVec = Render3DHelper.INSTANCE.getRenderPosition(chunkPos.x * 16, 0, chunkPos.z * 16);
                    Box box = new Box(0, -64, 0, 16, 40, 16).offset(renderVec);
                    slimeChunks.add(new Render3DHelper.BoxStorage(box, 0xff00ff00));
                }
            }
        }
        Render3DHelper.INSTANCE.drawList(event.getPoseStack(), slimeChunks, true);
    });

    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
       if (Wrapper.INSTANCE.getLocalPlayer() == null)
           EventManager.unregister(this);
    });
}
