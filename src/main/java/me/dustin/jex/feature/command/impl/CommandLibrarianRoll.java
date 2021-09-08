package me.dustin.jex.feature.command.impl;

import io.netty.util.internal.StringUtil;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.mod.impl.misc.AutoLibrarianRoll;
import me.dustin.jex.helper.misc.ChatHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.StringUtils;

@Cmd(name = "LibrarianRoll", alias = {"lr"}, syntax = ".librarianroll <enchantment> <level>", description = "Set the enchantment for AutoLibrarianRoll mod")
public class CommandLibrarianRoll extends Command {
    @Override
    public void runCommand(String command, String[] args) {
        try {
            String enchantName = args[1];
            int enchantLevel = Integer.parseInt(args[2]);

            AutoLibrarianRoll.enchantment = Registry.ENCHANTMENT.get(new Identifier(enchantName));
            AutoLibrarianRoll.enchantmentLevel = enchantLevel;
            ChatHelper.INSTANCE.addClientMessage("LibrarianRoll enchantment set to " + StringUtils.capitalize(enchantName) + " lvl " + enchantLevel);
        } catch (Exception e) {
            giveSyntaxMessage();
        }
    }
}
