package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.command.core.arguments.MessageArgumentType;
import me.dustin.jex.feature.command.core.arguments.Vec3ArgumentType;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.world.WorldHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.world.GeneratorType;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.StructureFeature;
import org.apache.commons.lang3.StringUtils;

import java.util.OptionalLong;

@Cmd(name = "find", description = "Find structures with a given seed, like the /locate command", syntax = ".find <structure> <seed> <startPos(optional)>")
public class CommandFind extends Command {
    private StructureFeature<?> structureFeature;
    private BlockPos startPos;
    @Override
    public void registerCommand() {
        LiteralArgumentBuilder<FabricClientCommandSource> argumentBuilder = literal(this.name);
        StructureFeature.STRUCTURES.forEach((s, structureFeature) -> {
            argumentBuilder.then(literal(s).then(argument("seed", MessageArgumentType.message()).executes(context -> {
                this.structureFeature = structureFeature;
                return run(context);
            })).then(argument("pos", Vec3ArgumentType.vec3()).executes(context -> {
                this.structureFeature = structureFeature;
                this.startPos = new BlockPos(Vec3ArgumentType.getVec3(context, "pos"));
                return run(context);
            })));
        });
        dispatcher.register(argumentBuilder);
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        long seed = getSeed(MessageArgumentType.getMessage(context, "seed").asString()).getAsLong();
        if (startPos == null)
            startPos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos();
        new Thread("structure_finder") {
            @Override
            public void run() {
                ChatHelper.INSTANCE.addClientMessage("Creating fake server for world gen... (This may take a moment)");
                IntegratedServer integratedServer = WorldHelper.INSTANCE.createIntegratedServer(this, seed, GeneratorType.DEFAULT);
                if (integratedServer == null) {
                    ChatHelper.INSTANCE.addClientMessage("Error: Could not create integrated server");
                }
                ServerWorld serverWorld = switch (WorldHelper.INSTANCE.getDimensionID().toString()) {
                    case "minecraft:the_nether" -> integratedServer.getWorld(World.NETHER);
                    case "minecraft:the_end" -> integratedServer.getWorld(World.END);
                    default -> integratedServer.getOverworld();
                };
                if (serverWorld != null) {
                    BlockPos pos = serverWorld.locateStructure(structureFeature, startPos, 100, false);
                    if (pos != null) {
                        ChatHelper.INSTANCE.addClientMessage(StringUtils.capitalize(structureFeature.getName()) + " found at " + pos);
                    } else {
                        ChatHelper.INSTANCE.addClientMessage("Could not find structure");
                    }
                } else {
                    ChatHelper.INSTANCE.addClientMessage("ServerWorld null");
                }
                startPos = null;
                integratedServer.shutdown();
            }
        }.start();
        return 1;
    }

    private OptionalLong getSeed(String string) {
        OptionalLong optionalLong4;
        if (StringUtils.isEmpty(string)) {
            optionalLong4 = OptionalLong.empty();
        } else {
            OptionalLong optionalLong2 = tryParseLong(string);
            if (optionalLong2.isPresent() && optionalLong2.getAsLong() != 0L) {
                optionalLong4 = optionalLong2;
            } else {
                optionalLong4 = OptionalLong.of((long)string.hashCode());
            }
        }

        return optionalLong4;
    }

    private static OptionalLong tryParseLong(String string) {
        try {
            return OptionalLong.of(Long.parseLong(string));
        } catch (NumberFormatException var2) {
            return OptionalLong.empty();
        }
    }
}
