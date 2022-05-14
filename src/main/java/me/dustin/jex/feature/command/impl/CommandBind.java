package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.CommandManagerJex;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.keybind.Keybind;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.KeyboardHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;

@Cmd(name = "bind", syntax = {".bind add <module> <key>", ".bind clear <module>", ".bind list"}, description = "Modify keybinds with a command. List with bind list")
public class CommandBind extends Command {

    @Override
    public void registerCommand() {
        dispatcher.register(literal(this.name).then(literal("list").executes(this)).then(literal("add").then(argument("key", StringArgumentType.string()).then(argument("command", StringArgumentType.string()).executes(context -> {
            int key = KeyboardHelper.INSTANCE.getKeyFromName(StringArgumentType.getString(context, "key"));
            String command = StringArgumentType.getString(context, "command");
            boolean isJexCommand = command.startsWith(CommandManagerJex.INSTANCE.getPrefix());
            Keybind.add(key, isJexCommand ? command.substring(1) : command, isJexCommand);
            ChatHelper.INSTANCE.addClientMessage("Added %s to key: %s".formatted(command, StringArgumentType.getString(context, "key")));
            return 1;
        })))).then(literal("remove").then(argument("command", StringArgumentType.string()).executes(context -> {
            String command = StringArgumentType.getString(context, "command");
            if (command.startsWith(CommandManagerJex.INSTANCE.getPrefix()))
                command = command.substring(1);
            Keybind bind = Keybind.get(command);
            if (bind == null) {
                ChatHelper.INSTANCE.addClientMessage("No bind matching command! If trying to clear all binds from a key, use %sbind clear".formatted(CommandManagerJex.INSTANCE.getPrefix()));
                return 0;
            }
            Keybind.remove(bind);
            ChatHelper.INSTANCE.addClientMessage("Removed keybind");
            return 1;
        }))).then(literal("clear").then(argument("key", StringArgumentType.string()).executes(context -> {
            int key = KeyboardHelper.INSTANCE.getKeyFromName(StringArgumentType.getString(context, "key"));
            Keybind.clear(key);
            ChatHelper.INSTANCE.addClientMessage("Cleared all binds from %s".formatted(StringArgumentType.getString(context, "key")));
            return 1;
        }))));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        ChatHelper.INSTANCE.addClientMessage("Listing keybinds.");
        for (Keybind keybind : Keybind.getKeybinds()) {
            ChatHelper.INSTANCE.addClientMessage("\247b" + keybind.command() + "\247f: \2477" + KeyboardHelper.INSTANCE.getKeyName(keybind.key()));
        }
        return 1;
    }
}
