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
import co.marcin.novaguilds.enums.GuildPermission;
import co.marcin.novaguilds.enums.Message;
import co.marcin.novaguilds.interfaces.Executor;
import co.marcin.novaguilds.util.TabUtils;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

public class CommandGuildOpenInvitation implements Executor {
	private final Command command = Command.GUILD_OPENINVITATION;

	public CommandGuildOpenInvitation() {
		plugin.getCommandManager().registerExecutor(command, this);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		NovaPlayer nPlayer = plugin.getPlayerManager().getPlayer(sender);

		if(!nPlayer.hasGuild()) {
			Message.CHAT_GUILD_NOTINGUILD.send(sender);
			return;
		}

		if(!nPlayer.hasPermission(GuildPermission.OPENINVITATION)) {
			Message.CHAT_GUILD_NOGUILDPERM.send(sender);
			return;
		}

		final boolean status = !nPlayer.getGuild().isOpenInvitation();
		nPlayer.getGuild().setOpenInvitation(status);

		Message.CHAT_GUILD_OPENINVITATION.vars(new HashMap<String, String>() {{
			put("STATUS", Message.getOnOff(status));
		}}).send(sender);

		TabUtils.refresh(nPlayer.getGuild());
	}

	@Override
	public Command getCommand() {
		return command;
	}
}
