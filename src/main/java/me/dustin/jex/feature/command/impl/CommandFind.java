package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.JexClient;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.command.core.arguments.MessageArgumentType;
import me.dustin.jex.feature.command.core.arguments.Vec3ArgumentType;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.world.seed.SeedHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.world.WorldHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.world.GeneratorType;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.StructureFeature;
import org.apache.commons.lang3.StringUtils;

@Cmd(name = "find", description = "Find structures with a given seed, like the /locate command", syntax = {".find <structure> <seed>", ".find <structure> <searchPos> <seed>"})
public class CommandFind extends Command {
    private StructureFeature<?> structureFeature;
    private BlockPos startPos;
    @Override
    public void registerCommand() {
        LiteralArgumentBuilder<FabricClientCommandSource> argumentBuilder = literal(this.name);
        StructureFeature.STRUCTURES.forEach((s, structureFeature) -> {
            argumentBuilder.then(literal(s).then(argument("pos", Vec3ArgumentType.vec3()).then(argument("seed", MessageArgumentType.message()).executes(context -> {
                this.structureFeature = structureFeature;
                this.startPos = new BlockPos(Vec3ArgumentType.getVec3(context, "pos"));
                return run(context);
            }))).then(argument("seed", MessageArgumentType.message()).executes(context -> {
                this.structureFeature = structureFeature;
                return run(context);
            })));
        });
        dispatcher.register(argumentBuilder);
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        long seed = SeedHelper.INSTANCE.getSeed(MessageArgumentType.getMessage(context, "seed").asString()).getAsLong();
        if (startPos == null)
            startPos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos();
        new Thread("structure_finder") {
            @Override
            public void run() {
                ChatHelper.INSTANCE.addClientMessage("Creating fake server for world gen... (This may take a moment)");
                IntegratedServer integratedServer = WorldHelper.INSTANCE.createIntegratedServer(this, seed, GeneratorType.DEFAULT);
                if (integratedServer == null) {
                    ChatHelper.INSTANCE.addClientMessage("Error: Could not create integrated server");
                    return;
                }
                ServerWorld serverWorld = switch (WorldHelper.INSTANCE.getDimensionID().toString()) {
                    case "minecraft:the_nether" -> integratedServer.getWorld(World.NETHER);
                    case "minecraft:the_end" -> integratedServer.getWorld(World.END);
                    default -> integratedServer.getOverworld();
                };
                if (serverWorld != null) {
                    JexClient.INSTANCE.getLogger().info(serverWorld.getStructureAccessor().getStructureAt(startPos, StructureFeature.VILLAGE));
                    BlockPos pos = serverWorld.locateStructure(structureFeature, startPos, 100, false);
                    if (pos != null) {
                        String posString = "BlockPos: X: \247b" + pos.getX() + (pos.getY() == 0 ? "" : " \2477Y: \247b" + pos.getY()) + " \2477Z: \247b" + pos.getZ();
                        ChatHelper.INSTANCE.addClientMessage(StringUtils.capitalize(structureFeature.getName()) + " found at " + posString);
                    } else {
                        ChatHelper.INSTANCE.addClientMessage("Could not find structure");
                    }
                } else {
                    ChatHelper.INSTANCE.addClientMessage("ServerWorld null");
                }
                startPos = null;
                JexClient.INSTANCE.getLogger().info("Shutting down integrated server");
                integratedServer.shutdown();
                JexClient.INSTANCE.getLogger().info("Cleaning up integrated server");
                //WorldHelper.INSTANCE.cleanupIntegratedServer();
            }
        }.start();
        return 1;
    }
}
