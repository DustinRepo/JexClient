/*
 * Copyright (c) 2014-2021 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package me.dustin.jex.helper.world.wurstpathfinder;

import me.dustin.jex.JexClient;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.Fly;
import me.dustin.jex.feature.mod.impl.movement.speed.Speed;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import net.minecraft.util.math.*;

import java.util.ArrayList;

public class FlyPathProcessor extends PathProcessor
{
	private final boolean creativeFlying;
	
	public FlyPathProcessor(ArrayList<PathPos> path, boolean creativeFlying)
	{
		super(path);
		this.creativeFlying = creativeFlying;
	}
	
	@Override
	public void process()
	{
		// get positions
		BlockPos pos = new BlockPos(Wrapper.INSTANCE.getLocalPlayer().getPos());
		Vec3d posVec = Wrapper.INSTANCE.getLocalPlayer().getPos();
		BlockPos nextPos = path.get(index);
		int posIndex = path.indexOf(pos);
		Box nextBox = new Box(nextPos.getX() + 0.3, nextPos.getY(), nextPos.getZ() + 0.3, nextPos.getX() + 0.7, nextPos.getY() + 0.2, nextPos.getZ() + 0.7);
		
		if(posIndex == -1)
			ticksOffPath++;
		else
			ticksOffPath = 0;
		
		// update index
		if(posIndex > index
			|| posVec.x >= nextBox.minX && posVec.x <= nextBox.maxX
				&& posVec.y >= nextBox.minY && posVec.y <= nextBox.maxY
				&& posVec.z >= nextBox.minZ && posVec.z <= nextBox.maxZ)
		{
			if(posIndex > index)
				index = posIndex + 1;
			else
				index++;
			
			// stop when changing directions
			if(creativeFlying)
			{
				Vec3d v = Wrapper.INSTANCE.getLocalPlayer().getVelocity();
				Wrapper.INSTANCE.getLocalPlayer().setVelocity(v.x / Math.max(Math.abs(v.x) * 50, 1), v.y / Math.max(Math.abs(v.y) * 50, 1), v.z / Math.max(Math.abs(v.z) * 50, 1));
			}
			
			if(index >= path.size())
				done = true;
			
			return;
		}
		
		lockControls();
		Wrapper.INSTANCE.getLocalPlayer().getAbilities().flying = creativeFlying;
		boolean x = posVec.x < nextBox.minX || posVec.x > nextBox.maxX;
		boolean y = posVec.y < nextBox.minY || posVec.y > nextBox.maxY;
		boolean z = posVec.z < nextBox.minZ || posVec.z > nextBox.maxZ;
		boolean horizontal = x || z;
		
		// skip mid-air nodes
		Vec3i offset = nextPos.subtract(pos);
		while(index < path.size() - 1
			&& path.get(index).add(offset).equals(path.get(index + 1)))
			index++;
		
		if(creativeFlying)
		{
			Vec3d v = Wrapper.INSTANCE.getLocalPlayer().getVelocity();
			
			if(!x)
				Wrapper.INSTANCE.getLocalPlayer().setVelocity(v.x / Math.max(Math.abs(v.x) * 50, 1), v.y, v.z);
			if(!y)
				Wrapper.INSTANCE.getLocalPlayer().setVelocity(v.x, v.y / Math.max(Math.abs(v.y) * 50, 1), v.z);
			if(!z)
				Wrapper.INSTANCE.getLocalPlayer().setVelocity(v.x, v.y, v.z / Math.max(Math.abs(v.z) * 50, 1));
		}
		
		Vec3d vecInPos = new Vec3d(nextPos.getX() + 0.5, nextPos.getY() + 0.1, nextPos.getZ() + 0.5);
		
		// horizontal movement
		if(horizontal)
		{
			float yaw = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), new Vec3d(nextPos.getX() + 0.5f, nextPos.getY(), nextPos.getZ() + 0.5f)).getYaw();
			PlayerHelper.INSTANCE.setVelocityX(0);
			PlayerHelper.INSTANCE.setVelocityZ(0);
			double newx = -Math.sin(yaw * 3.1415927F / 180.0F) * moveSpeed();
			double newz = Math.cos(yaw * 3.1415927F / 180.0F) * moveSpeed();
			if(Wrapper.INSTANCE.getLocalPlayer().isTouchingWater()){
				newx *= 0.4;
				newz *= 0.4;
			}
			//fix for speed going way past the point
			if (Feature.get(Fly.class).getState()) {
				if (!creativeFlying && Wrapper.INSTANCE.getLocalPlayer().getPos().distanceTo(vecInPos) <= ((Fly) Feature.get(Fly.class)).speed) {
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
			
			if(Wrapper.INSTANCE.getLocalPlayer().horizontalCollision)
				if(posVec.y > nextBox.maxY)
					Wrapper.INSTANCE.getOptions().keySneak.setPressed(true);
				else if(posVec.y < nextBox.minY)
					Wrapper.INSTANCE.getOptions().keyJump.setPressed(true);
				
			// vertical movement
		}else if(y)
		{
			PlayerHelper.INSTANCE.setVelocityY(0);
			if(!creativeFlying && Wrapper.INSTANCE.getLocalPlayer().getPos().distanceTo(vecInPos) <= ((Fly) Feature.get(Fly.class)).speed) {
				Wrapper.INSTANCE.getLocalPlayer().setPosition(vecInPos.x, vecInPos.y, vecInPos.z);
				return;
			}
			
			if(posVec.y < nextBox.minY)
				Wrapper.INSTANCE.getOptions().keyJump.setPressed(true);
			else
				Wrapper.INSTANCE.getOptions().keySneak.setPressed(true);
			
			if(Wrapper.INSTANCE.getLocalPlayer().verticalCollision)
			{
				Wrapper.INSTANCE.getOptions().keySneak.setPressed(false);
				Wrapper.INSTANCE.getOptions().keyForward.setPressed(true);
			}
		}
	}

	public double moveSpeed() {
		if (Speed.INSTANCE.getState()) {
			return ((Fly)Feature.get(Fly.class)).speed;
		}
		return PlayerHelper.INSTANCE.getBaseMoveSpeed();
	}
}
