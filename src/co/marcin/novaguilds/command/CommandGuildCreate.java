package co.marcin.novaguilds.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import co.marcin.novaguilds.event.GuildCreateEvent;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import co.marcin.novaguilds.basic.NovaGuild;
import co.marcin.novaguilds.NovaGuilds;
import co.marcin.novaguilds.basic.NovaPlayer;
import co.marcin.novaguilds.utils.StringUtils;

public class CommandGuildCreate implements CommandExecutor {
	private final NovaGuilds plugin;
	
	public CommandGuildCreate(NovaGuilds pl) {
		plugin = pl;
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("novaguilds.guild.create")) {
			plugin.sendMessagesMsg(sender,"chat.nopermissions");
			return true;
		}

		if(args.length != 2) {
			plugin.sendUsageMessage(sender,"guild.create");
			return true;
		}
		
		if(!(sender instanceof Player)) {
			plugin.info("You cannot create a guild from the console!");
			return true;
		}
		Player player = plugin.senderToPlayer(sender);

		String tag = args[0];
		String guildname = args[1];
		
		//remove colors
		guildname = StringUtils.removeColors(guildname);
		if(!plugin.getConfig().getBoolean("guild.settings.tag.color")) {
			tag = StringUtils.removeColors(tag);
		}
			
		NovaPlayer nPlayer = plugin.getPlayerManager().getPlayerBySender(sender);
		HashMap<String,String> vars = new HashMap<>();
		
		if(!nPlayer.hasGuild()) {
			if(plugin.getGuildManager().getGuildByName(guildname) == null) {
				if(plugin.getGuildManager().getGuildByTag(tag) == null) {
					if(plugin.getRegionManager().getRegionAtLocation(player.getLocation())==null) {
						//tag length
						if(tag.length() > plugin.getConfig().getInt("guild.settings.tag.max")) { //too long
							plugin.sendMessagesMsg(sender,"chat.createguild.tag.toolong");
							return true;
						}
						
						if(StringUtils.removeColors(tag).length() < plugin.getConfig().getInt("guild.settings.tag.min")) { //too short
							plugin.sendMessagesMsg(sender,"chat.createguild.tag.tooshort");
							return true;
						}
						
						//name length
						if(guildname.length() > plugin.getConfig().getInt("guild.settings.name.max")) { //too long
							plugin.sendMessagesMsg(sender,"chat.createguild.name.toolong");
							return true;
						}
						
						if(guildname.length() < plugin.getConfig().getInt("guild.settings.name.min")) { //too short
							plugin.sendMessagesMsg(sender,"chat.createguild.name.tooshort");
							return true;
						}

						//distance from spawn
						if(player.getWorld().getSpawnLocation().distance(player.getLocation()) < plugin.distanceFromSpawn) {
							vars.put("DISTANCE",plugin.distanceFromSpawn+"");
							plugin.sendMessagesMsg(sender,"chat.createguild.tooclosespawn",vars);
							return true;
						}
						
						String group = "default";
						
						for(String s : plugin.getConfig().getConfigurationSection("guild.create.groups").getKeys(false)) {
							if(sender.hasPermission("novaguilds.group."+s)) {
								group = s;
								break;
							}
						}
						
						//items required
						List<ItemStack> items = new ArrayList<>();
						List<String> itemstr = plugin.getConfig().getStringList("guild.create.groups."+group+".items");
						PlayerInventory inventory = player.getInventory();
						boolean hasitems = true;
						boolean hasMoney = true;
						int i;
						
						double requiredmoney = plugin.getConfig().getInt("guild.create.groups."+group+".money");
						
						if(requiredmoney>0 || sender.hasPermission("novaguilds.group.admin")) {
							if(plugin.econ.getBalance(player.getName()) < requiredmoney) {
								hasMoney = false;
							}
						}
						
						if(itemstr.size()==0 || sender.hasPermission("novaguilds.group.admin")) {
							hasitems=true;
							plugin.debug("no items required");
						}
						else {
							ItemStack stack;
							for(i=0;i<itemstr.size();i++) {
								String[] exp = itemstr.get(i).split(" ");
								String idname;
								String[] dataexp = null;
								byte data = (byte)0;
								int amount = Integer.parseInt(exp[1]);
								
								if(exp[0].contains(":")) {
									dataexp = exp[0].split(":");
									idname = dataexp[0];
									data = Byte.parseByte(dataexp[1]);
								}
								else {
									idname = exp[0];
								}
								
								stack = new ItemStack(Material.getMaterial(idname.toUpperCase()),amount);
								
								if(dataexp != null) {
									stack.getData().setData(data);
								}
								
								items.add(stack);
							}
							
							for(i=0;i<items.size();i++) {
								if(!inventory.containsAtLeast(items.get(i),items.get(i).getAmount())) {
									hasitems = false;
								}
							}
						}
							
						if(hasitems) { //ALL PASSED
							if(hasMoney) {
								//Guild object
								NovaGuild newGuild = new NovaGuild();
								newGuild.setName(guildname);
								newGuild.setTag(tag);
								newGuild.setLeaderName(sender.getName());
								newGuild.setSpawnPoint(player.getLocation());
								newGuild.addPlayer(nPlayer);
								plugin.getGuildManager().addGuild(newGuild);

								//nPlayer
								nPlayer.setGuild(newGuild);
								
								//taking money away
								plugin.econ.withdrawPlayer(sender.getName(),requiredmoney);
								
								//taking items away
								for(ItemStack item : items) {
									player.getInventory().removeItem(item);
								}
								
								//update tag and tabs
								plugin.tagUtils.updatePrefix(plugin.senderToPlayer(sender));
								
								//messages
								plugin.sendMessagesMsg(sender,"chat.createguild.success");

								vars.put("GUILDNAME", newGuild.getName());
								vars.put("PLAYER",sender.getName());
								plugin.broadcastMessage("broadcast.guild.created", vars);

								//fire event
								plugin.getServer().getPluginManager().callEvent(new GuildCreateEvent(newGuild));
							}
							else {
								String rmmsg = plugin.getMessages().getString("chat.createguild.notenoughtmoney");
								rmmsg = StringUtils.replace(rmmsg, "{REQUIREDMONEY}", requiredmoney + "");
								plugin.sendMessagesMsg(sender, rmmsg);
							}
						}
						else {
							String itemlist = "";
							for(i=0;i<items.size();i++) {
								String itemrow = plugin.getMessages().getString("chat.createguild.itemlist");
								itemrow = StringUtils.replace(itemrow, "{ITEMNAME}", items.get(i).getType().name());
								itemrow = StringUtils.replace(itemrow, "{AMOUNT}", items.get(i).getAmount() + "");
								
								itemlist += itemrow;
								
								if(i<items.size()-1) itemlist+= plugin.getMessages().getString("chat.createguild.itemlistsep");
							}

							plugin.sendMessagesMsg(sender, "chat.createguild.noitems");
							sender.sendMessage(StringUtils.fixColors(itemlist));
						}
					}
					else { //region at loc
						plugin.sendMessagesMsg(sender, "chat.createguild.regionhere");
					}
				}
				else { //tag exists
					plugin.sendMessagesMsg(sender, "chat.createguild.tagexists");
				}
			}
			else { //name exists
				plugin.sendMessagesMsg(sender, "chat.createguild.nameexists");
			}
		}
		else { //has guild already
			plugin.sendMessagesMsg(sender,"chat.createguild.hasguild");
		}
		return true;
	}
}
