package me.dustin.jex.command.impl;

import me.dustin.jex.command.core.Command;
import me.dustin.jex.command.core.annotate.Cmd;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.network.MCAPIHelper;

import java.io.*;
import java.net.URL;
import java.util.UUID;

@Cmd(name = "SkinSteal", syntax = ".skinsteal <name>", description = "Download a player's skin by name. Puts into .minecraft/JexClient/skins", alias = {"ss", "skin"})
public class CommandSkinSteal extends Command {
    @Override
    public void runCommand(String command, String[] args) {
        if (args.length < 1) {
            giveSyntaxMessage();
            return;
        }
        String name = args[1];
        ChatHelper.INSTANCE.addClientMessage("Downloading skin of " + name + "...");
        long time = System.currentTimeMillis();
        new Thread(() -> {
            UUID uuid = MCAPIHelper.INSTANCE.getUUIDFromName(name);
            if (uuid == null) {
                ChatHelper.INSTANCE.addClientMessage("UUID returned null. Player may not exist.");
                return;
            }
            String skinUrl = "https://crafatar.com/skins/" + uuid.toString().replace("-", "");
            try {
                InputStream in = new BufferedInputStream(new URL(skinUrl).openStream());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int n = 0;
                while (-1!=(n=in.read(buf)))
                {
                    out.write(buf, 0, n);
                }
                out.close();
                in.close();
                byte[] response = out.toByteArray();
                File skinsFolder = new File(ModFileHelper.INSTANCE.getJexDirectory() + File.separator + "skins");
                if (!skinsFolder.exists())
                    skinsFolder.mkdir();
                File file = new File(skinsFolder,  name + ".png");
                FileOutputStream fos = new FileOutputStream(file.getPath());
                fos.write(response);
                fos.close();
                ChatHelper.INSTANCE.addClientMessage(name + "'s skin downloaded.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
