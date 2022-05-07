package me.dustin.jex.helper.world;

import com.google.common.collect.Maps;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.ConnectedServerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.TintedGlassBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import java.io.File;
import java.util.concurrent.ConcurrentMap;
import java.util.*;

public enum WorldHelper {
    INSTANCE;
    private final ConcurrentMap<BlockPos, BlockEntity> blockEntities = Maps.newConcurrentMap();
    public static final AABB SINGLE_BOX = new AABB(0, 0, 0, 1, 1, 1);

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
        return blockState.getFluidState() != Fluids.EMPTY.defaultFluidState();
    }

    public boolean canUseOnPos(BlockPos pos) {
        return WorldHelper.INSTANCE.getBlockState(pos).use(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.ZERO, Direction.UP, BlockPos.ZERO, false)) != InteractionResult.PASS;
    }

    public boolean isCrop(BlockPos blockPos, boolean checkAge) {
        Block block = WorldHelper.INSTANCE.getBlock(blockPos);
        if (block instanceof CropBlock cropBlock) {
            int age = Wrapper.INSTANCE.getWorld().getBlockState(blockPos).getValue(cropBlock.getAgeProperty());
            if (!checkAge || age == cropBlock.getMaxAge()) {
                return true;
            }
        } else if (block == Blocks.MELON || block == Blocks.PUMPKIN) {
            return true;
        } else if (block == Blocks.SUGAR_CANE) {
            Block belowBlock = WorldHelper.INSTANCE.getBlock(blockPos.below());
            if (belowBlock == Blocks.SUGAR_CANE)
                return true;
        } else if (block == Blocks.BAMBOO) {
            Block belowBlock = WorldHelper.INSTANCE.getBlock(blockPos.below());
            if (belowBlock == Blocks.BAMBOO)
                return true;
        }
        return false;
    }

    //super fucking scuffed
    public Direction chestMergeDirection(ChestBlockEntity chestBlockEntity) {
        BlockState blockState = getBlockState(chestBlockEntity.getBlockPos());
        ChestBlock chestBlock = (ChestBlock) getBlock(chestBlockEntity.getBlockPos());
        AABB chestBox = chestBlock.getShape(blockState, Wrapper.INSTANCE.getWorld(), chestBlockEntity.getBlockPos(), CollisionContext.empty()).bounds();
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

    public ResourceLocation getDimensionID() {
        return Wrapper.INSTANCE.getWorld().dimension().location();
    }

    public Block getBlockBelowEntity(Entity entity, float offset) {
        if (Wrapper.INSTANCE.getWorld() == null || entity == null)
            return null;

        BlockPos blockPos = new BlockPos(entity.position().x(), entity.position().y() - offset, entity.position().z());
        return Wrapper.INSTANCE.getWorld().getBlockState(blockPos).getBlock();
    }

    public Block getBlockAboveEntity(Entity entity, float offset) {
        if (Wrapper.INSTANCE.getWorld() == null || entity == null)
            return null;

        BlockPos blockPos = new BlockPos(entity.position().x(), entity.position().y() + entity.getBbHeight() + offset, entity.position().z());
        return Wrapper.INSTANCE.getWorld().getBlockState(blockPos).getBlock();
    }

    public ArrayList<BlockPos> getBlocksInBox(AABB box) {
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

    public Vec3 sideOfBlock(BlockPos pos, Direction direction) {
        switch (direction) {
            case NORTH -> Vec3.atCenterOf(pos).add(0, 0, -0.5);
            case SOUTH -> Vec3.atCenterOf(pos).add(0, 0, 0.5);
            case EAST -> Vec3.atCenterOf(pos).add(0.5, 0, 0);
            case WEST -> Vec3.atCenterOf(pos).add(-0.5, 0, 0);
            case UP -> Vec3.atCenterOf(pos).add(0, 0.5, 0);
            case DOWN -> Vec3.atCenterOf(pos).add(0, -0.5, 0);
        }
        return Vec3.atCenterOf(pos);
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
                if (Wrapper.INSTANCE.getWorld().getBlockEntity(blockEntity.getBlockPos()) == null)
                    removeBlockEntity(blockEntity.getBlockPos());
            }
        }
    }, new TickFilter(EventTick.Mode.PRE));

    public Collection<BlockEntity> getBlockEntities() {
        return blockEntities.values();
    }

    public String getCurrentServerName() {
        try {
            boolean isSinglePlayer = Wrapper.INSTANCE.getMinecraft().isLocalServer();
            if (isSinglePlayer && Wrapper.INSTANCE.getMinecraft().getSingleplayerServer() != null) {
                String preString = Wrapper.INSTANCE.getMinecraft().getSingleplayerServer().getWorldScreenshotFile().toString().replace(File.separator + "icon.png", "").replace(File.separator, "/");
                String[] list = preString.split("/");
                return list[list.length - 1];
            } else {
                if (ConnectedServerHelper.INSTANCE.getServerAddress() == null)
                    return "";
                return ConnectedServerHelper.INSTANCE.getServerAddress().getHost();
            }
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public boolean isOnLiquid(Entity entity) {
        if (entity == null) {
            return false;
        }
        AABB boundingBox = entity.getBoundingBox();
        boundingBox = boundingBox.inflate(-0.01D, -0.0D, -0.01D).move(0.0D, -0.01D, 0.0D);
        boolean onLiquid = false;
        int y = (int) boundingBox.minY;
        for (int x = Mth.floor(boundingBox.minX); x < Mth.floor(boundingBox.maxX + 1.0D); x++) {
            for (int z = Mth.floor(boundingBox.minZ); z < Mth.floor(boundingBox.maxZ + 1.0D); z++) {
                BlockPos blockPos = new BlockPos(x, y, z);
                Block block = getBlock(new BlockPos(x, y, z));
                if (block != Blocks.AIR) {
                    if (!isWaterlogged(blockPos))
                        return false;
                    FluidState fluidState = getFluidState(blockPos);
                    AABB blockBB = fluidState.getShape(Wrapper.INSTANCE.getWorld(), blockPos).bounds().move(blockPos);
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
        AABB boundingBox = entity.getBoundingBox();
        boundingBox = boundingBox.inflate(-0.01D, -0.0D, -0.01D).move(0.0D, -0.01D, 0.0D);
        boolean onLiquid = false;
        int y = (int) boundingBox.minY;
        for (int x = Mth.floor(boundingBox.minX); x < Mth.floor(boundingBox.maxX + 1.0D); x++) {
            for (int z = Mth.floor(boundingBox.minZ); z <
                    Mth.floor(boundingBox.maxZ + 1.0D); z++) {
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
        AABB boundingBox = entity.getBoundingBox();
        boundingBox = boundingBox.inflate(-0, -0.081D, -0.081D);
        int var4 = Mth.floor(boundingBox.minX);
        int var5 = Mth.floor(boundingBox.maxX + 1.0D);
        int var6 = Mth.floor(boundingBox.minY);
        int var7 = Mth.floor(boundingBox.maxY + 0.8D);
        int var8 = Mth.floor(boundingBox.minZ);
        int var9 = Mth.floor(boundingBox.maxZ + 1.0D);
        if (Wrapper.INSTANCE.getWorld().getChunk(
                new BlockPos(entity.getX(), entity.getY(), entity.getZ())) == null) {
            return false;
        }
        for (int var12 = var4; var12 < var5; var12++) {
            for (int var13 = var6; var13 < var7; var13++) {
                for (int var14 = var8; var14 < var9; var14++) {
                    BlockPos blockPos = new BlockPos(var12, var13, var14);
                    Block var15 = getBlock(blockPos);
                    if ((var15 instanceof LiquidBlock)) {
                        FluidState fluidState = getFluidState(blockPos);
                        AABB blockBB = fluidState.getShape(Wrapper.INSTANCE.getWorld(), blockPos).bounds().move(blockPos);
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

        float f = stack.getDestroySpeed(block);
        if (f > 1.0F) {
            int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, stack);
            if (i > 0 && !stack.isEmpty()) {
                f += (float) (i * i + 1);
            }
        }

        if (MobEffectUtil.hasDigSpeed(Wrapper.INSTANCE.getLocalPlayer())) {
            f *= 1.0F + (float) (MobEffectUtil.getDigSpeedAmplification(Wrapper.INSTANCE.getLocalPlayer()) + 1) * 0.2F;
        }

        if (Wrapper.INSTANCE.getLocalPlayer().hasEffect(MobEffects.DIG_SLOWDOWN)) {
            float k;
            switch (Wrapper.INSTANCE.getLocalPlayer().getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
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

        if (Wrapper.INSTANCE.getLocalPlayer().isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(Wrapper.INSTANCE.getLocalPlayer())) {
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
        if (!blockState.isValidSpawn(Wrapper.INSTANCE.getWorld(), blockPos, EntityType.ZOMBIE))
            return false;
        if (block instanceof GlassBlock || block instanceof StainedGlassBlock || block instanceof TintedGlassBlock)
            return false;
        return block != Blocks.GLOWSTONE && block != Blocks.SEA_LANTERN;
    }

    public boolean canMobSpawnInside(BlockState blockState) {
        Block block = blockState.getBlock();
        if (blockState.getMaterial().blocksMotion() || !block.isPossibleToRespawnInThis())
            return false;
        if (block instanceof ButtonBlock)
            return false;
        if (block instanceof RedStoneWireBlock)
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
        if (block instanceof TripWireHookBlock)
            return false;
        if (block instanceof CarpetBlock)
            return false;
        if (block instanceof BaseRailBlock)
            return false;
        if (block instanceof FlowerPotBlock)
            return false;
        if (block instanceof SkullBlock)
            return false;
        return !(block instanceof CandleBlock);
    }

    public ArrayList<BlockPos> cubeSphere(Vec3 pos, double r, int lats, int longs) {
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

    public float calcExplosionDamage(float power, Player playerEntity, BlockPos explosionPos) {
        Vec3 vec3d = ClientMathHelper.INSTANCE.getVec(explosionPos);
        float j = power * 2.0F;
        double h = Math.sqrt(playerEntity.distanceToSqr(vec3d)) / (double) j;
        double v = 1 - h * getExposure(vec3d, playerEntity);

        return (float) ((int) ((v * v + v) / 2.0D * 7.0D * (double) j + 1.0D));
    }

    public static float getExposure(Vec3 source, Entity entity) {
        AABB box = entity.getBoundingBox();
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
                        double n = Mth.lerp(k, box.minX, box.maxX);
                        double o = Mth.lerp(l, box.minY, box.maxY);
                        double p = Mth.lerp(m, box.minZ, box.maxZ);
                        Vec3 vec3d = new Vec3(n + g, o, p + h);
                        if (Wrapper.INSTANCE.getWorld().clip(new ClipContext(vec3d, source, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS) {
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
