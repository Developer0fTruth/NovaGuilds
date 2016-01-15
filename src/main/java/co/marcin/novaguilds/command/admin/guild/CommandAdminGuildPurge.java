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
import co.marcin.novaguilds.enums.Command;
import co.marcin.novaguilds.enums.Message;
import co.marcin.novaguilds.event.GuildAbandonEvent;
import co.marcin.novaguilds.interfaces.Executor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CommandAdminGuildPurge implements Executor {
	private final Command command = Command.ADMIN_GUILD_PURGE;

	public CommandAdminGuildPurge() {
		plugin.getCommandManager().registerExecutor(command, this);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(plugin.getGuildManager().getGuilds().isEmpty()) {
			Message.CHAT_GUILD_NOGUILDS.send(sender);
			return;
		}

		for(NovaGuild guild : new ArrayList<>(plugin.getGuildManager().getGuilds())) {
			//fire event
			GuildAbandonEvent guildAbandonEvent = new GuildAbandonEvent(guild, AbandonCause.ADMIN_ALL);
			plugin.getServer().getPluginManager().callEvent(guildAbandonEvent);

			//if event is not cancelled
			if(!guildAbandonEvent.isCancelled()) {
				//delete guild
				plugin.getGuildManager().delete(guild);

				Map<String, String> vars = new HashMap<>();
				vars.put("PLAYERNAME", sender.getName());
				vars.put("GUILDNAME", guild.getName());
				Message.BROADCAST_ADMIN_GUILD_ABANDON.vars(vars).broadcast();
			}
		}
	}

	@Override
	public Command getCommand() {
		return command;
	}
}
