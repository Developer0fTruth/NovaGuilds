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

package co.marcin.novaguilds.enums;

import co.marcin.novaguilds.NovaGuilds;
import co.marcin.novaguilds.basic.NovaGuild;
import co.marcin.novaguilds.basic.NovaHologram;
import co.marcin.novaguilds.basic.NovaPlayer;
import co.marcin.novaguilds.basic.NovaRegion;
import co.marcin.novaguilds.command.tabcompleter.TabCompleterAdmin;
import co.marcin.novaguilds.command.tabcompleter.TabCompleterGuild;
import co.marcin.novaguilds.interfaces.Executor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public enum Command {
	ADMIN_ACCESS(Permission.NOVAGUILDS_ADMIN_ACCESS, true, "nga", new TabCompleterAdmin()),
	ADMIN_RELOAD(Permission.NOVAGUILDS_ADMIN_RELOAD, true),
	ADMIN_SAVE(Permission.NOVAGUILDS_ADMIN_SAVE, true),
	ADMIN_CHATSPY(Permission.NOVAGUILDS_ADMIN_CHATSPY_SELF, false),

	ADMIN_GUILD_ACCESS(Permission.NOVAGUILDS_GUILD_ACCESS, true),
	ADMIN_GUILD_ABANDON(Permission.NOVAGUILDS_GUILD_ABANDON, true),
	ADMIN_GUILD_PURGE(Permission.NOVAGUILDS_ADMIN_GUILD_PURGE, true, Message.CHAT_USAGE_NGA_GUILD_PURGE, true),
	ADMIN_GUILD_BANK_PAY(Permission.NOVAGUILDS_ADMIN_GUILD_BANK_PAY, true),
	ADMIN_GUILD_BANK_WITHDRAW(Permission.NOVAGUILDS_ADMIN_GUILD_BANK_WITHDRAW, true),
	ADMIN_GUILD_INACTIVE(Permission.NOVAGUILDS_ADMIN_GUILD_INACTIVE_LIST, true),
	ADMIN_GUILD_INVITE(Permission.NOVAGUILDS_ADMIN_GUILD_INVITE, true),
	ADMIN_GUILD_KICK(Permission.NOVAGUILDS_ADMIN_GUILD_KICK, true),
	ADMIN_GUILD_LIST(Permission.NOVAGUILDS_ADMIN_GUILD_LIST, true),
	ADMIN_GUILD_SET_LEADER(Permission.NOVAGUILDS_ADMIN_GUILD_SET_LEADER, true),
	ADMIN_GUILD_SET_LIVEREGENERATIONTIME(Permission.NOVAGUILDS_ADMIN_GUILD_SET_LIVEREGENERATIONTIME, true),
	ADMIN_GUILD_SET_LIVES(Permission.NOVAGUILDS_ADMIN_GUILD_SET_LIVES, true),
	ADMIN_GUILD_SET_NAME(Permission.NOVAGUILDS_ADMIN_GUILD_SET_NAME, true),
	ADMIN_GUILD_SET_POINTS(Permission.NOVAGUILDS_ADMIN_GUILD_SET_POINTS, true),
	ADMIN_GUILD_SET_TAG(Permission.NOVAGUILDS_ADMIN_GUILD_SET_TAG, true),
	ADMIN_GUILD_SET_TIMEREST(Permission.NOVAGUILDS_ADMIN_GUILD_SET_TIMEREST, true),
	ADMIN_GUILD_SET_SLOTS(Permission.NOVAGUILDS_ADMIN_GUILD_SET_SLOTS, true),
	ADMIN_GUILD_TELEPORT(Permission.NOVAGUILDS_ADMIN_GUILD_TELEPORT_SELF, true),

	ADMIN_REGION_ACCESS(Permission.NOVAGUILDS_ADMIN_REGION_ACCESS, true),
	ADMIN_REGION_BYPASS(Permission.NOVAGUILDS_ADMIN_REGION_BYPASS_SELF, true),
	ADMIN_REGION_DELETE(Permission.NOVAGUILDS_ADMIN_REGION_DELETE, true),
	ADMIN_REGION_LIST(Permission.NOVAGUILDS_ADMIN_REGION_LIST, true),
	ADMIN_REGION_TELEPORT(Permission.NOVAGUILDS_ADMIN_REGION_TELEPORT_SELF, true),

	ADMIN_HOLOGRAM_ACCESS(Permission.NOVAGUILDS_ADMIN_HOLOGRAM_ACCESS, true),
	ADMIN_HOLOGRAM_LIST(Permission.NOVAGUILDS_ADMIN_HOLOGRAM_LIST, true),
	ADMIN_HOLOGRAM_TELEPORT(Permission.NOVAGUILDS_ADMIN_HOLOGRAM_TELEPORT, false),
	ADMIN_HOLOGRAM_DELETE(Permission.NOVAGUILDS_ADMIN_HOLOGRAM_DELETE, true),
	ADMIN_HOLOGRAM_ADD(Permission.NOVAGUILDS_ADMIN_HOLOGRAM_ADD, false),
	ADMIN_HOLOGRAM_ADDTOP(Permission.NOVAGUILDS_ADMIN_HOLOGRAM_ADDTOP, false),
	ADMIN_HOLOGRAM_TELEPORT_HERE(Permission.NOVAGUILDS_ADMIN_HOLOGRAM_TELEPORT_HERE, false),

	GUILD_ACCESS(Permission.NOVAGUILDS_GUILD_ACCESS, false, "guild", new TabCompleterGuild()),
	GUILD_ABANDON(Permission.NOVAGUILDS_GUILD_ABANDON, false, "abandon"),
	GUILD_ALLY(Permission.NOVAGUILDS_GUILD_ALLY, false),
	GUILD_BANK_PAY(Permission.NOVAGUILDS_GUILD_BANK_PAY, false),
	GUILD_BANK_WITHDRAW(Permission.NOVAGUILDS_GUILD_BANK_WITHDRAW, false),
	GUILD_BUYLIFE(Permission.NOVAGUILDS_GUILD_BUYLIFE, false),
	GUILD_BUYSLOT(Permission.NOVAGUILDS_GUILD_BUYSLOT, false),
	GUILD_CHATMODE(Permission.NOVAGUILDS_GUILD_CHATMODE, false),
	GUILD_COMPASS(Permission.NOVAGUILDS_GUILD_COMPASS, false),
	GUILD_CREATE(Permission.NOVAGUILDS_GUILD_CREATE, false, "create"),
	GUILD_EFFECT(Permission.NOVAGUILDS_GUILD_EFFECT, false),
	GUILD_HOME(Permission.NOVAGUILDS_GUILD_HOME, false),
	GUILD_INFO(Permission.NOVAGUILDS_GUILD_INFO, true, "gi"),
	GUILD_INVITE(Permission.NOVAGUILDS_GUILD_INVITE, false, "invite"),
	GUILD_JOIN(Permission.NOVAGUILDS_GUILD_JOIN, false, "join"),
	GUILD_KICK(Permission.NOVAGUILDS_GUILD_KICK, false),
	GUILD_LEADER(Permission.NOVAGUILDS_GUILD_LEADER, false),
	GUILD_LEAVE(Permission.NOVAGUILDS_GUILD_LEAVE, false, "leave"),
	GUILD_MENU(Permission.NOVAGUILDS_GUILD_MENU, false, "guildmenu"),
	GUILD_PVPTOGGLE(Permission.NOVAGUILDS_GUILD_PVPTOGGLE, false),
	GUILD_REQUIREDITEMS(Permission.NOVAGUILDS_GUILD_REQUIREDITEMS, false),
	GUILD_TOP(Permission.NOVAGUILDS_GUILD_TOP, true),
	GUILD_WAR(Permission.NOVAGUILDS_GUILD_WAR, false),
	GUILD_BOSS(Permission.NOVAGUILDS_GUILD_BOSS, false),
	GUILD_OPENINVITATION(Permission.NOVAGUILDS_GUILD_OPENINVITATION, false),
	GUILD_SET_NAME(Permission.NOVAGUILDS_GUILD_SET_NAME, false),
	GUILD_SET_TAG(Permission.NOVAGUILDS_GUILD_SET_TAG, false),

	REGION_ACCESS(Permission.NOVAGUILDS_REGION_ACCESS, false),
	REGION_BUY(Permission.NOVAGUILDS_REGION_CREATE, false),
	REGION_DELETE(Permission.NOVAGUILDS_REGION_DELETE, false),

	TOOL_GET(Permission.NOVAGUILDS_TOOL_GET, false),
	PLAYERINFO(Permission.NOVAGUILDS_PLAYERINFO, true, "playerinfo"),
	NOVAGUILDS(Permission.NOVAGUILDS_NOVAGUILDS, true, "novaguilds"),
	CONFIRM(Permission.NOVAGUILDS_CONFIRM, false, "confirm");

	private Message usageMessage;
	private boolean allowConsole = true;
	private String permissionPath = "";
	private Permission permission;
	private String genericCommand;
	private TabCompleter tabCompleter;
	private boolean needConfirm = false;
	private Object executorVariable;

	Command(Permission permission, boolean allowConsole) {
		this.permission = permission;
		this.permissionPath = permission.getPath();
		this.allowConsole = allowConsole;
	}

	Command(Permission permission, boolean allowConsole, String genericCommand) {
		this.permission = permission;
		this.permissionPath = permission.getPath();
		this.allowConsole = allowConsole;
		this.genericCommand = genericCommand;
	}

	Command(Permission permission, boolean allowConsole, String genericCommand, TabCompleter tabCompleter) {
		this.permission = permission;
		this.permissionPath = permission.getPath();
		this.allowConsole = allowConsole;
		this.genericCommand = genericCommand;
		this.tabCompleter = tabCompleter;
	}

	Command(Permission permission, boolean allowConsole, Message usageMessage) {
		this.permission = permission;
		this.permissionPath = permission.getPath();
		this.allowConsole = allowConsole;
		this.usageMessage = usageMessage;
	}

	Command(Permission permission, boolean allowConsole, Message usageMessage, boolean needConfirm) {
		this.permission = permission;
		this.permissionPath = permission.getPath();
		this.allowConsole = allowConsole;
		this.usageMessage = usageMessage;
		this.needConfirm = needConfirm;
	}

	public boolean allowConsole() {
		return allowConsole;
	}

	public String getPermission() {
		return permissionPath;
	}

	public boolean hasPermission(CommandSender sender) {
		return permission.has(sender) || sender.isOp();
	}

	public boolean allowedSender(CommandSender sender) {
		return sender instanceof Player || allowConsole;
	}

	public Message getUsageMessage() {
		return usageMessage;
	}

	private Executor getExecutor() {
		return NovaGuilds.getInstance().getCommandManager().getExecutor(this);
	}

	public void execute(CommandSender sender, String[] args) {
		Executor executor = getExecutor();
		NovaPlayer nPlayer = NovaPlayer.get(sender);

		if(!this.hasPermission(sender)) {
			Message.CHAT_NOPERMISSIONS.send(sender);
			return;
		}

		if(!this.allowedSender(sender)) {
			Message.CHAT_CMDFROMCONSOLE.send(sender);
			return;
		}

		if(isNeedConfirm() && (nPlayer.getCommandExecutorHandler() == null || nPlayer.getCommandExecutorHandler().getState() != CommandExecutorHandlerState.CONFIRMED)) {
			nPlayer.newCommandExecutorHandler(this, args);
			nPlayer.getCommandExecutorHandler().executorVariable(getExecutorVariable());

		}
		else {
			if(executor instanceof Executor.ReversedAdminGuild) {
				((Executor.ReversedAdminGuild) executor).guild((NovaGuild) getExecutorVariable());
			}
			else if(executor instanceof Executor.ReversedAdminRegion) {
				((Executor.ReversedAdminRegion) executor).region((NovaRegion) getExecutorVariable());
			}
			else if(executor instanceof Executor.ReversedAdminHologram) {
				((Executor.ReversedAdminHologram) executor).hologram((NovaHologram) getExecutorVariable());
			}

			executor.execute(sender, args);
		}
	}

	public boolean hasGenericCommand() {
		return genericCommand != null;
	}

	public boolean hasTabCompleter() {
		return tabCompleter != null;
	}

	public String getGenericCommand() {
		return genericCommand;
	}

	public TabCompleter getTabCompleter() {
		return tabCompleter;
	}

	public boolean isNeedConfirm() {
		return needConfirm;
	}

	public Command executorVariable(Object executorVariable) {
		this.executorVariable = executorVariable;
		return this;
	}

	public Object getExecutorVariable() {
		return executorVariable;
	}
}
