package me.dustin.jex.feature.command.impl;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.events.EventManager;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ServerPacketFilter;
import me.dustin.jex.feature.command.core.Command;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import org.apache.commons.lang3.StringUtils;

import com.mojang.brigadier.suggestion.Suggestions;

import me.dustin.jex.JexClient;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.network.NetworkHelper;

@Cmd(name = "plugins", syntax = ".plugins", description = "List all plugins used on a server", alias = "pl")
public class CommandPlugins extends Command {

	@EventPointer
	private final EventListener<EventPacketReceive> eventPacketReceiveEventListener = new EventListener<>(event -> {
		CommandSuggestionsS2CPacket commandSuggestionsS2CPacket = (CommandSuggestionsS2CPacket)event.getPacket();
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
		EventManager.unregister(this);
	}, new ServerPacketFilter(EventPacketReceive.Mode.PRE, CommandSuggestionsS2CPacket.class));


	@Override
	public void registerCommand() {
		dispatcher.register(literal(this.name).executes(this));
	}

	@Override
	public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ChatHelper.INSTANCE.addClientMessage("Grabbing server plugins");
		EventManager.register(this);
		NetworkHelper.INSTANCE.sendPacket(new RequestCommandCompletionsC2SPacket(0, "/"));
		return 1;
	}
}
