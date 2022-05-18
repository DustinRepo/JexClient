package me.dustin.jex.helper.world.wurstpathfinder;

import java.util.*;

import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.fly.Fly;
import me.dustin.jex.feature.mod.impl.movement.Spider;
import me.dustin.jex.feature.mod.impl.player.Jesus;
import me.dustin.jex.feature.mod.impl.player.NoFall;
import me.dustin.jex.feature.mod.impl.player.NoPush;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CobwebBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.Material;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.SlimeBlock;
import net.minecraft.block.SoulSandBlock;
import net.minecraft.block.TripwireBlock;
import net.minecraft.block.VineBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;

public class PathFinder
{
	private final boolean invulnerable = Wrapper.INSTANCE.getPlayer().getAbilities().creativeMode;
	private final boolean creativeFlying = Wrapper.INSTANCE.getPlayer().getAbilities().flying;
	protected final boolean flying = creativeFlying || Feature.getState(Fly.class);
	private final boolean immuneToFallDamage = invulnerable || Feature.getState(NoFall.class);
	private final boolean noWaterSlowdown = Feature.getState(NoPush.class) && Feature.get(NoPush.class).water;
	private final boolean jesus = Feature.getState(Jesus.class);
	private final boolean spider = Feature.getState(Spider.class);
	protected boolean fallingAllowed = true;
	protected boolean divingAllowed = true;

	private final PathPos start;
	protected PathPos current;
	private final BlockPos goal;

	private final HashMap<PathPos, Float> costMap = new HashMap<>();
	protected final HashMap<PathPos, PathPos> prevPosMap = new HashMap<>();
	private final PathQueue queue = new PathQueue();

	protected int thinkSpeed = 1024;
	protected int thinkTime = 200;
	protected boolean canMine;
	private int iterations;

	protected boolean done;
	protected boolean failed;
	private final ArrayList<PathPos> path = new ArrayList<>();

	public PathFinder(BlockPos goal)
	{
		if(Wrapper.INSTANCE.getPlayer().isOnGround())
			start = new PathPos(new BlockPos(Wrapper.INSTANCE.getPlayer().getX(),Wrapper.INSTANCE.getPlayer().getY() + 0.5, Wrapper.INSTANCE.getPlayer().getZ()));
		else
			start = new PathPos(new BlockPos(Wrapper.INSTANCE.getPlayer().getPos()));
		this.goal = goal;

		costMap.put(start, 0F);
		queue.add(start, getHeuristic(start));
	}

	public PathFinder(PathFinder pathFinder)
	{
		this(pathFinder.goal);
		thinkSpeed = pathFinder.thinkSpeed;
		thinkTime = pathFinder.thinkTime;
	}

	public void think()
	{
		if(done)
			throw new IllegalStateException("Path was already found!");

		int i = 0;
		for(; i < thinkSpeed && !checkFailed(); i++)
		{
			// get next position from queue
			current = queue.poll();

			// check if path is found
			if(checkDone())
				return;

			// add neighbors to queue
			for(PathPos next : getNeighbors(current))
			{
				// check cost
				float newCost = costMap.get(current) + getCost(current, next);
				if(costMap.containsKey(next) && costMap.get(next) <= newCost)
					continue;

				// add to queue
				costMap.put(next, newCost);
				prevPosMap.put(next, current);
				queue.add(next, newCost + getHeuristic(next));
			}
		}
		iterations += i;
	}

	protected boolean checkDone()
	{
		return done = goal.equals(current);
	}

	private boolean checkFailed()
	{
		return failed = queue.isEmpty() || iterations >= thinkSpeed * thinkTime;
	}

	private ArrayList<PathPos> getNeighbors(PathPos pos)
	{
		ArrayList<PathPos> neighbors = new ArrayList<>();

		// abort if too far away
		if(Math.abs(start.getX() - pos.getX()) > 256
				|| Math.abs(start.getZ() - pos.getZ()) > 256)
			return neighbors;

		// get all neighbors
		BlockPos north = pos.north();
		BlockPos east = pos.east();
		BlockPos south = pos.south();
		BlockPos west = pos.west();

		BlockPos northEast = north.east();
		BlockPos southEast = south.east();
		BlockPos southWest = south.west();
		BlockPos northWest = north.west();

		BlockPos up = pos.up();
		BlockPos down = pos.down();

		// flying
		boolean flying = canFlyAt(pos);
		// walking
		boolean onGround = canBeSolid(down);

		// player can move sideways if flying, standing on the ground, jumping,
		// or inside of a block that allows sideways movement (ladders, webs,
		// etc.)
		if(flying || onGround || pos.isJumping()
				|| canMoveSidewaysInMidairAt(pos) || canClimbUpAt(pos.down()))
		{
			// north
			if(checkHorizontalMovement(pos, north))
				neighbors.add(new PathPos(north));

			// east
			if(checkHorizontalMovement(pos, east))
				neighbors.add(new PathPos(east));

			// south
			if(checkHorizontalMovement(pos, south))
				neighbors.add(new PathPos(south));

			// west
			if(checkHorizontalMovement(pos, west))
				neighbors.add(new PathPos(west));

			// north-east
			if(checkDiagonalMovement(pos, Direction.NORTH, Direction.EAST))
				neighbors.add(new PathPos(northEast));

			// south-east
			if(checkDiagonalMovement(pos, Direction.SOUTH, Direction.EAST))
				neighbors.add(new PathPos(southEast));

			// south-west
			if(checkDiagonalMovement(pos, Direction.SOUTH, Direction.WEST))
				neighbors.add(new PathPos(southWest));

			// north-west
			if(checkDiagonalMovement(pos, Direction.NORTH, Direction.WEST))
				neighbors.add(new PathPos(northWest));
		}

		// up
		if(pos.getY() < Wrapper.INSTANCE.getWorld().getHeight() && canGoThrough(up.up())
				&& (flying || onGround || canClimbUpAt(pos))
				&& (flying || canClimbUpAt(pos) || goal.equals(up)
				|| canSafelyStandOn(north) || canSafelyStandOn(east)
				|| canSafelyStandOn(south) || canSafelyStandOn(west))
				&& (divingAllowed || WorldHelper.INSTANCE.getBlockState(up.up()).getMaterial() != Material.WATER))
			neighbors.add(new PathPos(up, onGround));

		// down
		if(pos.getY() > Wrapper.INSTANCE.getWorld().getBottomY() && canGoThrough(down) && canGoAbove(down.down())
				&& (flying || canFallBelow(pos)) && (divingAllowed
				|| WorldHelper.INSTANCE.getBlockState(pos).getMaterial() != Material.WATER))
			neighbors.add(new PathPos(down));

		return neighbors;
	}

	private boolean checkHorizontalMovement(BlockPos current, BlockPos next) {
		if(isPassable(next) && (canFlyAt(current) || canGoThrough(next.down()) || canSafelyStandOn(next.down())))
			return true;

		return false;
	}

	private boolean checkDiagonalMovement(BlockPos current, Direction direction1, Direction direction2) {
		BlockPos horizontal1 = current.offset(direction1);
		BlockPos horizontal2 = current.offset(direction2);
		BlockPos next = horizontal1.offset(direction2);

		if(isPassableWithoutMining(horizontal1)
				&& isPassableWithoutMining(horizontal2)
				&& checkHorizontalMovement(current, next))
			return true;

		return false;
	}

	protected boolean isPassable(BlockPos pos)
	{
		if(!canGoThrough(pos) && !isMineable(pos))
			return false;

		BlockPos up = pos.up();
		if(!canGoThrough(up) && !isMineable(up))
			return false;

		if(!canGoAbove(pos.down()))
			return false;

		if(!divingAllowed && WorldHelper.INSTANCE.getBlockState(up).getMaterial() == Material.WATER)
			return false;

		return true;
	}

	protected boolean isPassableWithoutMining(BlockPos pos)
	{
		if(!canGoThrough(pos))
			return false;

		BlockPos up = pos.up();
		if(!canGoThrough(up))
			return false;

		if(!canGoAbove(pos.down()))
			return false;

		if(!divingAllowed && WorldHelper.INSTANCE.getBlockState(up).getMaterial() == Material.WATER)
			return false;

		return true;
	}

	protected boolean isMineable(BlockPos pos)
	{
		BlockState blockState = WorldHelper.INSTANCE.getBlockState(pos);
		return canMine && blockState.getCollisionShape(Wrapper.INSTANCE.getWorld(), pos) != VoxelShapes.empty() && WorldHelper.INSTANCE.getBlockBreakingSpeed(blockState, Wrapper.INSTANCE.getPlayer().getMainHandStack()) > 0;
	}

	protected boolean canBeSolid(BlockPos pos)
	{
		BlockState state = WorldHelper.INSTANCE.getBlockState(pos);
		Material material = state.getMaterial();
		Block block = state.getBlock();

		return material.blocksMovement()
				&& !(block instanceof AbstractSignBlock)
				|| block instanceof LadderBlock || jesus
				&& (material == Material.WATER || material == Material.LAVA);
	}

	@SuppressWarnings("deprecation")
	private boolean canGoThrough(BlockPos pos)
	{
		// check if loaded
		// Can't see why isChunkLoaded() is deprecated. Still seems to be widely
		// used with no replacement.
		if(!Wrapper.INSTANCE.getWorld().isChunkLoaded(pos))
			return false;

		// check if solid
		Material material = WorldHelper.INSTANCE.getBlockState(pos).getMaterial();
		Block block = WorldHelper.INSTANCE.getBlock(pos);
		if(material.blocksMovement() && !(block instanceof AbstractSignBlock))
			return false;

		// check if trapped
		if(block instanceof TripwireBlock
				|| block instanceof PressurePlateBlock)
			return false;

		// check if safe
		if((!invulnerable && !Wrapper.INSTANCE.getPlayer().hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) && (material == Material.LAVA || material == Material.FIRE))
			return false;

		return true;
	}

	private boolean canGoAbove(BlockPos pos)
	{
		// check for fences, etc.
		Block block = WorldHelper.INSTANCE.getBlock(pos);
		return !(block instanceof FenceBlock) && !(block instanceof WallBlock) && !(block instanceof FenceGateBlock);
	}

	private boolean canSafelyStandOn(BlockPos pos)
	{
		// check if solid
		Material material = WorldHelper.INSTANCE.getBlockState(pos).getMaterial();
		if(!canBeSolid(pos))
			return false;

		// check if safe																								TODO: lava jesus
		if(!invulnerable && (WorldHelper.INSTANCE.getBlock(pos) == Blocks.MAGMA_BLOCK || material == Material.CACTUS || (material == Material.LAVA && !Feature.getState(Jesus.class))))
			return false;

		return true;
	}

	private boolean canFallBelow(PathPos pos)
	{
		// check if player can keep falling
		BlockPos down2 = pos.down(2);
		if(fallingAllowed && canGoThrough(down2))
			return true;

		// check if player can stand below
		if(!canSafelyStandOn(down2))
			return false;

		// check if fall damage is off
		if(immuneToFallDamage && fallingAllowed)
			return true;

		// check if fall ends with slime block
		if(WorldHelper.INSTANCE.getBlock(down2) instanceof SlimeBlock && fallingAllowed)
			return true;

		// check fall damage
		BlockPos prevPos = pos;
		for(int i = 0; i <= (fallingAllowed ? 3 : 1); i++)
		{
			// check if prevPos does not exist, meaning that the pathfinding
			// started during the fall and fall damage should be ignored because
			// it cannot be prevented
			if(prevPos == null)
				return true;

			// check if point is not part of this fall, meaning that the fall is
			// too short to cause any damage
			if(!pos.up(i).equals(prevPos))
				return true;

			// check if block resets fall damage
			Block prevBlock = WorldHelper.INSTANCE.getBlock(prevPos);
			BlockState prevState = WorldHelper.INSTANCE.getBlockState(prevPos);
			if(prevState.getMaterial() == Material.WATER
					|| prevBlock instanceof LadderBlock
					|| prevBlock instanceof VineBlock
					|| prevBlock instanceof CobwebBlock)
				return true;

			prevPos = prevPosMap.get(prevPos);
		}

		return false;
	}

	private boolean canFlyAt(BlockPos pos)
	{
		return flying || !noWaterSlowdown
				&& WorldHelper.INSTANCE.getBlockState(pos).getMaterial() == Material.WATER;
	}

	private boolean canClimbUpAt(BlockPos pos)
	{
		// check if this block works for climbing
		Block block = WorldHelper.INSTANCE.getBlock(pos);
		if(!spider && !(block instanceof LadderBlock)
				&& !(block instanceof VineBlock))
			return false;

		// check if any adjacent block is solid
		BlockPos up = pos.up();
		if(!canBeSolid(pos.north()) && !canBeSolid(pos.east())
				&& !canBeSolid(pos.south()) && !canBeSolid(pos.west())
				&& !canBeSolid(up.north()) && !canBeSolid(up.east())
				&& !canBeSolid(up.south()) && !canBeSolid(up.west()))
			return false;

		return true;
	}

	private boolean canMoveSidewaysInMidairAt(BlockPos pos)
	{
		// check feet
		Block blockFeet = WorldHelper.INSTANCE.getBlock(pos);
		if(WorldHelper.INSTANCE.getBlockState(pos).getMaterial().isLiquid()
				|| blockFeet instanceof LadderBlock
				|| blockFeet instanceof VineBlock
				|| blockFeet instanceof CobwebBlock)
			return true;

		// check head
		Block blockHead = WorldHelper.INSTANCE.getBlock(pos.up());
		if(WorldHelper.INSTANCE.getBlockState(pos.up()).getMaterial().isLiquid()
				|| blockHead instanceof CobwebBlock)
			return true;

		return false;
	}

	private float getCost(BlockPos current, BlockPos next)
	{
		float[] costs = {0.5F, 0.5F};
		BlockPos[] positions = {current, next};

		for(int i = 0; i < positions.length; i++)
		{
			BlockPos pos = positions[i];
			Material material = WorldHelper.INSTANCE.getBlockState(pos).getMaterial();

			// liquids
			if(material == Material.WATER && !noWaterSlowdown)
				costs[i] *= 1.3164437838225804F;
			else if(material == Material.LAVA)
				costs[i] *= 4.539515393656079F;

			// soul sand
			if(!canFlyAt(pos) && WorldHelper.INSTANCE.getBlock(pos.down()) instanceof SoulSandBlock)
				costs[i] *= 2.5F;

			// mining
			if(isMineable(pos))
				costs[i] *= Math.min(2, 15 / WorldHelper.INSTANCE.getBlockBreakingSpeed(WorldHelper.INSTANCE.getBlockState(pos), Wrapper.INSTANCE.getPlayer().getMainHandStack()));
			if(isMineable(pos.up()))
				costs[i] *= Math.min(2, 15 / WorldHelper.INSTANCE.getBlockBreakingSpeed(WorldHelper.INSTANCE.getBlockState(pos.up()), Wrapper.INSTANCE.getPlayer().getMainHandStack()));
		}

		float cost = costs[0] + costs[1];

		// diagonal movement
		if(current.getX() != next.getX() && current.getZ() != next.getZ())
			cost *= 1.4142135623730951F;

		return cost;
	}

	private float getHeuristic(BlockPos pos)
	{
		float dx = Math.abs(pos.getX() - goal.getX());
		float dy = Math.abs(pos.getY() - goal.getY());
		float dz = Math.abs(pos.getZ() - goal.getZ());
		return 1.001F * (dx + dy + dz - 0.5857864376269049F * Math.min(dx, dz));
	}

	public PathPos getCurrentPos()
	{
		return current;
	}

	public BlockPos getGoal()
	{
		return goal;
	}

	public int countProcessedBlocks()
	{
		return prevPosMap.size();
	}

	public int getQueueSize()
	{
		return queue.size();
	}

	public float getCost(BlockPos pos)
	{
		return costMap.get(pos);
	}

	public boolean isDone()
	{
		return done;
	}

	public boolean isFailed()
	{
		return failed;
	}

	public boolean isCanMine() {
		return canMine;
	}

	public void setCanMine(boolean canMine) {
		this.canMine = canMine;
	}

	public ArrayList<PathPos> formatPath()
	{
		if(!done && !failed)
			throw new IllegalStateException("No path found!");
		if(!path.isEmpty())
			throw new IllegalStateException("Path was already formatted!");

		// get last position
		PathPos pos;
		if(!failed)
			pos = current;
		else {
			pos = start;
			for(PathPos next : prevPosMap.keySet())
				if(getHeuristic(next) < getHeuristic(pos) && (canFlyAt(next) || canBeSolid(next.down())))
					pos = next;
		}

		// get positions
		while(pos != null)
		{
			path.add(pos);
			pos = prevPosMap.get(pos);
		}

		// reverse path
		Collections.reverse(path);

		return path;
	}

	public void renderPath(MatrixStack matrixStack, boolean debugMode, boolean depthTest) {
		ArrayList<Render3DHelper.BoxStorage> boxes = new ArrayList<>();
		if (debugMode) {
			for (PathPos pathPos : queue.toArray()) {
				Vec3d vec = Render3DHelper.INSTANCE.getRenderPosition(Vec3d.ofCenter(pathPos));
				Box box = new Box(vec.getX() - 0.05, vec.getY() - 0.05, vec.getZ() - 0.05, vec.getX() + 0.05, vec.getY() + 0.05, vec.getZ() + 0.05);
				if (boxes.size() < 5000)
					boxes.add(new Render3DHelper.BoxStorage(box, 0xffffff00));
			}

			for(Map.Entry<PathPos, PathPos> entry : prevPosMap.entrySet()) {
				Vec3d vec = Render3DHelper.INSTANCE.getRenderPosition(Vec3d.ofCenter(entry.getKey()));
				Box box = new Box(vec.getX() - 0.05, vec.getY() - 0.05, vec.getZ() - 0.05, vec.getX() + 0.05, vec.getY() + 0.05, vec.getZ() + 0.05);
				if (boxes.size() < 5000)
					boxes.add(new Render3DHelper.BoxStorage(box, 0xffff00ff));
			}
		}

		for (PathPos pathPos : path) {
			Vec3d vec = Render3DHelper.INSTANCE.getRenderPosition(Vec3d.ofCenter(pathPos));
			Box box = new Box(vec.getX() - 0.05, vec.getY() - 0.05, vec.getZ() - 0.05, vec.getX() + 0.05, vec.getY() + 0.05, vec.getZ() + 0.05);
			if (boxes.size() < 5000)
				boxes.add(new Render3DHelper.BoxStorage(box, ColorHelper.INSTANCE.getClientColor()));
		}

		Render3DHelper.INSTANCE.drawList(matrixStack, boxes, depthTest);
	}

	public boolean isPathStillValid(int index)
	{
		if(path.isEmpty())
			throw new IllegalStateException("Path is not formatted!");

		// check player abilities
		if(invulnerable != Wrapper.INSTANCE.getPlayer().getAbilities().creativeMode
				|| flying != (creativeFlying
				|| Feature.get(Fly.class).getState())
				|| immuneToFallDamage != (invulnerable
				|| Feature.get(NoFall.class).getState())
				|| noWaterSlowdown != Feature.get(NoPush.class).getState() && ((NoPush)Feature.get(NoPush.class)).water
				|| jesus != Feature.get(Jesus.class).getState()
				|| spider != Feature.get(Spider.class).getState())
			return false;

		// if index is zero, check if first pos is safe
		if(index == 0)
		{
			PathPos pos = path.get(0);
			if(!isPassable(pos) || !canFlyAt(pos) && !canGoThrough(pos.down())
					&& !canSafelyStandOn(pos.down()))
				return false;
		}

		// check path
		for(int i = Math.max(1, index); i < path.size(); i++)
			if(!getNeighbors(path.get(i - 1)).contains(path.get(i)))
				return false;

		return true;
	}

	public PathProcessor getProcessor()
	{
		if(flying)
			return new FlyPathProcessor(path, creativeFlying);

		return new WalkPathProcessor(path);
	}

	public void setThinkSpeed(int thinkSpeed)
	{
		this.thinkSpeed = thinkSpeed;
	}

	public void setThinkTime(int thinkTime)
	{
		this.thinkTime = thinkTime;
	}

	public void setFallingAllowed(boolean fallingAllowed)
	{
		this.fallingAllowed = fallingAllowed;
	}

	public void setDivingAllowed(boolean divingAllowed)
	{
		this.divingAllowed = divingAllowed;
	}

	public List<PathPos> getPath()
	{
		return Collections.unmodifiableList(path);
	}
}
