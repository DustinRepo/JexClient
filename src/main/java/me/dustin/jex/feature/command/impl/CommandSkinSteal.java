package me.dustin.jex.feature.command.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.command.core.arguments.PlayerNameArgumentType;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.network.MCAPIHelper;
import me.dustin.jex.helper.network.WebHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Cmd(name = "skinsteal", syntax = ".skinsteal <name>", description = "Download a player's skin by name. Puts into .minecraft/JexClient/skins", alias = {"ss", "skin"})
public class CommandSkinSteal extends Command {

    @Override
    public void registerCommand() {
        CommandNode<FabricClientCommandSource> node = dispatcher.register(literal(this.name).then(argument("player", PlayerNameArgumentType.playerName()).executes(this)));
        dispatcher.register(literal("ss").redirect(node));
        dispatcher.register(literal("skin").redirect(node));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
            String name = PlayerNameArgumentType.getPlayerName(context, "player");
            ChatHelper.INSTANCE.addClientMessage("Downloading skin of " + name + "...");
            new Thread(() -> {
                UUID uuid = MCAPIHelper.INSTANCE.getUUIDFromName(name);
                if (uuid == null) {
                    ChatHelper.INSTANCE.addClientMessage("UUID returned null. Player may not exist.");
                    return;
                }
                try {
                    //going to explain what happens so I don't forget
                    //request their minecraft profile, all so we can get a base64 encoded string that contains ANOTHER json that then has the skin URL
                    String PROFILE_REQUEST_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s";
                    String profileResponse = null;
                    profileResponse = WebHelper.INSTANCE.readURL(new URL(String.format(PROFILE_REQUEST_URL, uuid.toString().replace("-", ""))));

                    JsonObject object = JsonHelper.INSTANCE.prettyGson.fromJson(profileResponse, JsonObject.class);
                    //Get the properties array which has what we need
                    JsonArray array = object.getAsJsonArray("properties");
                    JsonObject property = array.get(0).getAsJsonObject();
                    //value is what we grab but it's encoded so we have to decode it
                    String base64String = property.get("value").getAsString();
                    byte[] bs = Base64.decodeBase64(base64String);
                    //Convert the response to json and pull the skin url from there
                    String secondResponse = new String(bs, StandardCharsets.UTF_8);
                    JsonObject finalResponseObject = JsonHelper.INSTANCE.prettyGson.fromJson(secondResponse, JsonObject.class);
                    JsonObject texturesObject = finalResponseObject.getAsJsonObject("textures");
                    JsonObject skinObj = texturesObject.getAsJsonObject("SKIN");
                    String skinURL = skinObj.get("url").getAsString();

                    InputStream in = new BufferedInputStream(new URL(skinURL).openStream());
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int n = 0;
                    while (-1 != (n = in.read(buf))) {
                        out.write(buf, 0, n);
                    }
                    out.close();
                    in.close();
                    byte[] response = out.toByteArray();
                    File skinsFolder = new File(ModFileHelper.INSTANCE.getJexDirectory() + File.separator + "skins");
                    if (!skinsFolder.exists())
                        skinsFolder.mkdir();
                    File file = new File(skinsFolder, name + ".png");
                    FileOutputStream fos = new FileOutputStream(file.getPath());
                    fos.write(response);
                    fos.close();
                    ChatHelper.INSTANCE.addClientMessage(StringUtils.capitalize(name) + "'s skin downloaded.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        return 1;
    }
}
