package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.client.network.ClientPlayerEntity;

@Cmd(name = "clip", description = "Instantly teleport to position/can be used as a phase.", alias = {"mv"}, syntax = ".clip <x> <y> <z>")
public class CommandClip extends Command {

    @Override
    public void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        LiteralArgumentBuilder<FabricClientCommandSource> builder = literal(this.name).then(argument("x", FloatArgumentType.floatArg()).then(argument("y", FloatArgumentType.floatArg()).then(argument("z", FloatArgumentType.floatArg()).executes(this))));
        dispatcher.register(literal("mv").redirect(dispatcher.register(builder)));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        float xcord = FloatArgumentType.getFloat(context, "x");
        float ycord = FloatArgumentType.getFloat(context, "y");
        float zcord = FloatArgumentType.getFloat(context, "z");
        Wrapper.INSTANCE.getLocalPlayer().setPos(Wrapper.INSTANCE.getLocalPlayer().getX() + xcord, Wrapper.INSTANCE.getLocalPlayer().getY() + ycord, Wrapper.INSTANCE.getLocalPlayer().getZ() + zcord);
        if (Wrapper.INSTANCE.getLocalPlayer().isRiding()) {
            Wrapper.INSTANCE.getLocalPlayer().getVehicle().setPos(Wrapper.INSTANCE.getLocalPlayer().getVehicle().getX() + xcord, Wrapper.INSTANCE.getLocalPlayer().getVehicle().getY() + ycord, Wrapper.INSTANCE.getLocalPlayer().getVehicle().getZ() + zcord);
            NetworkHelper.INSTANCE.sendPacket(new VehicleMoveC2SPacket(Wrapper.INSTANCE.getLocalPlayer().getVehicle()));
        }
        ChatHelper.INSTANCE.addClientMessage("Position teleport done");
        return 1;
    }

}
