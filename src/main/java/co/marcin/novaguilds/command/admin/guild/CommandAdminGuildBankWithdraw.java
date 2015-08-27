package co.marcin.novaguilds.command.admin.guild;

import co.marcin.novaguilds.NovaGuilds;
import co.marcin.novaguilds.basic.NovaGuild;
import co.marcin.novaguilds.enums.Message;
import co.marcin.novaguilds.util.NumberUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

public class CommandAdminGuildBankWithdraw implements CommandExecutor {
	private final NovaGuilds plugin;
	private final NovaGuild guild;

	public CommandAdminGuildBankWithdraw(NovaGuilds pl, NovaGuild guild) {
		plugin = pl;
		this.guild = guild;
	}

	/*
	* args:
	*  0 - guildname
	*  1 - money
	* */
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("novaguilds.admin.guild.bank.withdraw")) { //no permissions
			Message.CHAT_NOPERMISSIONS.send(sender);
			return true;
		}

		if(args.length != 1) { //invalid arguments
			Message.CHAT_USAGE_NGA_GUILD_BANK_WITHDRAW.send(sender);
			return true;
		}

		String money_str = args[0];

		if(!NumberUtils.isNumeric(money_str)) { //money not int
			Message.CHAT_ENTERINTEGER.send(sender);
			return true;
		}

		double money = Double.parseDouble(money_str);

		if(money < 0) {
			Message.CHAT_BASIC_NEGATIVENUMBER.send(sender);
			return true;
		}

		if(guild.getMoney() < money) { //guild has not enough money
			Message.CHAT_GUILD_BANK_WITHDRAW_NOTENOUGH.send(sender);
			return true;
		}

		money = NumberUtils.roundOffTo2DecPlaces(money);

		guild.takeMoney(money);

		HashMap<String,String> vars = new HashMap<>();
		vars.put("MONEY",money_str);
		vars.put("GUILDNAME", guild.getName());
		Message.CHAT_ADMIN_GUILD_BANK_WITHDREW.vars(vars).send(sender);

		return true;
	}
}
