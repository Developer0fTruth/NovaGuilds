package co.marcin.NovaGuilds.command;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.marcin.NovaGuilds.basic.NovaGuild;
import co.marcin.NovaGuilds.NovaGuilds;
import co.marcin.NovaGuilds.basic.NovaPlayer;

public class CommandGuildLeave implements CommandExecutor {
	public final NovaGuilds plugin;
	
	public CommandGuildLeave(NovaGuilds pl) {
		plugin = pl;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
			plugin.info("Invalid command sender");
			return true;
		}
		
		NovaPlayer nPlayer = plugin.getPlayerManager().getPlayerByName(sender.getName());
		
		if(nPlayer.hasGuild()) {
			NovaGuild guild = nPlayer.getGuild();
			String leaderName = guild.getLeaderName();
			
			if(leaderName.equals(sender.getName())) {
				plugin.sendMessagesMsg(sender,"chat.guild.leave.isleader");
				return true;
			}
			
			nPlayer.setGuild(null);
			nPlayer.setHasGuild(false);
			plugin.getPlayerManager().updatePlayer(nPlayer);
			guild.removePlayer(nPlayer);
			plugin.getGuildManager().saveGuildLocal(guild);
			plugin.sendMessagesMsg(sender,"chat.guild.leave.left");
			
			HashMap<String,String> vars = new HashMap<>();
			vars.put("PLAYER",sender.getName());
			vars.put("GUILDNAME",guild.getName());
			plugin.broadcastMessage("broadcast.guild.left", vars);
			
			plugin.updateTabAll();
			plugin.tagUtils.updateTagAll();
		}
		else {
			plugin.sendMessagesMsg(sender,"chat.guild.notinguild");
		}
		
		return true;
	}
}
