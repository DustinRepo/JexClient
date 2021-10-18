package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.helper.world.seed.SeedCracker;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

@Cmd(name = "dungeonseedcrack", description = "Get a list of possible seeds using dungeons. If the first attempt doesn't work, try again", syntax = ".dungeonseedcrack <refine>(Optional, only for narrowing down seeds")
public class CommandDungeonSeedCracker extends Command {

    private ArrayList<BlockPos> attemptedSpots = new ArrayList<>();
    public static ArrayList<Long> possibleSeeds = new ArrayList<>();

    @Override
    public void registerCommand() {
        dispatcher.register(literal(this.name).executes(context -> crackSeed(false)).then(literal("refine").executes(context -> crackSeed(true))));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        return 0;
    }

    private int crackSeed(boolean useOldSeeds) {
        if (!useOldSeeds)
            possibleSeeds.clear();
        else {
            ChatHelper.INSTANCE.addClientMessage("Refining search");
        }
        MobSpawnerBlockEntity mobSpawnerBlockEntity = getClosest();
        if (mobSpawnerBlockEntity == null) {
            ChatHelper.INSTANCE.addClientMessage("No dungeons in sight. Aborting!");
            return 0;
        }
        BlockPos spawnerPos = mobSpawnerBlockEntity.getPos();
        ArrayList<Integer> blocksList = new ArrayList<>();
        for (int x = -5; x < 5; x++) {
            for (int z = -5; z < 5; z++) {
                BlockPos checkPos = spawnerPos.add(x, -1, z);
                if (isCobble(checkPos)) {
                    blocksList.add(0);
                } else if (isMossy(checkPos)) {
                    blocksList.add(1);
                }
            }
        }
        attemptedSpots.add(spawnerPos);
        if (blocksList.size() > 10) {
            ChatHelper.INSTANCE.addClientMessage("Attempting to crack seed from dungeon at BlockPos: " + spawnerPos + " (This can take a while)");
            SeedCracker.INSTANCE.crackSeed(spawnerPos, blocksList, new ArrayList<>(possibleSeeds));
        } else {
            ChatHelper.INSTANCE.addClientMessage("Dungeon doesn't have enough cobblestone/mossy cobblestone. Please choose another");
        }
        return 1;
    }

    private boolean isCobble(BlockPos blockPos) {
        Block block = WorldHelper.INSTANCE.getBlock(blockPos);
        return block == Blocks.COBBLESTONE;
    }

    private boolean isMossy(BlockPos blockPos) {
        Block block = WorldHelper.INSTANCE.getBlock(blockPos);
        return block == Blocks.MOSSY_COBBLESTONE;
    }

    public MobSpawnerBlockEntity getClosest() {
        MobSpawnerBlockEntity spawner = null;
        float distance = 9999;
        for (BlockEntity blockEntity : WorldHelper.INSTANCE.getBlockEntities()) {
            if (blockEntity instanceof MobSpawnerBlockEntity mobSpawnerBlockEntity && !attemptedSpots.contains(mobSpawnerBlockEntity.getPos())) {
                BlockPos spawnerPos = mobSpawnerBlockEntity.getPos();
                float distanceFrom = ClientMathHelper.INSTANCE.getDistance(ClientMathHelper.INSTANCE.getVec(spawnerPos), Wrapper.INSTANCE.getLocalPlayer().getPos());
                if (spawner == null || distanceFrom < distance){
                    spawner = mobSpawnerBlockEntity;
                    distance = distanceFrom;
                }
            }
        }
        return spawner;
    }
}
