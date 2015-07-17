package co.marcin.novaguilds.command;

import co.marcin.novaguilds.NovaGuilds;
import co.marcin.novaguilds.basic.NovaPlayer;
import co.marcin.novaguilds.enums.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

public class CommandAdminRegionBypass implements CommandExecutor {
	private final NovaGuilds plugin;

	public CommandAdminRegionBypass(NovaGuilds pl) {
		plugin = pl;
	}

	/*
	* Changing bypass
	* no args - for sender
	* args[1] - for specified player
	* */
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length==0) {
			if(!sender.hasPermission("novaguilds.admin.region.bypass")) {
				plugin.getMessageManager().sendNoPermissionsMessage(sender);
				return true;
			}

			NovaPlayer nPlayer = plugin.getPlayerManager().getPlayer(sender);

			nPlayer.toggleBypass();
			HashMap<String,String> vars = new HashMap<>();
			vars.put("BYPASS",nPlayer.getBypass()+"");
			Message.CHAT_ADMIN_REGION_BYPASS_TOGGLED_SELF.vars(vars).send(sender);
		}
		else { //for other
			if(sender.hasPermission("novaguilds.admin.region.bypass.other")) {
				plugin.getMessageManager().sendNoPermissionsMessage(sender);
				return true;
			}

			NovaPlayer nPlayer = plugin.getPlayerManager().getPlayer(args[0]);

			if(nPlayer == null) {
				plugin.getMessageManager().sendMessagesMsg(sender,"chat.player.notexists");
				return true;
			}

			nPlayer.toggleBypass();
			HashMap<String,String> vars = new HashMap<>();
			vars.put("PLAYER",nPlayer.getName());
			vars.put("BYPASS",nPlayer.getBypass() ? plugin.getMessageManager().getMessagesString("chat.basic.on") : plugin.getMessageManager().getMessagesString("chat.basic.off"));

			if(nPlayer.isOnline()) {
				Message.CHAT_ADMIN_REGION_BYPASS_NOTIFYOTHER.vars(vars).send(sender);
			}

			Message.CHAT_ADMIN_REGION_BYPASS_TOGGLED_OTHER.vars(vars).send(sender);
		}
		return true;
	}
}
