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


import co.marcin.novaguilds.api.basic.NovaGuild;
import co.marcin.novaguilds.basic.NovaPlayer;
import co.marcin.novaguilds.enums.Command;
import co.marcin.novaguilds.enums.GuildPermission;
import co.marcin.novaguilds.enums.Message;
import co.marcin.novaguilds.enums.VarKey;
import co.marcin.novaguilds.interfaces.Executor;
import co.marcin.novaguilds.util.NumberUtils;
import co.marcin.novaguilds.util.TabUtils;
import org.bukkit.command.CommandSender;

public class CommandGuildBankPay implements Executor {
	private final Command command = Command.GUILD_BANK_PAY;

	public CommandGuildBankPay() {
		plugin.getCommandManager().registerExecutor(command, this);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		NovaPlayer nPlayer = plugin.getPlayerManager().getPlayer(sender);

		if(!nPlayer.hasGuild()) {
			Message.CHAT_GUILD_NOTINGUILD.send(sender);
			return;
		}

		NovaGuild guild = nPlayer.getGuild();

		if(!nPlayer.hasPermission(GuildPermission.BANK_PAY)) {
			Message.CHAT_GUILD_NOGUILDPERM.send(sender);
			return;
		}

		if(args.length == 0 || !NumberUtils.isNumeric(args[0])) {
			Message.CHAT_GUILD_BANK_ENTERAMOUNT.send(sender);
			return;
		}

		Double money = Double.parseDouble(args[0]);

		money = NumberUtils.roundOffTo2DecPlaces(money);

		if(money < 0) {
			Message.CHAT_BASIC_NEGATIVENUMBER.send(sender);
			return;
		}

		if(!nPlayer.hasMoney(money)) {
			Message.CHAT_GUILD_BANK_PAY_NOTENOUGH.send(sender);
			return;
		}

		nPlayer.takeMoney(money);
		guild.addMoney(money);
		Message.CHAT_GUILD_BANK_PAY_PAID.setVar(VarKey.AMOUNT, money).send(sender);
		TabUtils.refresh(nPlayer.getGuild());
	}

	@Override
	public Command getCommand() {
		return command;
	}
}
