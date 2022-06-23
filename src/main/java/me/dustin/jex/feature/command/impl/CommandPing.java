package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.command.core.arguments.MessageArgumentType;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.network.ServerPinger;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

@Cmd(name = "ping", description = "Ping a server given an IP", syntax = ".ping <ip>:<port>")
public class CommandPing extends Command {
    @Override
    public void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register(literal(this.name).then(argument("ip(:port)", MessageArgumentType.message()).executes(this)));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        String ip = MessageArgumentType.getMessage(context, "ip(:port)").getString();
        ChatHelper.INSTANCE.addClientMessage("Pinging " + ip + "...");
        ServerPinger serverPinger = new ServerPinger(ip);
        serverPinger.pingServer();
        return 1;
    }
}
