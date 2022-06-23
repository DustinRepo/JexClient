package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.command.core.arguments.MessageArgumentType;
import me.dustin.jex.feature.command.core.arguments.PlayerNameArgumentType;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.ServerListFile;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.network.ServerPinger;
import me.dustin.jex.helper.network.ServerScrubber;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.util.Formatting;

@Cmd(name = "serverscrub", description = "Search for players by name in a customizable list of servers", syntax = ".serverscrub <add/del> <ip>/.serverscrub search <name>")
public class CommandServerScrubber extends Command {
    @Override
    public void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register(literal(this.name).then(literal("add").then(argument("ip(:port)", MessageArgumentType.message()).executes(context -> {
            String ip = MessageArgumentType.getMessage(context, "ip(:port)").getString();
            ServerScrubber.INSTANCE.getServers().add(ip);
            ChatHelper.INSTANCE.addClientMessage("Added " + Formatting.AQUA + ip + Formatting.GRAY + " to the server scrub list");
            ConfigManager.INSTANCE.get(ServerListFile.class).write();
            return 1;
        }))).then(literal("del").then(argument("ip(:port)", MessageArgumentType.message()).executes(context -> {
            String ip = MessageArgumentType.getMessage(context, "ip(:port)").getString();
            ServerScrubber.INSTANCE.getServers().remove(ip);
            ChatHelper.INSTANCE.addClientMessage("Removed " + Formatting.RED + ip + Formatting.GRAY + " from the server scrub list");
            ConfigManager.INSTANCE.get(ServerListFile.class).write();
            return 1;
        }))).then(literal("list").executes(context -> {
            ChatHelper.INSTANCE.addClientMessage("List of IPs to search: ");
            ServerScrubber.INSTANCE.getServers().forEach(ip -> {
                ChatHelper.INSTANCE.addClientMessage(Formatting.GREEN + ip);
            });
            return 1;
        })).then(literal("search").then(argument("name", PlayerNameArgumentType.playerName()).executes(this))));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        String name = PlayerNameArgumentType.getPlayerName(context, "name");
        ChatHelper.INSTANCE.addClientMessage("Searching for " + name + " in " + ServerScrubber.INSTANCE.getServers().size() + " servers...");
        ServerScrubber.INSTANCE.searchFor(name);
        return 1;
    }
}
