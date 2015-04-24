package co.marcin.NovaGuilds.command;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import co.marcin.NovaGuilds.basic.NovaGuild;
import co.marcin.NovaGuilds.NovaGuilds;
import co.marcin.NovaGuilds.basic.NovaPlayer;

public class CommandGuildAlly implements CommandExecutor {
	public final NovaGuilds plugin;
	
	public CommandGuildAlly(NovaGuilds pl) {
		plugin = pl;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		NovaPlayer nPlayer = plugin.getPlayerManager().getPlayerBySender(sender);
		
		if(sender.hasPermission("NovaGuilds.guild.ally")) {
			if(args.length==1) {
				String allyname = args[0];

				if(nPlayer.hasGuild()) {
					NovaGuild guild = nPlayer.getGuild();

					if(plugin.getGuildManager().exists(allyname)) {
						NovaGuild allyguild = plugin.getGuildManager().getGuildFind(allyname);

						if(!allyname.equalsIgnoreCase(guild.getName())) {
							if(guild.getLeaderName().equalsIgnoreCase(sender.getName())) {
								HashMap<String,String> vars = new HashMap<>();
								vars.put("GUILDNAME",guild.getName());
								vars.put("ALLYNAME",allyguild.getName());

								if(!guild.isAlly(allyguild)) {
									if(guild.isWarWith(allyguild)) {
										plugin.sendMessagesMsg(sender,"chat.guild.ally.war");
										return true;
									}

									for(NovaPlayer allyP : allyguild.getPlayers()) {
										if(allyP.isOnline()) {
											plugin.sendMessagesMsg(allyP.getPlayer(),"chat.guild.ally.newinvite",vars);
										}
									}

									if(guild.isInvitedToAlly(allyname)) { //Accepting
										allyguild.addAlly(guild.getName());
										guild.addAlly(allyguild.getName());
										guild.removeAllyInvitation(allyname);

										plugin.getGuildManager().saveGuildLocal(guild);
										plugin.getGuildManager().saveGuildLocal(allyguild);

										plugin.broadcastMessage("broadcast.guild.allied",vars);

										plugin.sendMessagesMsg(sender,"chat.guild.ally.accepted",vars);

										//tags
										plugin.tagUtils.updateTagAll();
									}
									else { //Inviting
										if(!allyguild.isInvitedToAlly(guild.getName())) {
											allyguild.addAllyInvitation(guild.getName());

											plugin.getGuildManager().saveGuildLocal(allyguild);

											plugin.sendMessagesMsg(sender,"chat.guild.ally.invited",vars);
											plugin.broadcastGuild(allyguild,"chat.guild.ally.notifyguild",vars);
										}
										else { //cancel inv
											allyguild.removeAllyInvitation(guild.getName());

											plugin.getGuildManager().saveGuildLocal(allyguild);

											plugin.sendMessagesMsg(sender,"chat.guild.ally.canceled",vars);
											plugin.broadcastGuild(allyguild,"chat.guild.ally.notifyguildcanceled",vars);
										}
									}
								}
								else { //UN-ALLY
									guild.removeAlly(allyname);
									allyguild.removeAlly(guild.getName());

									plugin.getGuildManager().saveGuildLocal(guild);
									plugin.getGuildManager().saveGuildLocal(allyguild);

									plugin.broadcastMessage("broadcast.guild.endally",vars);

									plugin.tagUtils.updateTagAll();
								}
							}
							else {
								plugin.sendMessagesMsg(sender,"chat.guild.notleader");
							}
						}
						else {
							plugin.sendMessagesMsg(sender,"chat.guild.ally.samename");
						}
					}
					else {
						plugin.sendMessagesMsg(sender,"chat.guild.namenotexist");
					}
				}
				else {
					plugin.sendMessagesMsg(sender,"chat.guild.notinguild");
				}
			}
			else {
				plugin.sendMessagesMsg(sender,"chat.guild.entername");
			}
		}
		else {
			plugin.sendMessagesMsg(sender,"chat.nopermissions");
		}
		
		return true;
	}
}
