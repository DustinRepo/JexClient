package me.dustin.jex.feature.command.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.mojang.brigadier.suggestion.Suggestions;

import me.dustin.events.api.EventAPI;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;

@Cmd(name = "Plugins", syntax = ".plugins", description = "List all plugins used on a server", alias = "pl")
public class CommandPlugins extends Command {

	@Override
	public void runCommand(String command, String[] args) {
		ChatHelper.INSTANCE.addClientMessage("Grabbing server plugins");
		EventAPI.getInstance().register(this);
		NetworkHelper.INSTANCE.sendPacket(new RequestCommandCompletionsC2SPacket(0, "/"));
	}

	@EventListener(events = {EventPacketReceive.class})
	private void runMethod(EventPacketReceive eventPacketReceive) {
		if (eventPacketReceive.getPacket() instanceof CommandSuggestionsS2CPacket commandSuggestionsS2CPacket) {
			Suggestions suggestions = commandSuggestionsS2CPacket.getSuggestions();
			List<String> commandsList = new ArrayList<>();
			suggestions.getList().forEach(suggestion -> {
				String command = suggestion.getText();
				String pluginName = command.split(":")[0];
				if (command.contains(":") && !pluginName.equalsIgnoreCase("minecraft") && !pluginName.equalsIgnoreCase("bukkit") && !commandsList.contains(pluginName)) {
					JexClient.INSTANCE.getLogger().info(pluginName);
					commandsList.add(pluginName);
				}
			});
			String message = "Plugins" + " (\247b" + commandsList.size() + "\2477)\247f: \247b" + StringUtils.join(commandsList, "\2477, \247b");
			ChatHelper.INSTANCE.addClientMessage(message);
			while (EventAPI.getInstance().alreadyRegistered(this))
				EventAPI.getInstance().unregister(this);
		}
	}
	
}
