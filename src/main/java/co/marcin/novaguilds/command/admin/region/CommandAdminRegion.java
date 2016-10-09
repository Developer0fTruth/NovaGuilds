/*
 *     NovaGuilds - Bukkit plugin
 *     Copyright (C) 2016 Marcin (CTRL) Wieczorek
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

package co.marcin.novaguilds.command.admin.region;

import co.marcin.novaguilds.api.basic.CommandWrapper;
import co.marcin.novaguilds.api.basic.NovaGuild;
import co.marcin.novaguilds.api.basic.NovaRegion;
import co.marcin.novaguilds.command.abstractexecutor.AbstractCommandExecutor;
import co.marcin.novaguilds.enums.Command;
import co.marcin.novaguilds.enums.Message;
import co.marcin.novaguilds.manager.GuildManager;
import co.marcin.novaguilds.util.NumberUtils;
import co.marcin.novaguilds.util.StringUtils;
import org.bukkit.command.CommandSender;

public class CommandAdminRegion extends AbstractCommandExecutor {
	public CommandAdminRegion() {
		commandsMap.put("bypass",   Command.ADMIN_REGION_BYPASS);
		commandsMap.put("bp",       Command.ADMIN_REGION_BYPASS);
		commandsMap.put("spectate", Command.ADMIN_REGION_SPECTATE);
		commandsMap.put("delete",   Command.ADMIN_REGION_DELETE);
		commandsMap.put("del",      Command.ADMIN_REGION_DELETE);
		commandsMap.put("list",     Command.ADMIN_REGION_LIST);
		commandsMap.put("teleport", Command.ADMIN_REGION_TELEPORT);
		commandsMap.put("tp",       Command.ADMIN_REGION_TELEPORT);
	}

	@Override
	public void execute(CommandSender sender, String[] args) throws Exception {
		if(args.length == 0) {
			Message.CHAT_COMMANDS_ADMIN_REGION_HEADER.send(sender);
			Message.send(Message.CHAT_USAGE_NGA_REGION_ACCESS.getNeighbours(), sender);
			return;
		}

		CommandWrapper subCommand = getCommandsMap().get(args[args.length >= 2 && getCommandsMap().get(args[1]) != null && getCommandsMap().get(args[1]).isReversed() ? 1 : 0]);

		if(subCommand == null) {
			Message.CHAT_UNKNOWNCMD.send(sender);
			return;
		}

		if(subCommand.isReversed()) {
			NovaGuild guild = GuildManager.getGuildFind(args[0]);

			if(guild == null) {
				Message.CHAT_GUILD_COULDNOTFIND.send(sender);
				return;
			}

			if(!guild.hasRegion()) {
				Message.CHAT_GUILD_HASNOREGION.send(sender);
				return;
			}

			if(args.length >= 3) {
				String indexString = args[2];

				if(!NumberUtils.isNumeric(indexString)) {
					Message.CHAT_ENTERINTEGER.send(sender);
					return;
				}

				int index = Integer.parseInt(indexString);

				if(index <= 0) {
					Message.CHAT_BASIC_NEGATIVENUMBER.send(sender);
					return;
				}

				NovaRegion region = guild.getRegion(index);

				if(region == null) {
					Message.CHAT_REGION_NOTFOUND.send(sender);
					return;
				}

				subCommand.executorVariable(region);
			}
		}

		subCommand.execute(sender, StringUtils.parseArgs(args, !subCommand.isReversed() ? 1 : 3));
	}
}
