package co.marcin.NovaGuilds.Commands;

import co.marcin.NovaGuilds.NovaGuilds;
import co.marcin.NovaGuilds.NovaPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

public class CommandAdminRegionBypass implements CommandExecutor {
	public NovaGuilds plugin;

	public CommandAdminRegionBypass(NovaGuilds pl) {
		plugin = pl;
	}

	/*
	* Changing bypass
	* no args - for sender
	* args[1] - for specified player
	* */
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length==1) {
			if(sender.hasPermission("novaguilds.admin.region.bypass")) {
				NovaPlayer nPlayer = plugin.getPlayerManager().getPlayerByName(sender.getName());

				nPlayer.toggleBypass();
				plugin.getPlayerManager().updateLocalPlayer(nPlayer);
				HashMap<String,String> vars = new HashMap<String,String>();
				vars.put("BYPASS",nPlayer.getBypass()+"");
				plugin.sendMessagesMsg(sender,"chat.admin.rgbypass.toggled",vars);
			}
			else {
				plugin.sendMessagesMsg(sender,"chat.nopermissions");
			}
		}
		else { //for other
			if(sender.hasPermission("novaguilds.admin.region.bypass.other")) {
				NovaPlayer nPlayer = plugin.getPlayerManager().getPlayerByName(args[1]);

				if(nPlayer == null) {
					plugin.sendMessagesMsg(sender,"chat.player.notexists");
					return true;
				}

				nPlayer.toggleBypass();
				plugin.getPlayerManager().updateLocalPlayer(nPlayer);
				HashMap<String,String> vars = new HashMap<String,String>();
				vars.put("PLAYER",nPlayer.getName());
				vars.put("BYPASS",nPlayer.getBypass()+"");
				plugin.sendMessagesMsg(sender,"chat.admin.rgbypass.toggledother",vars);
			}
			else {
				plugin.sendMessagesMsg(sender,"chat.nopermissions");
			}
		}
		return true;
	}
}
