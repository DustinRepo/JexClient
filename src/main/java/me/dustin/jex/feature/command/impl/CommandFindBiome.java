package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Pair;
import me.dustin.jex.JexClient;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.command.core.arguments.MessageArgumentType;
import me.dustin.jex.feature.command.core.arguments.RegistryPredicateArgumentType;
import me.dustin.jex.feature.command.core.arguments.Vec3ArgumentType;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.world.SeedHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.world.WorldHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.world.GeneratorType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

@Cmd(name = "findbiome", description = "Find Biomes with a given seed, like the /locatebiome command", syntax = {".find <biome> <seed>", ".find <biome> <searchPos> <seed>"})
public class CommandFindBiome /*extends Command*/ {
    /*private BlockPos startPos;

    SuggestionProvider<FabricClientCommandSource> ALL_BIOMES = SuggestionProviders.register(new Identifier("assets/jex", "available_biomes"), (context, builder) -> CommandSource.suggestIdentifiers((context.getSource()).getRegistryManager().get(Registry.BIOME_KEY).getIds(), builder));

    @Override
    public void registerCommand() {
        dispatcher.register(literal(this.name).then(argument("biome", RegistryPredicateArgumentType.registryPredicate(Registry.BIOME_KEY)).suggests(ALL_BIOMES).then(argument("pos", Vec3ArgumentType.vec3()).then(argument("seed", MessageArgumentType.message()).executes(context -> {
            this.startPos = new BlockPos(Vec3ArgumentType.getVec3(context, "pos"));
            return run(context);
        }))).then(argument("seed", MessageArgumentType.message()).executes(this))));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        long seed = SeedHelper.INSTANCE.getSeed(MessageArgumentType.getMessage(context, "seed").asString()).getAsLong();
        RegistryPredicateArgumentType.RegistryPredicate<Biome> biomeRegistryPredicate = RegistryPredicateArgumentType.getBiomePredicate(context, "biome");
        if (startPos == null)
            startPos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos();
        new Thread("biome_finder") {
            @Override
            public void run() {
                ChatHelper.INSTANCE.addClientMessage("Creating fake server for world gen... (This may take a moment)");
                IntegratedServer integratedServer = WorldHelper.INSTANCE.createIntegratedServer(this, seed, GeneratorType.DEFAULT);
                if (integratedServer == null) {
                    ChatHelper.INSTANCE.addClientMessage("Error: Could not create integrated server");
                    return;
                }

                if (biomeRegistryPredicate == null) {
                    ChatHelper.INSTANCE.addClientMessage("Error: Biome request returned null");
                }

                ServerWorld serverWorld = switch (WorldHelper.INSTANCE.getDimensionID().toString()) {
                    case "minecraft:the_nether" -> integratedServer.getWorld(World.NETHER);
                    case "minecraft:the_end" -> integratedServer.getWorld(World.END);
                    default -> integratedServer.getOverworld();
                };
                if (serverWorld != null) {
                    Pair<BlockPos, RegistryEntry<Biome>> pair = serverWorld.locateBiome(biomeRegistryPredicate, startPos, 6400, 8);
                    if (pair != null) {
                        String posString = "BlockPos: X: \247b" + pair.getFirst().getX() + (pair.getFirst().getY() == 0 ? "" : " \2477Y: \247b" + pair.getFirst().getY()) + " \2477Z: \247b" + pair.getFirst().getZ();
                        ChatHelper.INSTANCE.addClientMessage(pair.getSecond().value().toString() + " found at " + posString);
                    } else {
                        ChatHelper.INSTANCE.addClientMessage("Could not find biome");
                    }
                } else {
                    ChatHelper.INSTANCE.addClientMessage("ServerWorld null");
                }
                startPos = null;
                JexClient.INSTANCE.getLogger().info("Shutting down integrated server");
                integratedServer.shutdown();
                JexClient.INSTANCE.getLogger().info("Cleaning up integrated server");
                WorldHelper.INSTANCE.cleanupIntegratedServer();
            }
        }.start();
        return 1;
    }*/
}
