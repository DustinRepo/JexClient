/*
 * Copyright (c) 2014-2021 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package me.dustin.jex.helper.world.wurstpathfinder;

import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.fly.Fly;
import me.dustin.jex.feature.mod.impl.movement.speed.Speed;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
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
		Fly fly = Feature.get(Fly.class);
		// get positions
		BlockPos pos = new BlockPos(Wrapper.INSTANCE.getPlayer().getPos());
		Vec3d posVec = Wrapper.INSTANCE.getPlayer().getPos();
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
			if(creativeFlying) {
				Vec3d v = Wrapper.INSTANCE.getPlayer().getVelocity();
				Wrapper.INSTANCE.getPlayer().setVelocity(v.x / Math.max(Math.abs(v.x) * 50, 1), v.y / Math.max(Math.abs(v.y) * 50, 1), v.z / Math.max(Math.abs(v.z) * 50, 1));
			}
			
			if(index >= path.size())
				done = true;
			
			return;
		}
		
		Wrapper.INSTANCE.getPlayer().getAbilities().flying = creativeFlying;
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
			Vec3d v = Wrapper.INSTANCE.getPlayer().getVelocity();
			
			if(!x)
				Wrapper.INSTANCE.getPlayer().setVelocity(v.x / Math.max(Math.abs(v.x) * 50, 1), v.y, v.z);
			if(!y)
				Wrapper.INSTANCE.getPlayer().setVelocity(v.x, v.y / Math.max(Math.abs(v.y) * 50, 1), v.z);
			if(!z)
				Wrapper.INSTANCE.getPlayer().setVelocity(v.x, v.y, v.z / Math.max(Math.abs(v.z) * 50, 1));
		}
		
		Vec3d vecInPos = new Vec3d(nextPos.getX() + 0.5, nextPos.getY() + 0.1, nextPos.getZ() + 0.5);
		
		// horizontal movement
		if(horizontal)
		{
			float yaw = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getPlayer(), new Vec3d(nextPos.getX() + 0.5f, nextPos.getY(), nextPos.getZ() + 0.5f)).getYaw();
			PlayerHelper.INSTANCE.setVelocityX(Wrapper.INSTANCE.getPlayer(), 0);
			PlayerHelper.INSTANCE.setVelocityZ(Wrapper.INSTANCE.getPlayer(), 0);
			double newx = -Math.sin(yaw * 3.1415927F / 180.0F) * moveSpeed();
			double newz = Math.cos(yaw * 3.1415927F / 180.0F) * moveSpeed();
			if(Wrapper.INSTANCE.getPlayer().isTouchingWater()){
				newx *= 0.4;
				newz *= 0.4;
			}
			//fix for speed going way past the point
			if (Feature.getState(Fly.class)) {
			float cmp = (fly.hspeedProperty.value() + fly.vspeedProperty.value()) / 2;
				if (!creativeFlying && Wrapper.INSTANCE.getPlayer().getPos().distanceTo(vecInPos) <= cmp) {
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
			
			if(Wrapper.INSTANCE.getPlayer().horizontalCollision)
				if(posVec.y > nextBox.maxY)
					PlayerHelper.INSTANCE.setVelocityY(Wrapper.INSTANCE.getPlayer(), fly.vspeedProperty.value());
				else if(posVec.y < nextBox.minY)
					PlayerHelper.INSTANCE.setVelocityY(Wrapper.INSTANCE.getPlayer(), -fly.vspeedProperty.value());
				
			// vertical movement
		}else if(y)
		{
		      float cmp = (fly.hspeedProperty.value() + fly.vspeedProperty.value()) / 2;
			PlayerHelper.INSTANCE.setVelocityY(Wrapper.INSTANCE.getPlayer(), 0);
			if(!creativeFlying && Wrapper.INSTANCE.getPlayer().getPos().distanceTo(vecInPos) <= cmp) {
				Wrapper.INSTANCE.getPlayer().setPosition(vecInPos.x, vecInPos.y, vecInPos.z);
				return;
			}

			if(posVec.y < nextBox.minY)
				PlayerHelper.INSTANCE.setVelocityY(Wrapper.INSTANCE.getPlayer(), fly.vspeedProperty.value());
			else
				PlayerHelper.INSTANCE.setVelocityY(Wrapper.INSTANCE.getPlayer(), -fly.vspeedProperty.value());
			
			if(Wrapper.INSTANCE.getPlayer().verticalCollision) {
				float yaw = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getPlayer(), new Vec3d(nextPos.getX() + 0.5f, nextPos.getY(), nextPos.getZ() + 0.5f)).getYaw();
				double newx = -Math.sin(yaw * 3.1415927F / 180.0F) * moveSpeed();
				double newz = Math.cos(yaw * 3.1415927F / 180.0F) * moveSpeed();
				if(Wrapper.INSTANCE.getPlayer().isTouchingWater()){
					newx *= 0.4;
					newz *= 0.4;
				}
				PlayerHelper.INSTANCE.setVelocityX(Wrapper.INSTANCE.getPlayer(), newx);
				PlayerHelper.INSTANCE.setVelocityZ(Wrapper.INSTANCE.getPlayer(), newz);
			}
		}
	}

	public double moveSpeed() {
		if (Speed.INSTANCE.getState()) {
			return Feature.get(Fly.class).hspeedProperty.value();
		}
		return PlayerHelper.INSTANCE.getBaseMoveSpeed();
	}
}
