package me.dustin.jex.command.impl;

import me.dustin.jex.command.core.Command;
import me.dustin.jex.command.core.annotate.Cmd;
import me.dustin.jex.file.SearchFile;
import me.dustin.jex.gui.minecraft.blocklist.SearchSelectScreen;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.feature.impl.render.Search;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Random;

@Cmd(name = "Search", description = "Add or remove blocks from Search", syntax = {".search add <blockname> <hex color (f62d3e)/random>", ".search del <blockname>", ".search list"})
public class CommandSearch extends Command {

    @Override
    public void runCommand(String command, String[] args) {
        try {
            if (args[1].equalsIgnoreCase("list")) {
                Search.getBlocks().keySet().forEach(block -> {
                    ChatHelper.INSTANCE.addClientMessage(Registry.BLOCK.getId(block).toString());
                });
                return;
            }
            Block block = null;
            try {
                block = Registry.BLOCK.get(new Identifier(args[2]));
                if (block == Blocks.AIR) {
                    ChatHelper.INSTANCE.addClientMessage("Block not found!");
                    return;
                }
            } catch (Exception e) {
                ChatHelper.INSTANCE.addClientMessage("Block not found!");
                e.printStackTrace();
                return;
            }
            if (isAddString(args[1])) {
                Random r = new Random();
                int color = args[3].equalsIgnoreCase("random") ? ColorHelper.INSTANCE.getColorViaHue(ClientMathHelper.INSTANCE.getRandom(270), 1, 1).getRGB() : Render2DHelper.INSTANCE.hex2Rgb("ff" + args[3]).getRGB();
                if (block != null && block != Blocks.AIR) {
                    if (Search.getBlocks().containsKey(block))
                        ChatHelper.INSTANCE.addClientMessage("That block is already in the Search list!");
                    else {
                        Search.getBlocks().put(block, color);
                        ChatHelper.INSTANCE.addClientMessage("\247b" + block.getName().getString() + "\2477 added to Search list");
                        SearchFile.write();
                    }
                }
            } else if (isDeleteString(args[1])) {
                if (block != null && block != Blocks.AIR) {
                    if (!Search.getBlocks().containsKey(block))
                        ChatHelper.INSTANCE.addClientMessage("That block is not in the Search list!");
                    else {
                        Search.getBlocks().remove(block);
                        ChatHelper.INSTANCE.addClientMessage("\247c" + block.getName().getString() + "\2477 deleted from Search list");
                        SearchFile.write();
                    }
                }
            } else {
                giveSyntaxMessage();
            }
        } catch (Exception e) {
            Wrapper.INSTANCE.getMinecraft().openScreen(new SearchSelectScreen());
            giveSyntaxMessage();
        }

    }


}
