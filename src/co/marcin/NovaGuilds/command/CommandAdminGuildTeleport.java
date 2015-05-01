package co.marcin.NovaGuilds.command;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.marcin.NovaGuilds.basic.NovaGuild;
import co.marcin.NovaGuilds.NovaGuilds;

public class CommandAdminGuildTeleport implements CommandExecutor {
	private final NovaGuilds plugin;
	
	public CommandAdminGuildTeleport(NovaGuilds pl) {
		plugin = pl;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		//args:
		// 0 - guildname
		// 1 - other player
		
		if(!(sender instanceof Player)) {
			plugin.info("You cannot tp to a guild from the console!");
			return true;
		}
		
		if(sender.hasPermission("NovaGuilds.admin.guild.tp")) {
			if(args.length>0) {
				String guildname = args[0];
				
				NovaGuild guild = plugin.getGuildManager().getGuildByName(guildname);
				
				if(guild != null) {
					Location home = guild.getSpawnPoint();
					
					if(home != null) {
						Player player = null;

						HashMap<String,String> vars = new HashMap<>();
						vars.put("GUILDNAME",guild.getName());
						
						if(args.length==2) {
							String playername = args[1];
							
							if(plugin.getPlayerManager().exists(playername)) {
								player = plugin.getServer().getPlayer(playername);
								if(player == null) {
									plugin.sendMessagesMsg(sender,"chat.player.notonline");
									return true;
								}
							}
							else {
								plugin.sendMessagesMsg(sender,"chat.player.notexists");
								return true;
							}
						}
						else {
							player = plugin.senderToPlayer(sender);
						}
						
						if(player != null) {
							player.teleport(home);
							vars.put("PLAYERNAME",player.getName());
							plugin.sendMessagesMsg(sender, "chat.admin.guild.teleportedother", vars);
						}
						else {
							plugin.sendMessagesMsg(sender, "chat.admin.guild.teleported", vars);
						}
					}
				}
				else {
					plugin.sendMessagesMsg(sender,"chat.guild.namenotexist");
				}
			}
			else {
				plugin.sendMessagesMsg(sender,"chat.usage.nga.guild.tp");
			}
		}
		else {
			plugin.sendMessagesMsg(sender,"chat.nopermissions");
		}
		return true;
	}
}
