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

package co.marcin.novaguilds.command.admin.guild;

import co.marcin.novaguilds.basic.NovaGuild;
import co.marcin.novaguilds.enums.AbandonCause;
import co.marcin.novaguilds.enums.Commands;
import co.marcin.novaguilds.enums.Config;
import co.marcin.novaguilds.enums.Message;
import co.marcin.novaguilds.event.GuildAbandonEvent;
import co.marcin.novaguilds.interfaces.Executor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

public class CommandAdminGuildPurge implements Executor {
	private final Commands command;

	public CommandAdminGuildPurge(Commands command) {
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

		if(!Config.DEBUG.getBoolean()) {
			sender.sendMessage("This command is not available.");
			return;
		}

		if(plugin.getGuildManager().getGuilds().isEmpty()) {
			Message.CHAT_GUILD_NOGUILDS.send(sender);
			return;
		}

		for(NovaGuild guild : plugin.getGuildManager().getGuilds()) {
			//fire event
			GuildAbandonEvent guildAbandonEvent = new GuildAbandonEvent(guild, AbandonCause.ADMIN_ALL);
			plugin.getServer().getPluginManager().callEvent(guildAbandonEvent);

			//if event is not cancelled
			if(!guildAbandonEvent.isCancelled()) {
				guild.getLeader().cancelToolProgress();
				plugin.getHologramManager().refreshTopHolograms();

				//delete guild
				plugin.getGuildManager().delete(guild);

				HashMap<String, String> vars = new HashMap<>();
				vars.put("PLAYERNAME", sender.getName());
				vars.put("GUILDNAME", guild.getName());
				Message.BROADCAST_ADMIN_GUILD_ABANDON.vars(vars).broadcast();
			}
		}
	}

}
