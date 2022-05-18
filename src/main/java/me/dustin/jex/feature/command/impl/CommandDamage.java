package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

@Cmd(name = "damage", description = "Cause yourself to take damage", syntax = ".damage <amount> (max 7)", alias = {"dmg"})
public class CommandDamage extends Command {

    @Override
    public void registerCommand() {
        dispatcher.register(literal(this.name).then(argument("amount", IntegerArgumentType.integer(1, 7)).executes(this)));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        int damage = IntegerArgumentType.getInteger(context, "amount");
        NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY() + 2.1 + damage, Wrapper.INSTANCE.getLocalPlayer().getZ(), true));
        NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
        NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), true));
        ChatHelper.INSTANCE.addClientMessage("Dealing \247b" + damage + " \2477damage.");
        return 1;
    }
}
