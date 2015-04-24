package co.marcin.NovaGuilds;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import co.marcin.NovaGuilds.basic.NovaGuild;
import co.marcin.NovaGuilds.basic.NovaPlayer;
import co.marcin.NovaGuilds.basic.NovaRegion;
import co.marcin.NovaGuilds.listener.*;
import co.marcin.NovaGuilds.utils.StringUtils;
import co.marcin.NovaGuilds.utils.TagUtils;
import net.milkbowl.vault.Metrics;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.kitteh.tag.TagAPI;
import org.mcsg.double0negative.tabapi.TabAPI;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import code.husky.mysql.MySQL;
import code.husky.sqlite.SQLite;
import co.marcin.NovaGuilds.command.CommandAdmin;
import co.marcin.NovaGuilds.command.CommandGuild;
import co.marcin.NovaGuilds.command.CommandGuildAbandon;
import co.marcin.NovaGuilds.command.CommandGuildCreate;
import co.marcin.NovaGuilds.command.CommandGuildHome;
import co.marcin.NovaGuilds.command.CommandGuildInfo;
import co.marcin.NovaGuilds.command.CommandGuildInvite;
import co.marcin.NovaGuilds.command.CommandGuildJoin;
import co.marcin.NovaGuilds.command.CommandGuildLeave;
import co.marcin.NovaGuilds.command.CommandNovaGuilds;
import co.marcin.NovaGuilds.manager.GuildManager;
import co.marcin.NovaGuilds.manager.PlayerManager;
import co.marcin.NovaGuilds.manager.RegionManager;

public class NovaGuilds extends JavaPlugin {
	private final Logger log = Logger.getLogger("Minecraft");
	private static final String logprefix = "[NovaGuilds] ";
	public final PluginDescriptionFile pdf = this.getDescription();
	public final PluginManager pm = getServer().getPluginManager();
	public String prefix;
	public String sqlp;
	public FileConfiguration config;
	public final boolean DEBUG = getConfig().getBoolean("debug");
	
	private long MySQLReconnectStamp = System.currentTimeMillis();

	//TODO kickowanie z admina dubluje user�w gildii

	//Vault
	public Economy econ = null;
    //public static Permission perms = null;
    //public static Chat chat = null;

	public boolean useTabAPI;
	public boolean useTagAPI;
	public boolean useVault;
	public boolean useHolographicDisplays;
	
	public boolean useMySQL;
	public String lang;
	
	public HashMap<String,NovaPlayer> players = new HashMap<>();
	public HashMap<String,NovaGuild> guilds = new HashMap<>();
	public HashMap<String,NovaRegion> regions = new HashMap<>();

	public HashMap<String,NovaPlayer> players_changes = new HashMap<>();
	public HashMap<String,NovaGuild> guilds_changes = new HashMap<>();
	
	private GuildManager guildManager = new GuildManager(this);
	private RegionManager regionManager = new RegionManager(this);
	private PlayerManager playerManager = new PlayerManager(this);
	
	public int progress = 0;
	public long savePeriod = 15; //minutes

	public TagUtils tagUtils;
	
	//Database
	public MySQL MySQL;
	public SQLite sqlite;
	public Connection c = null;
	
	private FileConfiguration messages = null;
	private File messagesFile;
	
	public void onEnable() {
		saveDefaultConfig();
		config = getConfig();
		sqlp = config.getString("mysql.prefix");
		savePeriod = config.getLong("saveperiod");
		lang = config.getString("lang");
		
		useVault = config.getBoolean("usevault");
		useTabAPI = config.getBoolean("tabapi.enabled");
		useTagAPI = config.getBoolean("tagapi.enabled");
		useHolographicDisplays = config.getBoolean("holographicdisplays.enabled");
		
		useMySQL = getConfig().getBoolean("usemysql");

		tagUtils = new TagUtils(this);

		//HolographicDisplays
		if(useHolographicDisplays) {
			if (!checkHolographicDisplays() ) {
	            log.severe(String.format("[%s] - Disabled due to no HolographicDisplays dependency found!", pdf.getName()));
				useHolographicDisplays = false;
	            getServer().getPluginManager().disablePlugin(this);
	            return;
	        }
			info("HolographicDisplays hooked");
		}
		
		
		//Vault Economy
		if(useVault) {
			if (!setupEconomy() ) {
	            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", pdf.getName()));
	            getServer().getPluginManager().disablePlugin(this);
	            return;
	        }
			info("Vault's Economy hooked");
		}
		
		//TabAPI
		if(useTabAPI) {
			if(!checkTabAPI()) {
				log.severe(String.format("[%s] - Disabled due to no TabAPI dependency found!", pdf.getName()));
	            getServer().getPluginManager().disablePlugin(this);
	            return;
			}
			info("TabAPI hooked");
		}
		
		//TabAPI
		if(useTagAPI) {
			if(!checkTagAPI()) {
				log.severe(String.format("[%s] - Disabled due to no TagAPI dependency found!", pdf.getName()));
	            getServer().getPluginManager().disablePlugin(this);
	            return;
			}
			info("TagAPI hooked");
		}
		
		//messages.yml
		File langsDir = new File(getDataFolder(),"lang/");
		if(!langsDir.exists()) {
			if(langsDir.mkdir()) {
				info("Language dir created");
			}
		}
		
		if(!loadMessages()) {
            return;
		}
		
		prefix = messages.getString("chat.prefix");
		info("Messages loaded");
        
		//Version check
        String latest = StringUtils.getContent("http://NovaGuilds.marcin.co/latest.info");
        info("You're using version: v"+pdf.getVersion());
        info("Latest build of the plugin is: #"+latest);
        
        if(pdf.getVersion().equalsIgnoreCase(latest)) {
        	info("Your plugin build is the latest one");
        }
        else {
        	info("You should update your plugin to #"+latest+"!");
        }
		
		//command executors
		getCommand("NovaGuilds").setExecutor(new CommandNovaGuilds(this));
		getCommand("ng").setExecutor(new CommandNovaGuilds(this));
		getCommand("nga").setExecutor(new CommandAdmin(this));
		
		getCommand("abandon").setExecutor(new CommandGuildAbandon(this));
		getCommand("guild").setExecutor(new CommandGuild(this));
		getCommand("gi").setExecutor(new CommandGuildInfo(this));
		getCommand("create").setExecutor(new CommandGuildCreate(this));
		getCommand("nghome").setExecutor(new CommandGuildHome(this));
		getCommand("join").setExecutor(new CommandGuildJoin(this));
		getCommand("leave").setExecutor(new CommandGuildLeave(this));
		
		getCommand("invite").setExecutor(new CommandGuildInvite(this));
		
		try {
			if(useMySQL) {
				MySQL = new MySQL(this, getConfig().getString("mysql.host") , getConfig().getString("mysql.port"), getConfig().getString("mysql.database"), getConfig().getString("mysql.username"), getConfig().getString("mysql.password"));
				c = MySQL.openConnection();
				info("Connected to MySQL database");
			}
			else {
				sqlite = new SQLite(this,"sqlite.db");
				c = sqlite.openConnection();
				info("Connected to SQLite database");
			}
			
			//Tables setup
			DatabaseMetaData md = c.getMetaData();
			ResultSet rs = md.getTables(null, null, sqlp+"%", null);
			if(!rs.next()) {
				info("Couldn't find tables in the base. Creating...");
				String[] SQLCreateCode = getSQLCreateCode(useMySQL);
				if(SQLCreateCode.length != 0) {
					try {
						for(String tableCode : SQLCreateCode) {
							createTable(tableCode);
							info("Tables added to the database!");
						}
					}
					catch (SQLException e) {
						info("Could not create tables. Disabling");
						info("SQLException: "+e.getMessage());
						getServer().getPluginManager().disablePlugin(this);
						return;
					}
				}
				else {
					info("Couldn't find SQL create code for tables!");
					getServer().getPluginManager().disablePlugin(this);
					return;
				}
			}
			else {
				info("No MySQL config required.");
			}
			
			//Data loading
			getRegionManager().loadRegions();
			getGuildManager().loadGuilds();
			getPlayerManager().loadPlayers();
			
			//Listeners
			pm.registerEvents(new LoginListener(this),this);
			pm.registerEvents(new ToolListener(this),this);
			pm.registerEvents(new RegionInteractListener(this),this);
			pm.registerEvents(new MoveListener(this),this);
			pm.registerEvents(new ChatListener(this),this);

			pm.registerEvents(new PvpListener(this),this);
			pm.registerEvents(new DeathListener(this),this);

			new ReceiveNameTagListener(this);
			info("Listeners activated");
			
			//Tablist update
			updateTabAll();
			tagUtils.updateTagAll();
			
			//save scheduler
			runScheduler();
			info("Save scheduler is running");

			//metrics
			setupMetrics();

			info("#"+pdf.getVersion()+" Enabled");
		}
		catch (SQLException e) {
			info("MySQL connection failed.");
			info(e.getMessage());
			pm.disablePlugin(this);
		}
		catch (ClassNotFoundException e) {
			info("MySQL class not found.");
		}
	}
	
	public void onDisable() {
		getGuildManager().saveAll();
		getRegionManager().saveAll();
		getPlayerManager().saveAll();
		info("Saved all data");

		//removing holographic displays
		if(useHolographicDisplays) {
			for(Hologram h : HologramsAPI.getHolograms(this)) {
				h.delete();
			}
		}
		
		for(Player p : getServer().getOnlinePlayers()) {
			NovaPlayer nPlayer = getPlayerManager().getPlayerByName(p.getName());
			Location l1 = nPlayer.getSelectedLocation(0);
			Location l2 = nPlayer.getSelectedLocation(1);
			
			if(l1 != null && l2 != null) {
				getRegionManager().sendSquare(p,l1,l2,null,(byte)0);
				getRegionManager().resetCorner(p,l1);
				getRegionManager().resetCorner(p,l2);
			}
		}
		
		info("#"+pdf.getVersion()+" Disabled");
	}
	
	public void MySQLreload() {
		if(!useMySQL) return;
		long stamp = System.currentTimeMillis();
		
		if(stamp-MySQLReconnectStamp > 3000) {
	    	try {
				MySQL.closeConnection();
				try {
					c = MySQL.openConnection();
					info("MySQL reconnected");
					MySQLReconnectStamp = System.currentTimeMillis();
				}
				catch (ClassNotFoundException e) {
					info(e.getMessage());
				}
			}
	    	catch (SQLException e1) {
	    		info(e1.getMessage());
			}
		}
    }
	
	public void info(String msg) {
		log.info(logprefix+msg);
	}
	
	//Managers
	public GuildManager getGuildManager() {
		return guildManager;
	}

	public RegionManager getRegionManager() {
		return regionManager;
	}

	public PlayerManager getPlayerManager() {
		return playerManager;
	}
	
	//Vault economy
	private boolean setupEconomy() {
        if(getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        
        if (rsp == null) {
            return false;
        }
        
        econ = rsp.getProvider();
        return econ != null;
    }
	
	public boolean checkTabAPI() {
		return !(getServer().getPluginManager().getPlugin("TabAPI") == null);
	}
	
	//tagAPI
	public boolean checkTagAPI() {
		return !(getServer().getPluginManager().getPlugin("TagAPI") == null);
	}
	
	//HolographicDisplays
	public boolean checkHolographicDisplays() {
		return getServer().getPluginManager().isPluginEnabled("HolographicDisplays");
	}

	public String getTabName(Player player) {
		String tag;
		String guildtag;
		String rank = "";
		NovaPlayer nplayer = getPlayerManager().getPlayerByName(player.getName());
		String tabName = player.getName();

		if(nplayer.hasGuild()) {
			tag = config.getString("guild.tag");
			guildtag = nplayer.getGuild().getTag();

			if(!config.getBoolean("tabapi.colortags")) {
				guildtag = StringUtils.removeColors(guildtag);
			}

			tag = StringUtils.replace(tag, "{TAG}", guildtag);

			if(config.getBoolean("tabapi.rankprefix")) {
				if(nplayer.getGuild().getLeaderName().equalsIgnoreCase(player.getName())) {
					rank = messages.getString("chat.guildinfo.leaderprefix");
				}
			}

			tag = StringUtils.replace(tag, "{RANK}", rank);
			tag = StringUtils.fixColors(tag);
			tabName = tag + tabName;
		}
		
		return tabName;
	}
	
	public void updateTab(Player player) {
		int x=0;
		int y=0;
		String tabName;
		for(Player p : getServer().getOnlinePlayers()) {
			if(useTabAPI) {
				tabName = getTabName(p);
			}
			else {
				tabName = p.getName();
			}
			
			if(DEBUG) info(y+" - "+getTabName(p));
			TabAPI.setTabString(this,player,x,y,tabName);
			y++;
			
			if(y >= TabAPI.getVertSize()) {
				x++;
				y=0;
				if(DEBUG) info(">>>");
			}
		}
		TabAPI.updateAll();
	}
	
	public void updateTabAll() {
		if(checkTabAPI()) {
			int x;
			int y;
			String tabName;
			
			for(Player p2 : getServer().getOnlinePlayers()) {
				TabAPI.clearTab(p2);
				x=0;
				y=0;
				for(Player p : getServer().getOnlinePlayers()) {
					if(useTabAPI) {
						tabName = getTabName(p);
					}
					else {
						tabName = p.getName();
					}
					
					if(DEBUG) info(y+" - "+getTabName(p));
					TabAPI.setTabString(this,p2,x,y,tabName);
					y++;
					
					if(y >= TabAPI.getVertSize()) {
						x++;
						y=0;
						if(DEBUG) info(">>>");
					}
				}
			}
			TabAPI.updateAll();
		}
	}
	
	//update exclude
	public void updateTabAll(Player excludeplayer) {
		if(checkTabAPI()) {
			int x;
			int y;
			String tabName;
			
			for(Player p2 : getServer().getOnlinePlayers()) {
				TabAPI.clearTab(p2);
				TabAPI.resetTabList(p2);
				x=0;
				y=0;
				for(Player p : getServer().getOnlinePlayers()) {
					
					if(!p.equals(excludeplayer)) {
						if(useTabAPI) {
							tabName = getTabName(p);
						}
						else {
							tabName = p.getName();
						}
						
						if(DEBUG) info(y+" - "+getTabName(p));
						TabAPI.setTabString(this,p2,x,y,tabName);
						y++;
						
						if(y >= TabAPI.getVertSize()) {
							x++;
							y=0;
							if(DEBUG) info(">>>");
						}
					}
					else if(DEBUG) info("exclude: "+p.getName());
				}
			}
			TabAPI.updateAll();
		}
	}
	
	//MESSAGES
	
	public boolean loadMessages() {
		messagesFile = new File(getDataFolder()+"/lang", lang+".yml");
        if(!messagesFile.exists()) {
        	if(getResource("lang/"+lang+".yml") != null) {
				saveResource("lang/"+lang+".yml", false);
				info("New messages file created: "+lang+".yml");
        	}
        	else {
        		info("Couldn't find language file: "+lang+".yml");
        		getServer().getPluginManager().disablePlugin(this);
	            return false;
        	}
        }
        
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        return true;
	}
	
	//set string from file
	public String getMessagesString(String path) {
		String msg = getMessages().getString(path);
		
		if(msg == null || !(msg instanceof String)) {
			return path;
		}
				
		return msg;
	}
	
	//get messages
	public FileConfiguration getMessages() {
		return messages;
	}
	
	//set messages
	public void setMessages(FileConfiguration msg) {
		messages = msg;
	}
	
	public void loadMessagesFile(File msgFile) {
		messagesFile = msgFile;
		
		if(!messagesFile.exists()) {
    		saveResource("lang/"+lang+".yml", false);
    		info("New messages file created");
	    }

		setMessages(YamlConfiguration.loadConfiguration(messagesFile));
	}
	
	//send string with prefix to a player
	public void sendPrefixMessage(Player p, String msg) {
		p.sendMessage(StringUtils.fixColors(prefix + msg));
	}

	public void sendPrefixMessage(CommandSender sender, String msg) {
		sender.sendMessage(StringUtils.fixColors(prefix + msg));
	}
	
	//send message from file with prefix to a player
	public void sendMessagesMsg(Player p, String path) {
		p.sendMessage(StringUtils.fixColors(prefix + getMessagesString(path)));
	}
	
	//send message from file with prefix and vars to a player
	public void sendMessagesMsg(Player p, String path, HashMap<String,String> vars) {
		String msg = getMessagesString(path);
		msg = replaceMessage(msg,vars);
		p.sendMessage(StringUtils.fixColors(prefix + msg));
	}
	
	public void sendMessagesMsg(CommandSender sender, String path) {
		sender.sendMessage(StringUtils.fixColors(prefix + getMessagesString(path)));
	}
	
	public void sendMessagesMsg(CommandSender sender, String path, HashMap<String,String> vars) {
		String msg = getMessagesString(path);
		msg = replaceMessage(msg,vars);
		sender.sendMessage(StringUtils.fixColors(prefix + msg));
	}
	
	//broadcast string to all players
	public void broadcast(String msg) {
		for(Player p : getServer().getOnlinePlayers()) {
			sendPrefixMessage(p, msg);
		}
	}
	
	//broadcast message from file to all players
	public void broadcastMessage(String path) {
		broadcastMessage(path,null);
	}
	
	public void broadcastMessage(String path,HashMap<String,String> vars) {
		String msg = getMessagesString(path);
		msg = replaceMessage(msg,vars);
		
		for(Player p : getServer().getOnlinePlayers()) {
			sendPrefixMessage(p, msg);
		}
	}
	
	public void broadcastGuild(NovaGuild guild, String path,HashMap<String,String> vars) {
		String msg = getMessagesString(path);
		msg = replaceMessage(msg,vars);
		
		for(NovaPlayer p : guild.getPlayers()) {
			if(p.isOnline())
				sendPrefixMessage(p.getPlayer(), msg);
		}
	}
	
	public String replaceMessage(String msg, HashMap<String,String> vars) {
		if(vars != null) {
			if(vars.size() > 0) {
				for(Entry<String, String> e : vars.entrySet()) {
					msg = StringUtils.replace(msg, "{" + e.getKey() + "}", e.getValue());
				}
			}
		}
		
		return msg;
	}
	
	//convert sender to player
	public Player senderToPlayer(CommandSender sender) {
		return getServer().getPlayer(sender.getName());
	}
	
	//true=mysql, false=sqlite
	public String[] getSQLCreateCode(boolean mysql) {
		String url = "http://NovaGuilds.marcin.co/sqltables.txt";
		String sql = StringUtils.getContent(url);
		
		int index;
		if(mysql)
			index=0;
		else
			index=1;
		
		String[] types = sql.split("--TYPE--");
		String[] codes = types[index].split("--");
		return codes;
	}
	
	public void createTable(String sql) throws SQLException {
		MySQLreload();
		Statement statement;
		sql = StringUtils.replace(sql, "{SQLPREFIX}", sqlp);
		statement = c.createStatement();
		statement.executeUpdate(sql);
	}
	
	public void runScheduler() {
		BukkitScheduler scheduler = getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				getGuildManager().saveAll();
				getRegionManager().saveAll();
				getPlayerManager().saveAll();
				info("Saved data.");
			}
		}, 0L, 20L * 60 * savePeriod);
	}

	public void setupMetrics() {
		try {
			Metrics metrics = new Metrics(this);
			Metrics.Graph weaponsUsedGraph = metrics.createGraph("Guilds and users");

			weaponsUsedGraph.addPlotter(new Metrics.Plotter("Guilds") {

				@Override
				public int getValue() {
					return getGuildManager().getGuilds().size(); // Number of players who used a diamond sword
				}

			});

			weaponsUsedGraph.addPlotter(new Metrics.Plotter("Users") {

				@Override
				public int getValue() {
					return getPlayerManager().getPlayers().size();
				}

			});
			metrics.start();
		} catch (IOException e) {
			// Failed to submit the stats :-(
		}
	}
}
