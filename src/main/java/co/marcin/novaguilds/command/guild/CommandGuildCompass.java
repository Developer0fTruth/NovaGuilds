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

import co.marcin.novaguilds.basic.NovaPlayer;
import co.marcin.novaguilds.enums.Command;
import co.marcin.novaguilds.enums.Message;
import co.marcin.novaguilds.interfaces.Executor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandGuildCompass implements Executor {
	private final Command command = Command.GUILD_COMPASS;

	public CommandGuildCompass() {
		plugin.getCommandManager().registerExecutor(command, this);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		NovaPlayer nPlayer = plugin.getPlayerManager().getPlayer(sender);

		if(!nPlayer.hasGuild()) {
			Message.CHAT_GUILD_NOTINGUILD.send(sender);
			return;
		}

		if(nPlayer.isCompassPointingGuild()) { //disable
			nPlayer.setCompassPointingGuild(false);
			player.setCompassTarget(player.getWorld().getSpawnLocation());
			Message.CHAT_GUILD_COMPASSTARGET_OFF.send(sender);
		}
		else { //enable
			nPlayer.setCompassPointingGuild(true);
			player.setCompassTarget(nPlayer.getGuild().getSpawnPoint());
			Message.CHAT_GUILD_COMPASSTARGET_ON.send(sender);
		}
	}

	@Override
	public Command getCommand() {
		return command;
	}
}
