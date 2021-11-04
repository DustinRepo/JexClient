/*
 * Copyright (c) 2014-2021 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package me.dustin.jex.helper.world.wurstpathfinder;

import java.util.ArrayList;

import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.load.impl.IKeyBinding;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public abstract class PathProcessor
{
	private static final KeyBinding[] CONTROLS =
		{Wrapper.INSTANCE.getOptions().keyForward, Wrapper.INSTANCE.getOptions().keyBack, Wrapper.INSTANCE.getOptions().keyRight,
			Wrapper.INSTANCE.getOptions().keyLeft, Wrapper.INSTANCE.getOptions().keyJump, Wrapper.INSTANCE.getOptions().keySneak};
	
	protected final ArrayList<PathPos> path;
	protected int index;
	protected boolean done;
	protected int ticksOffPath;

	public static boolean lockedControls;
	
	public PathProcessor(ArrayList<PathPos> path)
	{
		if(path.isEmpty())
			throw new IllegalStateException("There is no path!");
		
		this.path = path;
	}
	
	public abstract void process();
	
	public final int getIndex()
	{
		return index;
	}
	
	public final boolean isDone()
	{
		return done;
	}
	
	public final int getTicksOffPath()
	{
		return ticksOffPath;
	}
	
	protected final void facePosition(BlockPos pos)
	{
		RotationVector rotationVector = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), Vec3d.ofCenter(pos));
		Wrapper.INSTANCE.getLocalPlayer().setYaw(rotationVector.getYaw());
		Wrapper.INSTANCE.getLocalPlayer().setPitch(0);
	}
	
	public static final void lockControls()
	{
		lockedControls = true;
		// disable keys
		for(KeyBinding key : CONTROLS)
			key.setPressed(false);
		
		// disable sprinting
		//Wrapper.INSTANCE.getLocalPlayer().setSprinting(false);
	}
	
	public static final void releaseControls()
	{
		lockedControls = false;
		// reset keys
		for(KeyBinding key : CONTROLS)
			key.setPressed(((IKeyBinding)key).isActuallyPressed());
	}
}
