package co.marcin.novaguilds.command;

import co.marcin.novaguilds.NovaGuilds;
import co.marcin.novaguilds.basic.NovaGuild;
import co.marcin.novaguilds.enums.Commands;
import co.marcin.novaguilds.enums.Message;
import co.marcin.novaguilds.util.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class CommandAdminGuild implements CommandExecutor {
	private final NovaGuilds plugin;
	
	public CommandAdminGuild(NovaGuilds pl) {
		plugin = pl;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String[] newArgs = StringUtils.parseArgs(args, 1); //nga g <guild> [command args]
		String[] newArgs2 = StringUtils.parseArgs(args, 2); //nga g <guild> command [args]

		List<String> noguildcmds = new ArrayList<>();
		noguildcmds.add("list");
		noguildcmds.add("kick");
		noguildcmds.add("promote");
		noguildcmds.add("purge");
		noguildcmds.add("inactive");

		if(sender.hasPermission("novaguilds.admin.guild.access")) {
			//command list
			if(args.length == 0) {
				Message.CHAT_COMMANDS_ADMIN_GUILD_HEADER.send(sender);

				for(String cItem : Message.CHAT_COMMANDS_ADMIN_GUILD_ITEMS.getList()) {
					sender.sendMessage(StringUtils.fixColors(cItem));
				}

				return true;
			}

			String subCmd = args[0];
			NovaGuild guild = null;
			if(!noguildcmds.contains(subCmd)) {
				guild = plugin.getGuildManager().getGuildFind(args[0]);

				if(guild == null) {
					Message.CHAT_GUILD_COULDNOTFIND.send(sender);
					return true;
				}

				if(args.length > 1) {
					subCmd = args[1];
				}
			}

			if(guild != null || noguildcmds.contains(subCmd)) {
				switch(subCmd.toLowerCase()) {
					case "tp":
					case "teleport":
						new CommandAdminGuildTeleport(plugin,guild).onCommand(sender, cmd, label, newArgs2);
						break;
					case "abandon":
						new CommandAdminGuildAbandon(plugin,guild).onCommand(sender, cmd, label, newArgs2);
						break;
					case "setname":
					case "name":
						new CommandAdminGuildSetName(plugin,guild).onCommand(sender, cmd, label, newArgs2);
						break;
					case "settag":
					case "tag":
						new CommandAdminGuildSetTag(plugin,guild).onCommand(sender, cmd, label, newArgs2);
						break;
					case "setpoints":
					case "points":
						new CommandAdminGuildSetPoints(plugin,guild).onCommand(sender, cmd, label, newArgs2);
						break;
					case "invite":
						new CommandAdminGuildInvite(plugin,guild).onCommand(sender, cmd, label, newArgs2);
						break;
					case "pay":
						new CommandAdminGuildBankPay(plugin,guild).onCommand(sender, cmd, label, newArgs2);
						break;
					case "withdraw":
						new CommandAdminGuildBankWithdraw(plugin,guild).onCommand(sender, cmd, label, newArgs2);
						break;
					case "timerest":
						new CommandAdminGuildSetTimerest(plugin,guild).onCommand(sender, cmd, label, newArgs2);
						break;
					case "liveregentime":
						new CommandAdminGuildSetLiveRegenerationTime(plugin,guild).onCommand(sender, cmd, label, newArgs2);
						break;
					case "lives":
						new CommandAdminGuildSetLives(plugin,guild).onCommand(sender, cmd, label, newArgs2);
						break;
					case "purge":
						new CommandAdminGuildPurge(plugin).onCommand(sender, cmd, label, newArgs2);
						break;
					case "list":
						plugin.getCommandManager().getExecutor(Commands.ADMIN_GUILD_LIST).execute(sender, newArgs);
						break;
					case "inactive":
						new CommandAdminGuildInactive(plugin).onCommand(sender, cmd, label, newArgs);
						break;
					case "kick":
						new CommandAdminGuildKick(plugin).onCommand(sender, cmd, label, newArgs);
						break;
					case "promote":
					case "leader":
						new CommandAdminGuildSetLeader(plugin).onCommand(sender, cmd, label, newArgs);
						break;
					default:
						Message.CHAT_UNKNOWNCMD.send(sender);
						break;
				}
			}
			else {
				Message.CHAT_GUILD_NAMENOTEXIST.send(sender);
			}
		}
		else {
			Message.CHAT_NOPERMISSIONS.send(sender);
		}

		return true;
	}
}
