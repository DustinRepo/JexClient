package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.command.core.arguments.MessageArgumentType;
import me.dustin.jex.helper.baritone.BaritoneHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;

@Cmd(name = "baritone", description = "Access Baritone commands", syntax = ".b <command>", alias = {"b", "bar"})
public class CommandBaritone extends Command {

    @Override
    public void registerCommand() {
        CommandNode<FabricClientCommandSource> node = dispatcher.register(literal(this.name).requires(source-> BaritoneHelper.INSTANCE.baritoneExists()).then(argument("message", MessageArgumentType.message()).executes(this)));
        dispatcher.register(literal("b").redirect(node));
        dispatcher.register(literal("bar").redirect(node));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        String cmd = MessageArgumentType.getMessage(context, "message").getString();
        if (BaritoneHelper.INSTANCE.baritoneExists()) {
            ChatHelper.INSTANCE.addClientMessage("Sending command to Baritone.");
            BaritoneHelper.INSTANCE.sendCommand(cmd);
        } else {
            ChatHelper.INSTANCE.addClientMessage("Baritone not found. Please make sure to place it in your mods folder.");
        }
        return 1;
    }
}

