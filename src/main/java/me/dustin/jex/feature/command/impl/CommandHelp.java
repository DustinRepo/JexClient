package me.dustin.jex.feature.command.impl;


import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.CommandManagerJex;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.command.core.arguments.CommandArgumentType;
import me.dustin.jex.helper.misc.ChatHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;

@Cmd(name = "helpcmd", syntax = {".helpcmd", ".helpcmd <command>"}, description = "Get info on commands or a list of commands.")
public class CommandHelp extends Command {

    @Override
    public void registerCommand() {
        dispatcher.register(literal(this.name).executes(this).then(argument("command", CommandArgumentType.command()).executes(context -> {
            Command command = CommandArgumentType.getCommand(context, "command");
            ChatHelper.INSTANCE.addClientMessage("\2479Command: \247f" + command.getName());
            StringBuilder aliasString = new StringBuilder(command.getName());
            for (String s : command.getAlias())
                aliasString.append("\2477, \247f").append(s);
            ChatHelper.INSTANCE.addClientMessage("\2479Aliases: \247f" + aliasString);
            ChatHelper.INSTANCE.addClientMessage("\2479Description: \247f" + command.getDescription());
            if (command.getSyntax().isEmpty()) {
                ChatHelper.INSTANCE.addClientMessage("\2479Syntax: \247f" + CommandManagerJex.INSTANCE.getPrefix() + command.getName());
            }else {
                ChatHelper.INSTANCE.addClientMessage("\2479Syntax: \247f");
                for (String s : command.getSyntax())
                    ChatHelper.INSTANCE.addClientMessage(s.replace(".", CommandManagerJex.INSTANCE.getPrefix()));
            }
            return 1;
        })));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        CommandManagerJex.INSTANCE.getCommands().forEach(command1 -> {
            ChatHelper.INSTANCE.addClientMessage(command1.getName());
        });
        ChatHelper.INSTANCE.addClientMessage("For more details use " + CommandManagerJex.INSTANCE.getPrefix() +"help <command>");
        return 1;
    }
}
