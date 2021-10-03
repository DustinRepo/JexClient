package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.command.core.arguments.EnchantmentArgumentType;
import me.dustin.jex.feature.mod.impl.misc.AutoLibrarianRoll;
import me.dustin.jex.helper.misc.ChatHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;

@Cmd(name = "librarianroll", alias = {"lr"}, syntax = ".librarianroll <clear/enchantment> <level>", description = "Set the enchantment for AutoLibrarianRoll mod")
public class CommandLibrarianRoll extends Command {

    @Override
    public void registerCommand() {
        CommandNode<FabricClientCommandSource> node = dispatcher.register(literal(this.name).then(literal("clear").executes(context -> {
            AutoLibrarianRoll.enchantment = null;
            AutoLibrarianRoll.enchantmentLevel = 0;

            ChatHelper.INSTANCE.addClientMessage("LibrarianRoll enchantment cleared");
            return 1;
        })).then(argument("enchantment", EnchantmentArgumentType.enchantment()).then(argument("level", IntegerArgumentType.integer()).executes(this))));
        dispatcher.register(literal("lr").redirect(node));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {

        AutoLibrarianRoll.enchantment = EnchantmentArgumentType.getEnchantment(context,"enchantment");
        AutoLibrarianRoll.enchantmentLevel = IntegerArgumentType.getInteger(context, "level");
        String enchantName = context.getInput().split(" ")[1];

        ChatHelper.INSTANCE.addClientMessage("LibrarianRoll enchantment set to \247b" + enchantName + " \2477lvl \247b" + AutoLibrarianRoll.enchantmentLevel);
        return 1;
    }
}
