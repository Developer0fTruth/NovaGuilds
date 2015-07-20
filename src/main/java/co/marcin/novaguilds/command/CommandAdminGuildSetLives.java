package co.marcin.novaguilds.command;

import co.marcin.novaguilds.NovaGuilds;
import co.marcin.novaguilds.basic.NovaGuild;
import co.marcin.novaguilds.enums.Message;
import co.marcin.novaguilds.util.NumberUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandAdminGuildSetLives implements CommandExecutor {
	private final NovaGuilds plugin;
	private final NovaGuild guild;

	public CommandAdminGuildSetLives(NovaGuilds pl, NovaGuild guild) {
		plugin = pl;
		this.guild = guild;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("novaguilds.admin.guild.lives")) {
			Message.CHAT_NOPERMISSIONS.send(sender);
			return true;
		}

		if(args.length == 0) {
			plugin.getMessageManager().sendUsageMessage(sender,"nga.guild.lives");
			return true;
		}

		if(!NumberUtils.isNumeric(args[0])) {
			Message.CHAT_ENTERINTEGER.send(sender);
			return true;
		}

		int lives = Integer.parseInt(args[0]);

		if(lives < 0) {
			Message.CHAT_BASIC_NEGATIVENUMBER.send(sender);
			return true;
		}

		guild.setLives(lives);
		Message.CHAT_ADMIN_GUILD_SET_LIVES.send(sender);
		return true;
	}
}
