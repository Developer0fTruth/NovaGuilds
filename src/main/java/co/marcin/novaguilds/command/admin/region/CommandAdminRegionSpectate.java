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

import co.marcin.novaguilds.api.basic.NovaPlayer;
import co.marcin.novaguilds.command.abstractexecutor.AbstractCommandExecutor;
import co.marcin.novaguilds.enums.Command;
import co.marcin.novaguilds.enums.Message;
import co.marcin.novaguilds.enums.Permission;
import co.marcin.novaguilds.enums.VarKey;
import co.marcin.novaguilds.manager.PlayerManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class CommandAdminRegionSpectate extends AbstractCommandExecutor {
	private static final Command command = Command.ADMIN_REGION_SPECTATE;

	public CommandAdminRegionSpectate() {
		super(command);
	}

	@Override
	public void execute(CommandSender sender, String[] args) throws Exception {
		Map<VarKey, String> vars = new HashMap<>();

		if(args.length == 0 || args[0].equalsIgnoreCase(sender.getName())) {
			if(!(sender instanceof Player)) {
				Message.CHAT_CMDFROMCONSOLE.send(sender);
				return;
			}

			NovaPlayer nPlayer = PlayerManager.getPlayer(sender);

			nPlayer.toggleRegionSpectate();
			vars.put(VarKey.FLAG, Message.getOnOff(nPlayer.getRegionSpectate()));
			Message.CHAT_ADMIN_REGION_SPECTATE_TOGGLED_SELF.vars(vars).send(sender);
		}
		else { //for other
			if(!Permission.NOVAGUILDS_ADMIN_REGION_CHANGE_SPECTATE_OTHER.has(sender)) {
				Message.CHAT_NOPERMISSIONS.send(sender);
				return;
			}

			NovaPlayer nPlayer = PlayerManager.getPlayer(args[0]);

			if(nPlayer == null) {
				Message.CHAT_PLAYER_NOTEXISTS.send(sender);
				return;
			}

			nPlayer.toggleRegionSpectate();
			vars.put(VarKey.PLAYER, nPlayer.getName());
			vars.put(VarKey.FLAG, Message.getOnOff(nPlayer.getRegionSpectate()));

			if(nPlayer.isOnline()) {
				Message.CHAT_ADMIN_REGION_SPECTATE_NOTIFYOTHER.vars(vars).send(nPlayer);
			}

			Message.CHAT_ADMIN_REGION_SPECTATE_TOGGLED_OTHER.vars(vars).send(sender);
		}
	}
}
