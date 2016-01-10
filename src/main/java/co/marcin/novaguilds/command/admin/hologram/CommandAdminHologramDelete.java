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

package co.marcin.novaguilds.command.admin.hologram;

import co.marcin.novaguilds.basic.NovaHologram;
import co.marcin.novaguilds.enums.Command;
import co.marcin.novaguilds.enums.Message;
import co.marcin.novaguilds.interfaces.Executor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class CommandAdminHologramDelete implements Executor.ReversedAdminHologram {
	private final Command command = Command.ADMIN_HOLOGRAM_DELETE;
	private NovaHologram hologram;

	public CommandAdminHologramDelete() {
		plugin.getCommandManager().registerExecutor(command, this);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		hologram.delete();

		Map<String, String> vars = new HashMap<>();
		vars.put("NAME", hologram.getName());
		Message.CHAT_ADMIN_HOLOGRAM_DELETE_SUCCESS.vars(vars).send(sender);
	}

	@Override
	public void hologram(NovaHologram hologram) {
		this.hologram = hologram;
	}

	@Override
	public Command getCommand() {
		return command;
	}
}

