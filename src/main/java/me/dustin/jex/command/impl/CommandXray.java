package me.dustin.jex.command.impl;

import me.dustin.jex.command.core.Command;
import me.dustin.jex.command.core.annotate.Cmd;
import me.dustin.jex.file.XrayFile;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.impl.world.Xray;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@Cmd(name = "Xray", description = "Add or remove blocks from Xray", syntax = {".xray add <blockname>", ".xray del <blockname>", ".xray list"})
public class CommandXray extends Command {

    @Override
    public void runCommand(String command, String[] args) {
        try {
            if (args[1].equalsIgnoreCase("list")) {
                Xray.blockList.forEach(block -> {
                    ChatHelper.INSTANCE.addClientMessage(Registry.BLOCK.getId(block).toString());
                });
                return;
            }
            Block block = Registry.BLOCK.get(new Identifier(args[2]));
            if (block == Blocks.AIR) {
                ChatHelper.INSTANCE.addClientMessage("Block not found!");
                return;
            }
            if (isAddString(args[1])) {
                if (args[2] != null) {
                    if (!Xray.blockList.contains(block)) {
                        Xray.blockList.add(block);
                        if (Feature.get(Xray.class).getState()) {
                            Wrapper.INSTANCE.getMinecraft().worldRenderer.reload();
                        }
                        ChatHelper.INSTANCE.addClientMessage("\247b" + block.getName().getString() + "\2477 has been added to Xray.");
                        XrayFile.write();
                    }
                }
            } else if (isDeleteString(args[1])) {
                if (args[2] != null) {
                    if (Xray.blockList.contains(block)) {
                        Xray.blockList.remove(block);
                        if (Feature.get(Xray.class).getState()) {
                            Wrapper.INSTANCE.getMinecraft().worldRenderer.reload();
                        }
                        ChatHelper.INSTANCE.addClientMessage("\247c" + block.getName().getString() + "\2477 has been removed from Xray.");
                        XrayFile.write();
                    }
                }
            }
        } catch (Exception e) {
            giveSyntaxMessage();
        }
    }


}
