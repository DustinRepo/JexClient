package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;

@Cmd(name = "vclip", description = "Instantly teleport vertically.", alias = {"up"}, syntax = ".vclip <amount>")
public class CommandVClip extends Command {

    @Override
    public void registerCommand() {
        LiteralArgumentBuilder<FabricClientCommandSource> builder = literal(this.name).then(argument("amount", FloatArgumentType.floatArg()).executes(this));
        dispatcher.register(literal("up").redirect(dispatcher.register(builder)));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        float num = FloatArgumentType.getFloat(context, "amount");
        NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY() + num, Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
        NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), -1337.0, Wrapper.INSTANCE.getLocalPlayer().getZ(), true));
        if (Wrapper.INSTANCE.getLocalPlayer().isRiding()) {
            Wrapper.INSTANCE.getLocalPlayer().getVehicle().setPos(Wrapper.INSTANCE.getLocalPlayer().getVehicle().getX(), Wrapper.INSTANCE.getLocalPlayer().getVehicle().getY() + num, Wrapper.INSTANCE.getLocalPlayer().getVehicle().getZ());
            NetworkHelper.INSTANCE.sendPacket(new VehicleMoveC2SPacket(Wrapper.INSTANCE.getLocalPlayer().getVehicle()));
        }
        ChatHelper.INSTANCE.addClientMessage("Vertical teleport done");
        return 1;
    }

}
