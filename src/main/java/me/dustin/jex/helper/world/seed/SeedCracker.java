package me.dustin.jex.helper.world.seed;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.command.impl.CommandDungeonSeedCracker;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.helper.world.seed.kaptainwutax.magic.PopulationReversal;
import me.dustin.jex.helper.world.seed.kaptainwutax.magic.RandomSeed;
import me.dustin.jex.helper.world.seed.kaptainwutax.util.LCG;
import me.dustin.jex.helper.world.seed.kaptainwutax.util.Rand;
import me.dustin.jex.helper.world.seed.randomreversor.ReverserDevice;
import me.dustin.jex.helper.world.seed.randomreversor.call.FilteredSkip;
import me.dustin.jex.helper.world.seed.randomreversor.call.NextInt;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public enum SeedCracker {
    INSTANCE;

    public void crackSeed(BlockPos spawnerPos, ArrayList<Integer> blocksList, ArrayList<Long> compare) {
        new Thread("seed cracker") {
            @Override
            public void run() {
                int offsetX = spawnerPos.getX() & 0xF;
                int offsetZ = spawnerPos.getZ() & 0xF;
                ReverserDevice device = new ReverserDevice();
                device.addCall(NextInt.withValue(16, offsetX));
                device.addCall(NextInt.withValue(16, offsetZ));
                device.addCall(NextInt.withValue(256, spawnerPos.getY()));
                device.addCall(NextInt.consume(2, 2));
                for (Integer integer : blocksList) {
                    if (integer == 0) {
                        device.addCall(NextInt.withValue(4, 0));
                    }
                    else if (integer == 1) {
                        device.addCall(FilteredSkip.filter(r -> r.nextInt(4) != 0));
                    }
                    else {
                        device.addCall(NextInt.consume(4, 1));
                    }
                }

                Set<Long> decoratorSeeds = (device.streamSeeds().sequential()).limit(1L).collect(Collectors.toSet());
                decoratorSeeds.forEach(s -> ChatHelper.INSTANCE.addClientMessage("Found Dungeon seed: " + decoratorSeeds));

                ChatHelper.INSTANCE.addClientMessage("Finished dungeon search and looking for world seeds.");

                for (Iterator<Long> iterator = decoratorSeeds.iterator(); iterator.hasNext(); ) {
                    long decoratorSeed = (Long) iterator.next();
                    LCG failedDungeon = Rand.JAVA_LCG.combine(-5L);

                    for (int i = 0; i < 8; i++) {
                        for (Long structureSeed : PopulationReversal.getWorldSeeds((decoratorSeed ^ Rand.JAVA_LCG.multiplier) - 30002L, spawnerPos.getX() & 0xFFFFFFF0, spawnerPos.getZ() & 0xFFFFFFF0)) {

                            for (long upperBits = 0L; upperBits < 65536L; upperBits++) {
                                long worldSeed = upperBits << 48 | structureSeed;
                                if (RandomSeed.isRandomSeed(worldSeed)) {
                                    if (!compare.isEmpty()) {
                                        if (compare.contains(worldSeed))
                                            ChatHelper.INSTANCE.addClientMessage("Possible seed: \247b" + worldSeed);
                                    } else {
                                        CommandDungeonSeedCracker.possibleSeeds.add(worldSeed);
                                        ChatHelper.INSTANCE.addClientMessage("Possible seed: \247b" + worldSeed);
                                    }
                                }
                            }
                        }
                        decoratorSeed = failedDungeon.nextSeed(decoratorSeed);
                    }
                }
                ChatHelper.INSTANCE.addClientMessage("Stopping crack attempt");
            }
        }.start();
    }
}
