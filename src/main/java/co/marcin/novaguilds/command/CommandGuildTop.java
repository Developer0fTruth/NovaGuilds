package co.marcin.novaguilds.command;

import co.marcin.novaguilds.NovaGuilds;
import co.marcin.novaguilds.basic.NovaGuild;
import co.marcin.novaguilds.enums.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.*;

public class CommandGuildTop implements CommandExecutor {
	private final NovaGuilds plugin;

	public CommandGuildTop(NovaGuilds pl) {
		plugin = pl;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("novaguilds.guild.top")) {
			Message.CHAT_NOPERMISSIONS.send(sender);
			return true;
		}

		Collection<NovaGuild> guilds = plugin.getGuildManager().getGuilds();

		if(guilds.isEmpty()) {
			Message.CHAT_GUILD_NOGUILDS.send(sender);
			return true;
		}

		int limit = Integer.parseInt(Message.HOLOGRAPHICDISPLAYS_TOPGUILDS_TOPROWS.get()); //TODO move to config
		int i=1;

		Message.HOLOGRAPHICDISPLAYS_TOPGUILDS_HEADER.send(sender);

		HashMap<String, String> vars = new HashMap<>();
		for(NovaGuild guild : plugin.getGuildManager().getTopGuildsByPoints(limit)) {
			vars.clear();
			vars.put("GUILDNAME", guild.getName());
			vars.put("N", String.valueOf(i));
			vars.put("POINTS", String.valueOf(guild.getPoints()));
			Message.HOLOGRAPHICDISPLAYS_TOPGUILDS_ROW.title(false).vars(vars).send(sender);
			i++;
		}

		return true;
	}
}
