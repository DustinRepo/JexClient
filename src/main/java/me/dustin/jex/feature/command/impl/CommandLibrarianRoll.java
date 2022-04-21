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
import net.minecraft.enchantment.Enchantment;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Cmd(name = "librarianroll", alias = {"lr"}, syntax = ".librarianroll <clear/list/add> <enchantment> <level>", description = "Set the enchantment for AutoLibrarianRoll mod")
public class CommandLibrarianRoll extends Command {

    @Override
    public void registerCommand() {
        CommandNode<FabricClientCommandSource> node = dispatcher.register(literal(this.name).then(literal("clear").executes(context -> {
            AutoLibrarianRoll.enchantments.clear();

            ChatHelper.INSTANCE.addClientMessage("LibrarianRoll enchantments cleared");
            return 1;
        })).then(literal("list").executes(context -> {
            ChatHelper.INSTANCE.addClientMessage("LibrarianRoll enchantments: ");

            for (Enchantment enchantment : AutoLibrarianRoll.enchantments.keySet()) {
                StringBuilder sb = new StringBuilder();
                sb.append(" - \247b").append(Text.translatable(enchantment.getTranslationKey()).getString()).append("\247r: ");
                for (int level : AutoLibrarianRoll.enchantments.get(enchantment)) {
                    sb.append("\247b").append(level).append("\247r, ");
                }
                sb.deleteCharAt(sb.length() - 2);
                ChatHelper.INSTANCE.addClientMessage(sb.toString());
            }

            return 1;
        })).then(literal("del").then(argument("enchantment", EnchantmentArgumentType.enchantment()).then(argument("level", IntegerArgumentType.integer()).executes(context -> {
            Enchantment enchantment = EnchantmentArgumentType.getEnchantment(context, "enchantment");
            int level = IntegerArgumentType.getInteger(context, "level");

            if (AutoLibrarianRoll.enchantments.containsKey(enchantment)) {
                ArrayList<Integer> levels = AutoLibrarianRoll.enchantments.get(enchantment);
                levels.remove(levels.get(levels.indexOf(level)));
            }

            ChatHelper.INSTANCE.addClientMessage("\247b" + Text.translatable(enchantment.getTranslationKey()).getString() + " \2477lvl \247b" + level + "\2477 removed from LibrarianRoll");
            return 1;
        })))).then(literal("del").then(argument("enchantment", EnchantmentArgumentType.enchantment()).executes(context -> {
            Enchantment enchantment = EnchantmentArgumentType.getEnchantment(context, "enchantment");
            AutoLibrarianRoll.enchantments.remove(EnchantmentArgumentType.getEnchantment(context, "enchantment"));

            ChatHelper.INSTANCE.addClientMessage("\247b" + Text.translatable(enchantment.getTranslationKey()).getString() + " \2477lvl \247b*\2477 removed from LibrarianRoll");
            return 1;
        }))).then(literal("add").then(argument("enchantment", EnchantmentArgumentType.enchantment()).then(argument("level", IntegerArgumentType.integer()).executes(this)))));
        dispatcher.register(literal("lr").redirect(node));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {

        Enchantment enchantment = EnchantmentArgumentType.getEnchantment(context, "enchantment");
        int level = IntegerArgumentType.getInteger(context, "level");

        if (AutoLibrarianRoll.enchantments.containsKey(enchantment)) {
            AutoLibrarianRoll.enchantments.get(enchantment).add(level);
        } else {
            AutoLibrarianRoll.enchantments.put(enchantment, new ArrayList<>(List.of(level)));
        }

        ChatHelper.INSTANCE.addClientMessage("\247b" + Text.translatable(enchantment.getTranslationKey()).getString() + " \2477lvl \247b" + level + "\2477 added to LibrarianRoll");
        return 1;
    }
}
