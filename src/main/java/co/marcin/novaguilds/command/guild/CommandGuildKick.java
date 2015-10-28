/*
 *     NovaGuilds - Bukkit plugin
 *     Copyright (C) 2015 Marcin (CTRL) Wieczorek
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package co.marcin.novaguilds.command.guild;

import co.marcin.novaguilds.basic.NovaGuild;
import co.marcin.novaguilds.basic.NovaPlayer;
import co.marcin.novaguilds.enums.Commands;
import co.marcin.novaguilds.enums.Message;
import co.marcin.novaguilds.interfaces.Executor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

public class CommandGuildKick implements Executor {
	private final Commands command;

	public CommandGuildKick(Commands command) {
		this.command = command;
		plugin.getCommandManager().registerExecutor(command, this);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!command.hasPermission(sender)) {
			Message.CHAT_NOPERMISSIONS.send(sender);
			return;
		}

		if(!command.allowedSender(sender)) {
			Message.CHAT_CMDFROMCONSOLE.send(sender);
			return;
		}
				
		NovaPlayer nPlayer = plugin.getPlayerManager().getPlayer(sender);
		
		if(!nPlayer.hasGuild()) {
			Message.CHAT_GUILD_NOTINGUILD.send(sender);
			return;
		}
		
		NovaGuild guild = nPlayer.getGuild();
		
		if(!guild.getLeader().getName().equalsIgnoreCase(sender.getName())) {
			Message.CHAT_GUILD_NOTLEADER.send(sender);
			return;
		}
		
		if(args.length == 0) {
			Message.CHAT_PLAYER_ENTERNAME.send(sender);
			return;
		}
		
		NovaPlayer nPlayerKick = plugin.getPlayerManager().getPlayer(args[0]);
		
		if(nPlayerKick == null) {
			Message.CHAT_PLAYER_NOTEXISTS.send(sender);
			return;
		}

		if(!nPlayerKick.hasGuild()) {
			Message.CHAT_PLAYER_HASNOGUILD.send(sender);
			return;
		}
		
		if(!nPlayerKick.getGuild().getName().equalsIgnoreCase(guild.getName())) {
			Message.CHAT_PLAYER_NOTINYOURGUILD.send(sender);
			return;
		}

		if(nPlayer.getName().equalsIgnoreCase(nPlayerKick.getName())) {
			Message.CHAT_GUILD_KICKYOURSELF.send(sender);
			return;
		}
		
		//all passed
		nPlayerKick.setGuild(null);

		nPlayer.getGuild().removePlayer(nPlayerKick);

		if(nPlayerKick.isOnline()) {
			guild.hideVaultHologram(nPlayerKick.getPlayer());
		}
		
		HashMap<String,String> vars = new HashMap<>();
		vars.put("PLAYERNAME",nPlayerKick.getName());
		vars.put("GUILDNAME",guild.getName());
		Message.BROADCAST_GUILD_KICKED.vars(vars).broadcast();
		
		//tab/tag
		plugin.tagUtils.refreshAll();
	}
}
