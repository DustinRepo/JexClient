package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.command.core.arguments.Vec3ArgumentType;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.world.Excavator;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.util.math.BlockPos;

@Cmd(name = "excavator", description = "Set the positions for Excavator mod", syntax = ".excavator <pos1> <pos2>")
public class CommandExcavatorPos extends Command {
    @Override
    public void registerCommand() {
        dispatcher.register(literal(this.name).then(argument("pos1", Vec3ArgumentType.vec3()).then(argument("pos2", Vec3ArgumentType.vec3()).executes(this))));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        BlockPos pos1 = new BlockPos(Vec3ArgumentType.getVec3(context, "pos1"));
        BlockPos pos2 = new BlockPos(Vec3ArgumentType.getVec3(context, "pos2"));
        Excavator.miningArea = new Excavator.MiningArea(pos1, pos2);
        Feature.get(Excavator.class).setState(true);
        return 1;
    }
}
