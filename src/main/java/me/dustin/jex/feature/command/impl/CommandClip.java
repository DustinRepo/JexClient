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

@Cmd(name = "clip", description = "Instantly teleport up/can be used as a phase.", alias = {"up"}, syntax = ".vclip <height>")
public class CommandClip extends Command {

    @Override
    public void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        LiteralArgumentBuilder<FabricClientCommandSource> builder = literal(this.name).then(argument("y", FloatArgumentType.floatArg()));
        dispatcher.register(literal("up").redirect(dispatcher.register(builder)));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        float up = FloatArgumentType.getFloat(context, "height");
        Wrapper.INSTANCE.getLocalPlayer().setPos(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY() + up, Wrapper.INSTANCE.getLocalPlayer().getZ();
        if (Wrapper.INSTANCE.getLocalPlayer().isRiding()) {
            Wrapper.INSTANCE.getLocalPlayer().getVehicle().setPos(Wrapper.INSTANCE.getLocalPlayer().getVehicle().getX() + xcord, Wrapper.INSTANCE.getLocalPlayer().getVehicle().getY() + ycord, Wrapper.INSTANCE.getLocalPlayer().getVehicle().getZ() + zcord);
            NetworkHelper.INSTANCE.sendPacket(new VehicleMoveC2SPacket(Wrapper.INSTANCE.getLocalPlayer().getVehicle()));
        }
        ChatHelper.INSTANCE.addClientMessage("Position teleport done");
        return 1;
    }

}
