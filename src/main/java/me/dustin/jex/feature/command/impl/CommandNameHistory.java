package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.command.core.arguments.PlayerNameArgumentType;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.network.MCAPIHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Cmd(name = "namehistory", description = "See the name history of any player", syntax = ".namehistory <name>")
public class CommandNameHistory extends Command {
    @Override
    public void registerCommand() {
        dispatcher.register(literal(this.name).then(argument("name", PlayerNameArgumentType.playerName()).executes(this)));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        String playerName = PlayerNameArgumentType.getPlayerName(context, "name");
        ChatHelper.INSTANCE.addClientMessage("Getting name history of " + playerName);
        new Thread(() -> {
            ChatHelper.INSTANCE.addClientMessage("Grabbing UUID from name");
            UUID uuid = MCAPIHelper.INSTANCE.getUUIDFromName(playerName);
            if (uuid == null) {
                ChatHelper.INSTANCE.addClientMessage("Could not find UUID for name");
                return;
            }
            Map<String, Long> sort = sortByValue(MCAPIHelper.INSTANCE.getNameHistory(uuid));
            sort.forEach((s, aLong) -> {
                if (aLong == -1) {
                    ChatHelper.INSTANCE.addRawMessage("\247b" + s + " \2477first name.");
                } else {
                    DateFormat df = new SimpleDateFormat("MM/dd/yy HH:mm");
                    String formattedDate = df.format(aLong);
                    String dateString = formattedDate.split(" ")[0] + " " + formattedDate.split(" ")[1];
                    ChatHelper.INSTANCE.addRawMessage("\247b" + s + " \2477changed to at: \247a" + dateString);
                }
            });
        }).start();
        return 1;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
