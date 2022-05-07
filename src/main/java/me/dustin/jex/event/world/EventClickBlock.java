package me.dustin.jex.event.world;

import me.dustin.events.core.Event;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class EventClickBlock extends Event {

	private final BlockPos blockPos;
	private final Direction face;
	private final Mode mode;

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
