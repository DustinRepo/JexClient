/*
 * Copyright (c) 2014-2021 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package me.dustin.jex.helper.world.wurstpathfinder;

import java.util.ArrayList;

import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.Step;
import me.dustin.jex.feature.mod.impl.movement.speed.Speed;
import me.dustin.jex.feature.mod.impl.player.Freecam;
import me.dustin.jex.feature.mod.impl.player.Jesus;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.VineBlock;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class WalkPathProcessor extends PathProcessor
{
	public WalkPathProcessor(ArrayList<PathPos> path)
	{
		super(path);
	}

	@Override
	public void process()
	{
		// get positions
		BlockPos pos;
		if(Wrapper.INSTANCE.getPlayer().isOnGround())
			pos = new BlockPos(Wrapper.INSTANCE.getPlayer().getX(),
					Wrapper.INSTANCE.getPlayer().getY() + 0.5,
					Wrapper.INSTANCE.getPlayer().getZ());
		else
			pos = new BlockPos(Wrapper.INSTANCE.getPlayer().getPos());
		PathPos nextPos = path.get(index);
		int posIndex = path.indexOf(pos);

		if(posIndex == -1)
			ticksOffPath++;
		else
			ticksOffPath = 0;

		// update index
		if(pos.equals(nextPos))
		{
			index++;

			// disable when done
			if(index >= path.size())
				done = true;
			return;
		}
		if(posIndex > index)
		{
			index = posIndex + 1;

			// disable when done
			if(index >= path.size())
				done = true;
			return;
		}

		lockControls();
		Wrapper.INSTANCE.getPlayer().getAbilities().flying = false;
		float yaw = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getPlayer(), new Vec3d(nextPos.getX() + 0.5f, nextPos.getY(), nextPos.getZ() + 0.5f)).getYaw();

		if (WorldHelper.INSTANCE.getBlockState(nextPos).getMaterial().blocksMovement()) {
			Wrapper.INSTANCE.getMultiPlayerGameMode().updateBlockBreakingProgress(nextPos, Direction.UP);
			Wrapper.INSTANCE.getPlayer().swingHand(Hand.MAIN_HAND);
			return;
		} else if (WorldHelper.INSTANCE.getBlockState(nextPos.up()).getMaterial().blocksMovement()) {
			Wrapper.INSTANCE.getMultiPlayerGameMode().updateBlockBreakingProgress(nextPos.up(), Direction.UP);
			Wrapper.INSTANCE.getPlayer().swingHand(Hand.MAIN_HAND);
			return;
		}

		if(Feature.getState(Jesus.class)) {
			// wait for Jesus to swim up
			if(Wrapper.INSTANCE.getPlayer().getY() - nextPos.getY() < -0.5f && (Wrapper.INSTANCE.getPlayer().isTouchingWater() || Wrapper.INSTANCE.getPlayer().isInLava()))
				return;

			// manually swim down if using Jesus
			if(Wrapper.INSTANCE.getPlayer().getY() - nextPos.getY() > 0.5 && (Wrapper.INSTANCE.getPlayer().isTouchingWater() || Wrapper.INSTANCE.getPlayer().isInLava() || WorldHelper.INSTANCE.isOnLiquid(Wrapper.INSTANCE.getPlayer()) || WorldHelper.INSTANCE.isTouchingLiquidBlockSpace(Wrapper.INSTANCE.getPlayer())))
				if (Wrapper.INSTANCE.getPlayer() == Freecam.playerEntity) {
					Wrapper.INSTANCE.getPlayer().setSneaking(true);
				} else {
					Wrapper.INSTANCE.getOptions().sneakKey.setPressed(true);
				}
			else
				Wrapper.INSTANCE.getPlayer().setSneaking(false);
		}

		Vec3d vecInPos = new Vec3d(nextPos.getX() + 0.5, nextPos.getY() + 0.1, nextPos.getZ() + 0.5);
		// horizontal movement
		if(pos.getX() != nextPos.getX() || pos.getZ() != nextPos.getZ())
		{
			PlayerHelper.INSTANCE.setVelocityX(Wrapper.INSTANCE.getPlayer(), 0);
			PlayerHelper.INSTANCE.setVelocityZ(Wrapper.INSTANCE.getPlayer(), 0);
			double newx = -Math.sin(yaw * 3.1415927F / 180.0F) * moveSpeed();
			double newz = Math.cos(yaw * 3.1415927F / 180.0F) * moveSpeed();
			if(Wrapper.INSTANCE.getPlayer().isTouchingWater()){
				newx *= 0.4;
				newz *= 0.4;
			}
			if (Speed.INSTANCE.getState()) {
				if (Wrapper.INSTANCE.getPlayer().getPos().distanceTo(vecInPos) <= getSpeedModSpeed()) {
					PlayerHelper.INSTANCE.setVelocityX(Wrapper.INSTANCE.getPlayer(), 0);
					PlayerHelper.INSTANCE.setVelocityZ(Wrapper.INSTANCE.getPlayer(), 0);
					Wrapper.INSTANCE.getPlayer().setPosition(vecInPos.x, vecInPos.y, vecInPos.z);
					return;
				}
			}

			PlayerHelper.INSTANCE.setVelocityX(Wrapper.INSTANCE.getPlayer(), newx);
			PlayerHelper.INSTANCE.setVelocityZ(Wrapper.INSTANCE.getPlayer(), newz);

			if(index > 0 && path.get(index - 1).isJumping() || pos.getY() < nextPos.getY()) {
				if (!Feature.getState(Step.class)) {
					double d = (double)(0.42f * getJumpVelocityMultiplier()) + Wrapper.INSTANCE.getPlayer().getJumpBoostVelocityModifier();
					Vec3d vec3d = Wrapper.INSTANCE.getPlayer().getVelocity();
					if (Wrapper.INSTANCE.getPlayer().isOnGround())
						Wrapper.INSTANCE.getPlayer().setVelocity(vec3d.x, d, vec3d.z);
				}
			}
			// vertical movement
		}else if(pos.getY() != nextPos.getY())
			// go up
			if(pos.getY() < nextPos.getY()) {
				// climb up
				// TODO: Spider
				Block block = WorldHelper.INSTANCE.getBlock(pos);
				if(block instanceof LadderBlock || block instanceof VineBlock) {
					Wrapper.INSTANCE.getOptions().jumpKey.setPressed(true);
				}else {
					// directional jump
					if(index < path.size() - 1
							&& !nextPos.up().equals(path.get(index + 1)))
						index++;

					// jump up
					if (!Feature.getState(Step.class)) {
						double d = (double)(0.42f * getJumpVelocityMultiplier()) + Wrapper.INSTANCE.getPlayer().getJumpBoostVelocityModifier();
						Vec3d vec3d = Wrapper.INSTANCE.getPlayer().getVelocity();
						if (Wrapper.INSTANCE.getPlayer().isOnGround())
							Wrapper.INSTANCE.getPlayer().setVelocity(vec3d.x, d, vec3d.z);
					}
				}
				// go down
			}else {
				// skip mid-air nodes and go straight to the bottom
				while(index < path.size() - 1
						&& path.get(index).down().equals(path.get(index + 1)))
					index++;

				// walk off the edge
				if(Wrapper.INSTANCE.getPlayer().isOnGround()) {
					PlayerHelper.INSTANCE.setVelocityX(Wrapper.INSTANCE.getPlayer(), 0);
					PlayerHelper.INSTANCE.setVelocityZ(Wrapper.INSTANCE.getPlayer(), 0);
					double newx = -Math.sin(yaw * 3.1415927F / 180.0F) * moveSpeed();
					double newz = Math.cos(yaw * 3.1415927F / 180.0F) * moveSpeed();
					if(Wrapper.INSTANCE.getPlayer().isTouchingWater() || PlayerHelper.INSTANCE.isOnEdgeOfBlock()){
						newx *= 0.4;
						newz *= 0.4;
					}
					//fix for speed going way past the point
					if (Speed.INSTANCE.getState()) {
						if (Wrapper.INSTANCE.getPlayer().getPos().distanceTo(vecInPos) <= getSpeedModSpeed()) {
							PlayerHelper.INSTANCE.setVelocityX(Wrapper.INSTANCE.getPlayer(), 0);
							PlayerHelper.INSTANCE.setVelocityZ(Wrapper.INSTANCE.getPlayer(), 0);
							Wrapper.INSTANCE.getPlayer().setPosition(vecInPos.x, vecInPos.y, vecInPos.z);
							return;
						}
					}
					//fix for player going way past the point even with speed disabled (speed potions, soul speed, etc)
					if (Wrapper.INSTANCE.getPlayer().getPos().distanceTo(vecInPos) <= Math.abs(Math.abs(newx) + Math.abs(newz))) {
						PlayerHelper.INSTANCE.setVelocityX(Wrapper.INSTANCE.getPlayer(), 0);
						PlayerHelper.INSTANCE.setVelocityZ(Wrapper.INSTANCE.getPlayer(), 0);
						Wrapper.INSTANCE.getPlayer().setPosition(vecInPos.x, vecInPos.y, vecInPos.z);
						return;
					}
					PlayerHelper.INSTANCE.setVelocityX(Wrapper.INSTANCE.getPlayer(), newx);
					PlayerHelper.INSTANCE.setVelocityZ(Wrapper.INSTANCE.getPlayer(), newz);
				}
			}
	}

	protected float getJumpVelocityMultiplier() {
		float f = WorldHelper.INSTANCE.getBlock(Wrapper.INSTANCE.getPlayer().getBlockPos()).getJumpVelocityMultiplier();
		float g = WorldHelper.INSTANCE.getBlock(new BlockPos(Wrapper.INSTANCE.getPlayer().getPos().x, Wrapper.INSTANCE.getPlayer().getBoundingBox().minY - 0.5000001, Wrapper.INSTANCE.getPlayer().getPos().z)).getJumpVelocityMultiplier();
		return (double)f == 1.0 ? g : f;
	}

	public double moveSpeed() {
		if (Speed.INSTANCE.getState()) {
			return getSpeedModSpeed();
		}
		return PlayerHelper.INSTANCE.getBaseMoveSpeed();
	}

	public double getSpeedModSpeed() {
		return switch (Speed.INSTANCE.mode.toLowerCase()) {
			case "vanilla" -> Speed.INSTANCE.vanillaSpeed;
			case "strafe" -> Speed.INSTANCE.strafeSpeed;
			default -> PlayerHelper.INSTANCE.getBaseMoveSpeed();
		};
	}

	public float getYaw(Vec3d pos)
	{
		double xD = Wrapper.INSTANCE.getPlayer().getX() - pos.getX();
		double zD = Wrapper.INSTANCE.getPlayer().getZ() - pos.getZ();
		double yaw = Math.atan2(zD, xD);
		return (float)Math.toDegrees(yaw) + 90.0F;
	}
}

