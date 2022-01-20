package me.dustin.jex.event.world;

import me.dustin.events.core.Event;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class EventClickBlock extends Event {

	private BlockPos blockPos;
	private Direction face;
	private Mode mode;

	public EventClickBlock(BlockPos loc, Direction face, Mode mode) {
		this.blockPos = loc;
		this.face = face;
		this.mode = mode;
	}

	public BlockPos getBlockPos() {
		return blockPos;
	}

	public Direction getFace() {
		return face;
	}

	public Mode getMode() {
		return this.mode;
	}

	public enum Mode {
		PRE, POST
	}
}
