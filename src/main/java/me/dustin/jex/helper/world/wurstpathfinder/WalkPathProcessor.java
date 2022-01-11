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
		if(Wrapper.INSTANCE.getLocalPlayer().isOnGround())
			pos = new BlockPos(Wrapper.INSTANCE.getLocalPlayer().getX(),
					Wrapper.INSTANCE.getLocalPlayer().getY() + 0.5,
					Wrapper.INSTANCE.getLocalPlayer().getZ());
		else
			pos = new BlockPos(Wrapper.INSTANCE.getLocalPlayer().getPos());
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
		Wrapper.INSTANCE.getLocalPlayer().getAbilities().flying = false;
		float yaw = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), new Vec3d(nextPos.getX() + 0.5f, nextPos.getY(), nextPos.getZ() + 0.5f)).getYaw();

		if (WorldHelper.INSTANCE.getBlockState(nextPos).getMaterial().blocksMovement()) {
			Wrapper.INSTANCE.getInteractionManager().updateBlockBreakingProgress(nextPos, Direction.UP);
			Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
			return;
		} else if (WorldHelper.INSTANCE.getBlockState(nextPos.up()).getMaterial().blocksMovement()) {
			Wrapper.INSTANCE.getInteractionManager().updateBlockBreakingProgress(nextPos.up(), Direction.UP);
			Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
			return;
		}

		if(Feature.getState(Jesus.class)) {
			// wait for Jesus to swim up
			if(Wrapper.INSTANCE.getLocalPlayer().getY() < nextPos.getY() && (Wrapper.INSTANCE.getLocalPlayer().isTouchingWater() || Wrapper.INSTANCE.getLocalPlayer().isInLava()))
				return;

			// manually swim down if using Jesus
			if(Wrapper.INSTANCE.getLocalPlayer().getY() - nextPos.getY() > 0.5 && (Wrapper.INSTANCE.getLocalPlayer().isTouchingWater() || Wrapper.INSTANCE.getLocalPlayer().isInLava() || WorldHelper.INSTANCE.isOnLiquid(Wrapper.INSTANCE.getLocalPlayer()) || WorldHelper.INSTANCE.isTouchingLiquidBlockSpace(Wrapper.INSTANCE.getLocalPlayer())))
				Wrapper.INSTANCE.getOptions().keySneak.setPressed(true);
		}

		Vec3d vecInPos = new Vec3d(nextPos.getX() + 0.5, nextPos.getY() + 0.1, nextPos.getZ() + 0.5);
		// horizontal movement
		if(pos.getX() != nextPos.getX() || pos.getZ() != nextPos.getZ())
		{
			PlayerHelper.INSTANCE.setVelocityX(0);
			PlayerHelper.INSTANCE.setVelocityZ(0);
			double newx = -Math.sin(yaw * 3.1415927F / 180.0F) * moveSpeed();
			double newz = Math.cos(yaw * 3.1415927F / 180.0F) * moveSpeed();
			if(Wrapper.INSTANCE.getLocalPlayer().isTouchingWater()){
				newx *= 0.4;
				newz *= 0.4;
			}
			if (Speed.INSTANCE.getState()) {
				if (Wrapper.INSTANCE.getLocalPlayer().getPos().distanceTo(vecInPos) <= getSpeedModSpeed()) {
					PlayerHelper.INSTANCE.setVelocityX(0);
					PlayerHelper.INSTANCE.setVelocityZ(0);
					Wrapper.INSTANCE.getLocalPlayer().setPosition(vecInPos.x, vecInPos.y, vecInPos.z);
					return;
				}
			}

			PlayerHelper.INSTANCE.setVelocityX(newx);
			PlayerHelper.INSTANCE.setVelocityZ(newz);

			if(index > 0 && path.get(index - 1).isJumping() || pos.getY() < nextPos.getY())
				Wrapper.INSTANCE.getOptions().keyJump.setPressed(true);

			// vertical movement
		}else if(pos.getY() != nextPos.getY())
			// go up
			if(pos.getY() < nextPos.getY()) {
				// climb up
				// TODO: Spider
				Block block = WorldHelper.INSTANCE.getBlock(pos);
				if(block instanceof LadderBlock || block instanceof VineBlock) {
					Wrapper.INSTANCE.getOptions().keyJump.setPressed(true);
				}else {
					// directional jump
					if(index < path.size() - 1
							&& !nextPos.up().equals(path.get(index + 1)))
						index++;

					// jump up
					if (!Feature.getState(Step.class))
						Wrapper.INSTANCE.getOptions().keyJump.setPressed(true);
				}
				// go down
			}else {
				// skip mid-air nodes and go straight to the bottom
				while(index < path.size() - 1
						&& path.get(index).down().equals(path.get(index + 1)))
					index++;

				// walk off the edge
				if(Wrapper.INSTANCE.getLocalPlayer().isOnGround()) {
					PlayerHelper.INSTANCE.setVelocityX(0);
					PlayerHelper.INSTANCE.setVelocityZ(0);
					double newx = -Math.sin(yaw * 3.1415927F / 180.0F) * moveSpeed();
					double newz = Math.cos(yaw * 3.1415927F / 180.0F) * moveSpeed();
					if(Wrapper.INSTANCE.getLocalPlayer().isTouchingWater() || PlayerHelper.INSTANCE.isOnEdgeOfBlock()){
						newx *= 0.4;
						newz *= 0.4;
					}
					//fix for speed going way past the point
					if (Speed.INSTANCE.getState()) {
						if (Wrapper.INSTANCE.getLocalPlayer().getPos().distanceTo(vecInPos) <= getSpeedModSpeed()) {
							PlayerHelper.INSTANCE.setVelocityX(0);
							PlayerHelper.INSTANCE.setVelocityZ(0);
							Wrapper.INSTANCE.getLocalPlayer().setPosition(vecInPos.x, vecInPos.y, vecInPos.z);
							return;
						}
					}
					//fix for player going way past the point even with speed disabled (speed potions, soul speed, etc)
					if (Wrapper.INSTANCE.getLocalPlayer().getPos().distanceTo(vecInPos) <= Math.abs(Math.abs(newx) + Math.abs(newz))) {
						PlayerHelper.INSTANCE.setVelocityX(0);
						PlayerHelper.INSTANCE.setVelocityZ(0);
						Wrapper.INSTANCE.getLocalPlayer().setPosition(vecInPos.x, vecInPos.y, vecInPos.z);
						return;
					}
					PlayerHelper.INSTANCE.setVelocityX(newx);
					PlayerHelper.INSTANCE.setVelocityZ(newz);
				}
			}
	}

	public double moveSpeed() {
		if (Speed.INSTANCE.getState()) {
			return getSpeedModSpeed();
		}
		return PlayerHelper.INSTANCE.getBaseMoveSpeed();
	}

	public double getSpeedModSpeed() {
		switch (Speed.INSTANCE.mode.toLowerCase()) {
			case "vanilla":
				return Speed.INSTANCE.vanillaSpeed;
			case "strafe":
				return Speed.INSTANCE.strafeSpeed;
		}
		return PlayerHelper.INSTANCE.getBaseMoveSpeed();
	}

	public float getYaw(Vec3d pos)
	{
		double xD = Wrapper.INSTANCE.getLocalPlayer().getX() - pos.getX();
		double zD = Wrapper.INSTANCE.getLocalPlayer().getZ() - pos.getZ();
		double yaw = Math.atan2(zD, xD);
		return (float)Math.toDegrees(yaw) + 90.0F;
	}
}

