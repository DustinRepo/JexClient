package me.dustin.jex.helper.world;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.world.GeneratorType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.level.storage.LevelStorage;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentMap;
import java.util.*;

public enum WorldHelper {
    INSTANCE;
    private final ConcurrentMap<BlockPos, BlockEntity> blockEntities = Maps.newConcurrentMap();
    public static final Box SINGLE_BOX = new Box(0, 0, 0, 1, 1, 1);

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

    public boolean isWaterlogged(BlockPos pos) {
        BlockState blockState = getBlockState(pos);
        if (blockState == null)
            return false;
        return blockState.getFluidState() != Fluids.EMPTY.getDefaultState();
    }

    public boolean canUseOnPos(BlockPos pos) {
        return WorldHelper.INSTANCE.getBlockState(pos).onUse(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), Hand.MAIN_HAND, new BlockHitResult(Vec3d.ZERO, Direction.UP, BlockPos.ORIGIN, false)) != ActionResult.PASS;
    }

    public boolean isCrop(BlockPos blockPos, boolean checkAge) {
        Block block = WorldHelper.INSTANCE.getBlock(blockPos);
        if (block instanceof CropBlock cropBlock) {
            int age = Wrapper.INSTANCE.getWorld().getBlockState(blockPos).get(cropBlock.getAgeProperty());
            if (!checkAge || age == cropBlock.getMaxAge()) {
                return true;
            }
        } else if (block == Blocks.MELON || block == Blocks.PUMPKIN) {
            return true;
        } else if (block == Blocks.SUGAR_CANE) {
            Block belowBlock = WorldHelper.INSTANCE.getBlock(blockPos.down());
            if (belowBlock == Blocks.SUGAR_CANE)
                return true;
        } else if (block == Blocks.BAMBOO) {
            Block belowBlock = WorldHelper.INSTANCE.getBlock(blockPos.down());
            if (belowBlock == Blocks.BAMBOO)
                return true;
        }
        return false;
    }

    //super fucking scuffed
    public Direction chestMergeDirection(ChestBlockEntity chestBlockEntity) {
        BlockState blockState = getBlockState(chestBlockEntity.getPos());
        ChestBlock chestBlock = (ChestBlock) getBlock(chestBlockEntity.getPos());
        Box chestBox = chestBlock.getOutlineShape(blockState, Wrapper.INSTANCE.getWorld(), chestBlockEntity.getPos(), ShapeContext.absent()).getBoundingBox();
        if (chestBox.minZ == 0)
            return Direction.NORTH;
        if (chestBox.maxZ == 1)
            return Direction.SOUTH;
        if (chestBox.maxX == 1)
            return Direction.EAST;
        if (chestBox.minX == 0)
            return Direction.WEST;
        return Direction.UP;
    }

    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
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

    public ArrayList<BlockPos> getBlocksInBox(Box box) {
        ArrayList<BlockPos> blocks = new ArrayList<>();
        for (int x = (int)box.minX; x <= box.maxX; x++) {
            for (int y = (int)box.minY; y <= box.maxY; y++) {
                for (int z = (int)box.minZ; z <= box.maxZ; z++) {
                    blocks.add(new BlockPos(x, y, z));
                }
            }
        }
        return blocks;
    }

    public Vec3d sideOfBlock(BlockPos pos, Direction direction) {
        switch (direction) {
            case NORTH -> Vec3d.ofCenter(pos).add(0, 0, -0.5);
            case SOUTH -> Vec3d.ofCenter(pos).add(0, 0, 0.5);
            case EAST -> Vec3d.ofCenter(pos).add(0.5, 0, 0);
            case WEST -> Vec3d.ofCenter(pos).add(-0.5, 0, 0);
            case UP -> Vec3d.ofCenter(pos).add(0, 0.5, 0);
            case DOWN -> Vec3d.ofCenter(pos).add(0, -0.5, 0);
        }
        return Vec3d.ofCenter(pos);
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

    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getWorld() == null)
            blockEntities.clear();
        else {
            for (BlockEntity blockEntity : blockEntities.values()) {
                if (Wrapper.INSTANCE.getWorld().getBlockEntity(blockEntity.getPos()) == null)
                    removeBlockEntity(blockEntity.getPos());
            }
        }
    }, new TickFilter(EventTick.Mode.PRE));

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
            for (int z = MathHelper.floor(boundingBox.minZ); z < MathHelper.floor(boundingBox.maxZ + 1.0D); z++) {
                BlockPos blockPos = new BlockPos(x, y, z);
                Block block = getBlock(new BlockPos(x, y, z));
                if (block != Blocks.AIR) {
                    if (!isWaterlogged(blockPos))
                        return false;
                    FluidState fluidState = getFluidState(blockPos);
                    Box blockBB = fluidState.getShape(Wrapper.INSTANCE.getWorld(), blockPos).getBoundingBox().offset(blockPos);
                    if (boundingBox.minX < blockBB.maxX &&
                            boundingBox.maxX > blockBB.minX &&
                            boundingBox.minY < blockBB.maxY &&
                            boundingBox.maxY > blockBB.minY &&
                            boundingBox.minZ < blockBB.maxZ &&
                            boundingBox.maxZ > blockBB.minZ) {
                        onLiquid = true;
                    }
                }
            }
        }
        return onLiquid;
    }

    public boolean isSlimeChunk(long seed, int chunkX, int chunkZ) {
        Random r = new Random(seed + (int)(chunkX * chunkX * 0x4c1906) + (int)(chunkX * 0x5ac0db) + (int)(chunkZ * chunkZ) * 0x4307a7L + (int)(chunkZ * 0x5f24f) ^ 0x3AD8025FL);
        return (r.nextInt(10) == 0);
    }

    public boolean isTouchingLiquidBlockSpace(Entity entity) {
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
                    if (!isWaterlogged(new BlockPos(x, y, z))) {
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
        Box boundingBox = entity.getBoundingBox();
        boundingBox = boundingBox.expand(-0, -0.081D, -0.081D);
        int var4 = MathHelper.floor(boundingBox.minX);
        int var5 = MathHelper.floor(boundingBox.maxX + 1.0D);
        int var6 = MathHelper.floor(boundingBox.minY);
        int var7 = MathHelper.floor(boundingBox.maxY + 0.8D);
        int var8 = MathHelper.floor(boundingBox.minZ);
        int var9 = MathHelper.floor(boundingBox.maxZ + 1.0D);
        if (Wrapper.INSTANCE.getWorld().getChunk(
                new BlockPos(entity.getX(), entity.getY(), entity.getZ())) == null) {
            return false;
        }
        for (int var12 = var4; var12 < var5; var12++) {
            for (int var13 = var6; var13 < var7; var13++) {
                for (int var14 = var8; var14 < var9; var14++) {
                    BlockPos blockPos = new BlockPos(var12, var13, var14);
                    Block var15 = getBlock(blockPos);
                    if ((var15 instanceof FluidBlock)) {
                        FluidState fluidState = getFluidState(blockPos);
                        Box blockBB = fluidState.getShape(Wrapper.INSTANCE.getWorld(), blockPos).getBoundingBox().offset(blockPos);
                        if (boundingBox.minX < blockBB.maxX &&
                                boundingBox.maxX > blockBB.minX &&
                                boundingBox.minY < blockBB.maxY &&
                                boundingBox.maxY > blockBB.minY &&
                                boundingBox.minZ < blockBB.maxZ &&
                                boundingBox.maxZ > blockBB.minZ) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public float getBlockBreakingSpeed(BlockState block, ItemStack stack) {

        float f = stack.getMiningSpeedMultiplier(block);
        if (f > 1.0F) {
            int i = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
            if (i > 0 && !stack.isEmpty()) {
                f += (float) (i * i + 1);
            }
        }

        if (StatusEffectUtil.hasHaste(Wrapper.INSTANCE.getLocalPlayer())) {
            f *= 1.0F + (float) (StatusEffectUtil.getHasteAmplifier(Wrapper.INSTANCE.getLocalPlayer()) + 1) * 0.2F;
        }

        if (Wrapper.INSTANCE.getLocalPlayer().hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            float k;
            switch (Wrapper.INSTANCE.getLocalPlayer().getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0:
                    k = 0.3F;
                    break;
                case 1:
                    k = 0.09F;
                    break;
                case 2:
                    k = 0.0027F;
                    break;
                case 3:
                default:
                    k = 8.1E-4F;
            }

            f *= k;
        }

        if (Wrapper.INSTANCE.getLocalPlayer().isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(Wrapper.INSTANCE.getLocalPlayer())) {
            f /= 5.0F;
        }

        if (!Wrapper.INSTANCE.getLocalPlayer().isOnGround()) {
            f /= 5.0F;
        }

        return f;
    }

    public boolean isBreakable(Block block) {
        return block != Blocks.BEDROCK && block != Blocks.BARRIER && block != Blocks.COMMAND_BLOCK && block != Blocks.NETHER_PORTAL && block != Blocks.END_PORTAL && block != Blocks.END_GATEWAY;
    }

    public boolean canMobSpawnOntop(BlockPos blockPos) {
        BlockState blockState = WorldHelper.INSTANCE.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (!blockState.allowsSpawning(Wrapper.INSTANCE.getWorld(), blockPos, EntityType.ZOMBIE))
            return false;
        if (block instanceof GlassBlock || block instanceof StainedGlassBlock || block instanceof TintedGlassBlock)
            return false;
        return block != Blocks.GLOWSTONE && block != Blocks.SEA_LANTERN;
    }

    public boolean canMobSpawnInside(BlockState blockState) {
        Block block = blockState.getBlock();
        if (blockState.getMaterial().blocksMovement() || !block.canMobSpawnInside())
            return false;
        if (block instanceof AbstractButtonBlock)
            return false;
        if (block instanceof RedstoneWireBlock)
            return false;
        if (block instanceof ComparatorBlock)
            return false;
        if (block instanceof RepeaterBlock)
            return false;
        if (block instanceof RedstoneTorchBlock)
            return false;
        if (block instanceof PressurePlateBlock)
            return false;
        if (block instanceof LeverBlock)
            return false;
        if (block instanceof TripwireHookBlock)
            return false;
        if (block instanceof CarpetBlock)
            return false;
        if (block instanceof AbstractRailBlock)
            return false;
        if (block instanceof FlowerPotBlock)
            return false;
        if (block instanceof SkullBlock)
            return false;
        return !(block instanceof CandleBlock);
    }

    public ArrayList<BlockPos> cubeSphere(Vec3d pos, double r, int lats, int longs) {
        ArrayList<BlockPos> positions = new ArrayList<>();
        int i, j;
        for (i = 0; i <= lats; i++)
        {
            double lat0 = Math.PI * (-0.5 + (double)(i - 1) / lats);
            double z0 = Math.sin(lat0) * r;
            double zr0 = Math.cos(lat0) * r;

            for (j = 0; j <= longs; j++)
            {
                double lng = 2 * Math.PI * (double)(j - 1) / longs;
                double x = Math.cos(lng);
                double y = Math.sin(lng);
                BlockPos blockPos = new BlockPos(pos.add(x * zr0, y * zr0, z0));
                if (!positions.contains(blockPos)) {
                    positions.add(blockPos);
                }
            }
        }
        return positions;
    }

    public IntegratedServer createIntegratedServer(Thread thread, long seed, GeneratorType generatorType) {
        //TODO: recreate this after 1.18.2
        /*String worldName = "jex_finder" + seed;
        LevelStorage.Session session2;
        try {
            session2 = Wrapper.INSTANCE.getMinecraft().getLevelStorage().createSession(worldName);
        } catch (IOException var21) {
            JexClient.INSTANCE.getLogger().warn("Failed to read level {} data {}", worldName, var21);
            SystemToast.addWorldAccessFailureToast(Wrapper.INSTANCE.getMinecraft(), worldName);
            Wrapper.INSTANCE.getMinecraft().setScreen(null);
            return null;
        }
        DynamicRegistryManager.MutableImpl registryTracker = DynamicRegistryManager.createAndLoad().create();
        GeneratorOptions generatorOptions = generatorType.createDefaultOptions(registryTracker, seed, true, false);
        MinecraftClient.IntegratedResourceManager integratedResourceManager2;
        LevelInfo levelInfo = new LevelInfo(worldName, GameMode.CREATIVE, false, Difficulty.HARD, true, new GameRules(), DataPackSettings.SAFE_MODE);

        Function<LevelStorage.Session, DataPackSettings> dataPackSettingsGetter = session -> DataPackSettings.SAFE_MODE;
        Function4<LevelStorage.Session, DynamicRegistryManager.Impl, ResourceManager, DataPackSettings, SaveProperties> savePropertiesGetter = (session, impl, resourceManager, dataPackSettings) -> new LevelProperties(levelInfo, generatorOptions, Lifecycle.stable());
        try {
            integratedResourceManager2 = Wrapper.INSTANCE.getMinecraft().createIntegratedResourceManager(registryTracker, dataPackSettingsGetter, savePropertiesGetter, true, session2);
        } catch (Exception var20) {
            JexClient.INSTANCE.getLogger().warn("Failed to load datapacks, can't proceed with server load", var20);
            try {
                session2.close();
            } catch (IOException var16) {
                JexClient.INSTANCE.getLogger().warn("Failed to unlock access to level {} {}", worldName, var16);
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
        //cleanupIntegratedServer();*/
        return null;
    }

    public void cleanupIntegratedServer() {
        try {
            LevelStorage.Session session = Wrapper.INSTANCE.getMinecraft().getLevelStorage().createSession("jex_finder");

            try {
                session.deleteSessionLock();
            } catch (Throwable var7) {
                if (session != null) {
                    try {
                        session.close();
                    } catch (Throwable var6) {
                        var7.addSuppressed(var6);
                    }
                }

                throw var7;
            }

            session.close();
        } catch (IOException var8) {
            SystemToast.addWorldDeleteFailureToast(Wrapper.INSTANCE.getMinecraft(), "jex_finder");
            JexClient.INSTANCE.getLogger().error("Failed to delete world {}", "jex_finder", var8);
        }
    }

    public float calcExplosionDamage(float power, PlayerEntity playerEntity, BlockPos explosionPos) {
        Vec3d vec3d = ClientMathHelper.INSTANCE.getVec(explosionPos);
        float j = power * 2.0F;
        double h = Math.sqrt(playerEntity.squaredDistanceTo(vec3d)) / (double) j;
        double v = 1 - h * getExposure(vec3d, playerEntity);

        return (float) ((int) ((v * v + v) / 2.0D * 7.0D * (double) j + 1.0D));
    }

    public static float getExposure(Vec3d source, Entity entity) {
        Box box = entity.getBoundingBox();
        double d = 1.0D / ((box.maxX - box.minX) * 2.0D + 1.0D);
        double e = 1.0D / ((box.maxY - box.minY) * 2.0D + 1.0D);
        double f = 1.0D / ((box.maxZ - box.minZ) * 2.0D + 1.0D);
        double g = (1.0D - Math.floor(1.0D / d) * d) / 2.0D;
        double h = (1.0D - Math.floor(1.0D / f) * f) / 2.0D;
        if (!(d < 0.0D) && !(e < 0.0D) && !(f < 0.0D)) {
            int i = 0;
            int j = 0;

            for(double k = 0.0D; k <= 1.0D; k += d) {
                for(double l = 0.0D; l <= 1.0D; l += e) {
                    for(double m = 0.0D; m <= 1.0D; m += f) {
                        double n = MathHelper.lerp(k, box.minX, box.maxX);
                        double o = MathHelper.lerp(l, box.minY, box.maxY);
                        double p = MathHelper.lerp(m, box.minZ, box.maxZ);
                        Vec3d vec3d = new Vec3d(n + g, o, p + h);
                        if (Wrapper.INSTANCE.getWorld().raycast(new RaycastContext(vec3d, source, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity)).getType() == HitResult.Type.MISS) {
                            ++i;
                        }

                        ++j;
                    }
                }
            }

            return (float)i / (float)j;
        } else {
            return 0.0F;
        }
    }
}
