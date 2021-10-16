package me.dustin.jex.helper.world;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.Lifecycle;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.WorldGenerationProgressTracker;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.world.GeneratorType;
import net.minecraft.entity.Entity;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.QueueingWorldGenerationProgressListener;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.UserCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public enum WorldHelper {
    INSTANCE;
    private Queue<Runnable> var10001 = Queues.newConcurrentLinkedQueue();
    private ConcurrentMap<BlockPos, BlockEntity> blockEntities = Maps.newConcurrentMap();

    public Block getBlock(BlockPos pos) {
        if (Wrapper.INSTANCE.getWorld() == null)
            return null;
        return Wrapper.INSTANCE.getWorld().getBlockState(pos).getBlock();
    }

    public BlockState getBlockState(BlockPos pos) {
        if (Wrapper.INSTANCE.getWorld() == null)
            return null;
        return Wrapper.INSTANCE.getWorld().getBlockState(pos);
    }

    public Identifier getDimensionID() {
        return Wrapper.INSTANCE.getWorld().getRegistryKey().getValue();
    }

    public Block getBlockBelowEntity(Entity entity, float offset) {
        if (Wrapper.INSTANCE.getWorld() == null || entity == null)
            return null;

        BlockPos blockPos = new BlockPos(entity.getPos().getX(), entity.getPos().getY() - offset, entity.getPos().getZ());
        return Wrapper.INSTANCE.getWorld().getBlockState(blockPos).getBlock();
    }

    public Block getBlockAboveEntity(Entity entity, float offset) {
        if (Wrapper.INSTANCE.getWorld() == null || entity == null)
            return null;

        BlockPos blockPos = new BlockPos(entity.getPos().getX(), entity.getPos().getY() + entity.getHeight() + offset, entity.getPos().getZ());
        return Wrapper.INSTANCE.getWorld().getBlockState(blockPos).getBlock();
    }

    public Block getBlockAboveEntity(Entity entity) {
        return getBlockAboveEntity(entity, -2.5f);
    }

    public Block getBlockBelowEntity(Entity entity) {
        return getBlockBelowEntity(entity, 0.5f);
    }

    public void removeBlockEntity(BlockPos pos) {
        blockEntities.remove(pos);
    }

    public ConcurrentMap<BlockPos, BlockEntity> getBlockEntityList() {
        return blockEntities;
    }

    @EventListener(events={EventTick.class})
    private void runMethod(EventTick eventTick) {
        if (Wrapper.INSTANCE.getWorld() == null)
            blockEntities.clear();
        else {
            Iterator<BlockEntity> it = blockEntities.values().iterator();
            while (it.hasNext()) {
                BlockEntity blockEntity = it.next();
                if (Wrapper.INSTANCE.getWorld().getBlockEntity(blockEntity.getPos()) == null)
                    removeBlockEntity(blockEntity.getPos());
            }
        }
    }

    public Collection<BlockEntity> getBlockEntities() {
        return blockEntities.values();
    }

    public String getCurrentServerName() {
        boolean isSinglePlayer = Wrapper.INSTANCE.getMinecraft().isInSingleplayer();
        if (isSinglePlayer) {
            String preString = Wrapper.INSTANCE.getMinecraft().getServer().getIconFile().toString().replace(File.separator + "icon.png", "").replace(File.separator, "/");
            String[] list = preString.split("/");
            return list[list.length - 1];
        } else {
            return Wrapper.INSTANCE.getMinecraft().getCurrentServerEntry().address;
        }
    }

    public boolean isOnLiquid(Entity entity) {
        if (entity == null) {
            return false;
        }
        Box boundingBox = entity.getBoundingBox();
        boundingBox = boundingBox.expand(-0.01D, -0.0D, -0.01D).offset(0.0D, -0.01D, 0.0D);
        boolean onLiquid = false;
        int y = (int) boundingBox.minY;
        for (int x = MathHelper.floor(boundingBox.minX); x < MathHelper.floor(boundingBox.maxX + 1.0D); x++) {
            for (int z = MathHelper.floor(boundingBox.minZ); z <
                    MathHelper.floor(boundingBox.maxZ + 1.0D); z++) {
                Block block = getBlock(new BlockPos(x, y, z));
                if (block != Blocks.AIR) {
                    if (!(block instanceof FluidBlock)) {
                        return false;
                    }
                    onLiquid = true;
                }
            }
        }
        return onLiquid;
    }

    public boolean isInLiquid(Entity entity) {
        if (entity == null) {
            return false;
        }
        Box par1AxisAlignedBB = entity.getBoundingBox();
        par1AxisAlignedBB = par1AxisAlignedBB.expand(-0, -0.081D, -0.081D);
        int var4 = MathHelper.floor(par1AxisAlignedBB.minX);
        int var5 = MathHelper.floor(par1AxisAlignedBB.maxX + 1.0D);
        int var6 = MathHelper.floor(par1AxisAlignedBB.minY);
        int var7 = MathHelper.floor(par1AxisAlignedBB.maxY + 0.8D);
        int var8 = MathHelper.floor(par1AxisAlignedBB.minZ);
        int var9 = MathHelper.floor(par1AxisAlignedBB.maxZ + 1.0D);
        if (Wrapper.INSTANCE.getWorld().getChunk(
                new BlockPos(entity.getX(), entity.getY(), entity.getZ())) == null) {
            return false;
        }
        for (int var12 = var4; var12 < var5; var12++) {
            for (int var13 = var6; var13 < var7; var13++) {
                for (int var14 = var8; var14 < var9; var14++) {
                    Block var15 = getBlock(new BlockPos(var12, var13, var14));
                    if ((var15 instanceof FluidBlock)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public IntegratedServer createIntegratedServer(Thread thread, long seed, GeneratorType generatorType) {
        String worldName = "structure_find";
        LevelStorage.Session session2;
        try {
            session2 = Wrapper.INSTANCE.getMinecraft().getLevelStorage().createSession(worldName);
        } catch (IOException var21) {
            JexClient.INSTANCE.getLogger().warn((String)"Failed to read level {} data {}", (Object)worldName, (Object)var21);
            SystemToast.addWorldAccessFailureToast(Wrapper.INSTANCE.getMinecraft(), worldName);
            Wrapper.INSTANCE.getMinecraft().setScreen(null);
            return null;
        }
        DynamicRegistryManager.Impl registryTracker = DynamicRegistryManager.create();
        GeneratorOptions generatorOptions = generatorType.createDefaultOptions(registryTracker, seed, true, false);
        MinecraftClient.IntegratedResourceManager integratedResourceManager2;
        LevelInfo levelInfo = new LevelInfo(worldName, GameMode.CREATIVE, false, Difficulty.HARD, true, new GameRules(), DataPackSettings.SAFE_MODE);

        Function<LevelStorage.Session, DataPackSettings> dataPackSettingsGetter = session -> {
            return DataPackSettings.SAFE_MODE;
        };
        Function4<LevelStorage.Session, DynamicRegistryManager.Impl, ResourceManager, DataPackSettings, SaveProperties> savePropertiesGetter = (session, impl, resourceManager, dataPackSettings) -> {
            return new LevelProperties(levelInfo, generatorOptions, Lifecycle.stable());
        };
        try {
            integratedResourceManager2 = Wrapper.INSTANCE.getMinecraft().createIntegratedResourceManager(registryTracker, dataPackSettingsGetter, savePropertiesGetter, false, session2);
        } catch (Exception var20) {
            JexClient.INSTANCE.getLogger().warn("Failed to load datapacks, can't proceed with server load", (Throwable)var20);
            try {
                session2.close();
            } catch (IOException var16) {
                JexClient.INSTANCE.getLogger().warn("Failed to unlock access to level {} {}", (Object)worldName, (Object)var16);
            }

            return null;
        }
        SaveProperties saveProperties = integratedResourceManager2.getSaveProperties();
        integratedResourceManager2.getServerResourceManager().loadRegistryTags();
        YggdrasilAuthenticationService yggdrasilAuthenticationService = new YggdrasilAuthenticationService(Wrapper.INSTANCE.getMinecraft().getNetworkProxy());
        MinecraftSessionService minecraftSessionService = yggdrasilAuthenticationService.createMinecraftSessionService();
        GameProfileRepository gameProfileRepository = yggdrasilAuthenticationService.createProfileRepository();
        UserCache userCache = new UserCache(gameProfileRepository, new File(Wrapper.INSTANCE.getMinecraft().runDirectory, MinecraftServer.USER_CACHE_FILE.getName()));

        IntegratedServer integratedServer = new IntegratedServer(thread, Wrapper.INSTANCE.getMinecraft(), registryTracker, session2, integratedResourceManager2.getResourcePackManager(), integratedResourceManager2.getServerResourceManager(), saveProperties, minecraftSessionService, gameProfileRepository, userCache, (i) -> {
            WorldGenerationProgressTracker worldGenerationProgressTracker = new WorldGenerationProgressTracker(i);
            return QueueingWorldGenerationProgressListener.create(worldGenerationProgressTracker, var10001::add);
        });
        if (integratedServer.setupServer()) {
            return integratedServer;
        }
        return null;
    }
}
