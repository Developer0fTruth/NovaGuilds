package co.marcin.NovaGuilds.command;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import co.marcin.NovaGuilds.basic.NovaGuild;
import co.marcin.NovaGuilds.NovaGuilds;
import co.marcin.NovaGuilds.basic.NovaPlayer;

public class CommandGuildKick  implements CommandExecutor {
	public final NovaGuilds plugin;
	
	public CommandGuildKick(NovaGuilds novaGuilds) {
		plugin = novaGuilds;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("NovaGuilds.guild.kick")) {

			return true;
		}
		
		NovaPlayer nPlayer = plugin.getPlayerManager().getPlayerBySender(sender);
		
		if(!nPlayer.hasGuild()) {
			
			return true;
		}
		
		NovaGuild guild = plugin.getGuildManager().getGuildByPlayer(nPlayer);
		
		if(!guild.getLeaderName().equalsIgnoreCase(sender.getName())) {

			return true;
		}
		
		if(args.length == 0) {

			return true;
		}
		
		NovaPlayer nPlayerKick = plugin.getPlayerManager().getPlayerByName(args[0]);
		
		if(!(nPlayerKick instanceof NovaPlayer)) {

			return true;
		}
		
		if(!nPlayerKick.getGuild().getName().equalsIgnoreCase(guild.getName())) {

			return true;
		}
		
		//all passed
		nPlayerKick.setGuild(null);
		nPlayerKick.setHasGuild(false);
		
		plugin.getPlayerManager().updateLocalPlayer(nPlayerKick);
		
		HashMap<String,String> vars = new HashMap<>();
		vars.put("PLAYERNAME",nPlayerKick.getName());
		vars.put("GUILDNAME",guild.getName());
		plugin.broadcastMessage("broadcast.guild.kicked", vars);
		
		//tab/tag
		plugin.updateTabAll();
		plugin.tagUtils.updateTagAll();
		
		return true;
	}
}
