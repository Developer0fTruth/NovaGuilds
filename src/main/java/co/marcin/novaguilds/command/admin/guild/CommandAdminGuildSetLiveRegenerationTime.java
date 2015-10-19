package co.marcin.novaguilds.command.admin.guild;

import co.marcin.novaguilds.basic.NovaGuild;
import co.marcin.novaguilds.enums.Commands;
import co.marcin.novaguilds.enums.Config;
import co.marcin.novaguilds.enums.Message;
import co.marcin.novaguilds.interfaces.Executor;
import co.marcin.novaguilds.interfaces.ExecutorReversedAdminGuild;
import co.marcin.novaguilds.util.LoggerUtils;
import co.marcin.novaguilds.util.NumberUtils;
import co.marcin.novaguilds.util.StringUtils;
import org.bukkit.command.CommandSender;

public class CommandAdminGuildSetLiveRegenerationTime implements Executor, ExecutorReversedAdminGuild {
	private NovaGuild guild;
	private final Commands command;

	public CommandAdminGuildSetLiveRegenerationTime(Commands command) {
		this.command = command;
		plugin.getCommandManager().registerExecutor(command, this);
	}

	@Override
	public void guild(NovaGuild guild) {
		this.guild = guild;
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

		String timeString;
		if(args.length > 1) {
			timeString = StringUtils.join(args," ");
		}
		else {
			timeString = args[0];
		}

		int iseconds = StringUtils.StringToSeconds(timeString);
		long seconds = Long.parseLong(iseconds+"");

		long newregentime = NumberUtils.systemSeconds() + (seconds - Config.LIVEREGENERATION_REGENTIME.getSeconds());

		guild.setLostLiveTime(newregentime);
		Message.CHAT_ADMIN_GUILD_TIMEREST_SET.send(sender);
	}
}
