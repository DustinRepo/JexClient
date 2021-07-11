package me.dustin.jex.feature.command.impl;


import me.dustin.jex.feature.command.CommandManager;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.ChatHelper;

@Cmd(name = "Help", syntax = {".help", ".help <command>"}, description = "Get info on commands or a list of commands.")
public class CommandHelp extends Command {

    @Override
    public void runCommand(String command, String[] args) {
        try {
            Command command1 = CommandManager.INSTANCE.getCommand(args[1]);
            if (command1 != null) {
                ChatHelper.INSTANCE.addClientMessage("\2479Command: \247f" + command1.getName());
                String aliasString = command1.getName();
                for (String s : command1.getAlias())
                    aliasString += "\2477, \247f" + s;
                ChatHelper.INSTANCE.addClientMessage("\2479Aliases: \247f" + aliasString);
                ChatHelper.INSTANCE.addClientMessage("\2479Description: \247f" + command1.getDescription());
                if (command1.getSyntax().isEmpty()) {
                    ChatHelper.INSTANCE.addClientMessage("\2479Syntax: \247f" + CommandManager.INSTANCE.getPrefix() + command1.getName());
                }else {
                    ChatHelper.INSTANCE.addClientMessage("\2479Syntax: \247f");
                    for (String s : command1.getSyntax())
                        ChatHelper.INSTANCE.addClientMessage(s.replace(".", CommandManager.INSTANCE.getPrefix()));
                }
            } else {
                ChatHelper.INSTANCE.addClientMessage(args[1] + " is not a command.");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            CommandManager.INSTANCE.getCommands().forEach(command1 -> {
                ChatHelper.INSTANCE.addClientMessage(command1.getName());
            });
            ChatHelper.INSTANCE.addClientMessage("For more details use .help <command>");
        }
    }
}
