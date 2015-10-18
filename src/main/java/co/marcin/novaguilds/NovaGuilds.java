package co.marcin.novaguilds;

import co.marcin.mchttp.McHTTP;
import co.marcin.novaguilds.api.NovaGuildsAPI;
import co.marcin.novaguilds.basic.NovaGuild;
import co.marcin.novaguilds.basic.NovaPlayer;
import co.marcin.novaguilds.basic.NovaRaid;
import co.marcin.novaguilds.enums.Config;
import co.marcin.novaguilds.enums.DataStorageType;
import co.marcin.novaguilds.enums.EntityUseAction;
import co.marcin.novaguilds.enums.Message;
import co.marcin.novaguilds.listener.*;
import co.marcin.novaguilds.manager.*;
import co.marcin.novaguilds.runnable.RunnableAutoSave;
import co.marcin.novaguilds.runnable.RunnableLiveRegeneration;
import co.marcin.novaguilds.util.*;
import co.marcin.novaguilds.util.reflect.PacketExtension;
import com.earth2me.essentials.Essentials;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import me.confuser.barapi.BarAPI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.kitteh.vanish.VanishPlugin;
import org.mcstats.Metrics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NovaGuilds extends JavaPlugin implements NovaGuildsAPI {
	/*
	* Dioricie nasz, ktorys jest w javie, swiec sie bugi Twoje, przyjdz ficzery Twoje,
	* badz kod Twoj jako w gicie tak i w mavenie, stacktrace naszego powszedniego
	* daj nam dzisiaj, i daj nam buildy Twoje, jako i my commity dajemy,
	* i nie wodz nas na wycieki pamieci, ale daj nam Bugi.
	* Escape. ~Bukkit.PL
	* */

	private static NovaGuilds inst;
	private final int build = Integer.parseInt(getDescription().getVersion());

	//Vault
	public Economy econ = null;

	private final GuildManager guildManager = new GuildManager(this);
	private final RegionManager regionManager = new RegionManager(this);
	private final PlayerManager playerManager = new PlayerManager(this);
	private final MessageManager messageManager = new MessageManager(this);
	private CommandManager commandManager;
	private ConfigManager configManager;
	private GroupManager groupManager;
	private FlatDataManager flatDataManager;
	private static final String logPrefix = "[NovaGuilds]";

	public TagUtils tagUtils;

	public final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
	public final List<NovaGuild> guildRaids = new ArrayList<>();

	//Database
	private DatabaseManager databaseManager;
	private VanishPlugin vanishNoPacket;
	private HologramManager hologramManager = new HologramManager(new File(getDataFolder(), "holograms.yml"));
	private McHTTP mcHTTP;

	public void onEnable() {
		inst = this;

		//managers
		configManager = new ConfigManager(this);

		//Essentials locale detection
		Essentials essentials = (Essentials) getServer().getPluginManager().getPlugin("Essentials");

		if(essentials != null && !Config.LANG_OVERRIDEESSENTIALS.getBoolean()) {
			String locale = essentials.getSettings().getLocale();
			if(locale.isEmpty()) {
				locale = "en";
			}

			if(ConfigManager.essentialsLocale.containsKey(locale)) {
				Config.LANG_NAME.set(ConfigManager.essentialsLocale.get(locale));
			}

			LoggerUtils.info("Changed lang to Essentials' locale: "+Config.LANG_NAME.getString());
		}

		if(!getMessageManager().load()) {
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		LoggerUtils.info("Messages loaded");

		commandManager = new CommandManager(this);
		groupManager = new GroupManager(this);

		tagUtils = new TagUtils(this);
		databaseManager = new DatabaseManager(this);

		if(!checkDependencies()) {
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		//Version check
		VersionUtils.checkVersion();

		int attempts = 0;
		while(!databaseManager.isConnected()) {
			if(attempts == 2) {
				LoggerUtils.error("Tried to connect twice but failed.");
				break;
			}

			LoggerUtils.info("Connecting to " + getConfigManager().getDataStorageType().name() + " storage");
			attempts++;

			if(getConfigManager().getDataStorageType() == DataStorageType.MYSQL) {
				databaseManager.connectToMysql();
			}

			if(getConfigManager().getDataStorageType() == DataStorageType.SQLITE) {
				databaseManager.connectToSQLite();
			}

			if(getConfigManager().getDataStorageType() == DataStorageType.FLAT) {
				flatDataManager = new FlatDataManager(this);
				if(flatDataManager.isConnected()) {
					LoggerUtils.info("Connected to FLAT storage.");
					break;
				}
				else {
					getConfigManager().setToSecondaryDataStorageType();
				}
			}
		}

		//Data loading
		getRegionManager().load();
		LoggerUtils.info("Regions data loaded");
		getGuildManager().load();
		LoggerUtils.info("Guilds data loaded");
		getPlayerManager().load();
		LoggerUtils.info("Players data loaded");

		LoggerUtils.info("Post checks running");
		getGuildManager().postCheck();
		getRegionManager().postCheck();

		//HologramManager
		if(Config.HOLOGRAPHICDISPLAYS_ENABLED.getBoolean()) {
			hologramManager.load();
		}

		//Listeners
		new LoginListener(this);
		new ToolListener(this);
		new RegionInteractListener(this);
		new MoveListener(this);
		new ChatListener(this);
		new PvpListener(this);
		new DeathListener(this);
		new InventoryListener(this);
		new PlayerInfoListener(this);

		if(Config.PACKETS_ENABLED.getBoolean()) {
			new PacketListener(this);

			//Register players (for reload)
			for(Player p : Bukkit.getOnlinePlayers()) {
				PacketExtension.registerPlayer(p);
			}
		}
		else {
			if(Config.TABLIST_ENABLED.getBoolean()) {
				Config.TABLIST_ENABLED.set(false);
			}

			getServer().getPluginManager().registerEvents(new Listener() {
				@EventHandler
				public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
					co.marcin.novaguilds.event.PlayerInteractEntityEvent clickEvent = new co.marcin.novaguilds.event.PlayerInteractEntityEvent(event.getPlayer(), event.getRightClicked(), EntityUseAction.INTERACT);
					getServer().getPluginManager().callEvent(clickEvent);
				}
			}, this);
		}

		if(Config.VAULT_ENABLED.getBoolean()) {
			new VaultListener(this);
		}

		if(getConfigManager().useVanishNoPacket()) {
			new VanishListener(this);
		}

		//Tablist/tag update
		tagUtils.refreshAll();

		//save scheduler
		runSaveScheduler();
		LoggerUtils.info("Save scheduler is running");

		//live regeneration task
		runLiveRegenerationTask();
		LoggerUtils.info("Live regeneration task is running");

		//metrics
		setupMetrics();

		//HTTP Server
		if(Config.WWW_ENABLED.getBoolean()) {
			mcHTTP = new McHTTP();
			mcHTTP.setPort(Config.WWW_PORT.getInt());
			mcHTTP.prepareFiles();
			mcHTTP.start();
			LoggerUtils.info("HTTP Server started.");
		}

		LoggerUtils.info("#" + VersionUtils.buildCurrent + " Enabled");
	}
	
	public void onDisable() {
		getGuildManager().save();
		getRegionManager().save();
		getPlayerManager().save();
		LoggerUtils.info("Saved all data");

		//Save Holograms
		getHologramManager().save();

		if(Config.PACKETS_ENABLED.getBoolean()) {
			PacketExtension.unregisterNovaGuildsChannel();
		}

		//Stop schedulers
		worker.shutdown();

		//reset barapi
		if(Config.BARAPI_ENABLED.getBoolean()) {
			for(Player player : getServer().getOnlinePlayers()) {
				BarAPI.removeBar(player);
			}
		}

		//removing holograms
		if(Config.HOLOGRAPHICDISPLAYS_ENABLED.getBoolean()) {
			for(Hologram h : HologramsAPI.getHolograms(this)) {
				h.delete();
			}
		}
		
		for(Player p : getServer().getOnlinePlayers()) {
			NovaPlayer nPlayer = getPlayerManager().getPlayer(p);

			if(nPlayer != null) {
				Location l1 = nPlayer.getSelectedLocation(0);
				Location l2 = nPlayer.getSelectedLocation(1);

				if(l1 != null && l2 != null) {
					RegionUtils.sendSquare(p, l1, l2, null, (byte) -1);
					RegionUtils.setCorner(p, l1, null);
					RegionUtils.setCorner(p, l2, null);

					if(nPlayer.getSelectedRegion() != null) {
						RegionUtils.highlightRegion(p, nPlayer.getSelectedRegion(), null);
					}
				}
			}
		}

		//getConfigManager().disable();
		LoggerUtils.info("#" + VersionUtils.buildCurrent + " Disabled");
	}

	public static NovaGuilds getInstance() {
		return inst;
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

	public CommandManager getCommandManager() {
		return commandManager;
	}

	public MessageManager getMessageManager() {
		return messageManager;
	}

	public HologramManager getHologramManager() {
		return hologramManager;
	}

	public ConfigManager getConfigManager() {
		return configManager;
	}

	public GroupManager getGroupManager() {
		return groupManager;
	}

	public FlatDataManager getFlatDataManager() {
		return flatDataManager;
	}

	public DatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	//Vault economy
	private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        
        if(rsp == null) {
            return false;
        }
        
        econ = rsp.getProvider();
        return econ != null;
    }

	//VanishNoPacket
	private boolean checkVanishNoPacket() {
		Plugin p = getServer().getPluginManager().getPlugin("VanishNoPacket");

		if(p instanceof VanishPlugin) {
			vanishNoPacket = (VanishPlugin) p;
		}

		return vanishNoPacket != null;
	}
	
	private void runSaveScheduler() {
		worker.scheduleAtFixedRate(new RunnableAutoSave(this), Config.SAVEINTERVAL.getSeconds(), Config.SAVEINTERVAL.getSeconds(), TimeUnit.SECONDS);
	}

	private void runLiveRegenerationTask() {
		Runnable task = new RunnableLiveRegeneration(this);
		worker.scheduleAtFixedRate(task, Config.LIVEREGENERATION_TASKINTERVAL.getSeconds(), Config.LIVEREGENERATION_TASKINTERVAL.getSeconds(), TimeUnit.SECONDS);
	}

	private void setupMetrics() {
		try {
			Metrics metrics = new Metrics(this);
			Metrics.Graph weaponsUsedGraph = metrics.createGraph("Guilds and users");

			weaponsUsedGraph.addPlotter(new Metrics.Plotter("Guilds") {
				@Override
				public int getValue() {
					return getGuildManager().getGuilds().size();
				}
			});

			weaponsUsedGraph.addPlotter(new Metrics.Plotter("Users") {
				@Override
				public int getValue() {
					return getPlayerManager().getPlayers().size();
				}
			});

			metrics.start();
		}
		catch(IOException e) {
			LoggerUtils.info("Failed to update stats!");
			LoggerUtils.info(e.getMessage());
		}
	}

	public void showRaidBar(NovaRaid raid) {
		if(raid.getFinished()) {
			resetWarBar(raid.getGuildAttacker());
			resetWarBar(raid.getGuildDefender());
		}
		else {
			HashMap<String,String> vars = new HashMap<>();
			vars.put("DEFENDER", raid.getGuildDefender().getName());
			List<Player> players = raid.getGuildAttacker().getOnlinePlayers();
			players.addAll(raid.getGuildDefender().getOnlinePlayers());

			for(Player player : players) {
				if(Config.BARAPI_ENABLED.getBoolean()) {
					BarAPI.setMessage(player, Message.BARAPI_WARPROGRESS.vars(vars).get(), raid.getProgress());
				}
				else {
					//TODO
					if(raid.getProgress() == 0 || raid.getProgress()%10 == 0 || raid.getProgress() >= 90) {
						String lines;
						if(raid.getProgress() == 0) {
							lines = "&f";
						}
						else {
							lines = "&4";
						}

						for(int i=1; i<=100; i++) {
							lines += "|";
							if(i == raid.getProgress()) {
								lines += "&f";
							}
						}

						MessageManager.sendPrefixMessage(player, lines);
					}
				}
			}
		}
	}

	public void resetWarBar(NovaGuild guild) {
		if(Config.BARAPI_ENABLED.getBoolean()) {
			for(Player player : guild.getOnlinePlayers()) {
				BarAPI.removeBar(player);
			}
		}
	}

	private boolean checkDependencies() {
		//Vault Economy
		if(getServer().getPluginManager().getPlugin("Vault") == null) {
			LoggerUtils.error("Disabled due to no Vault dependency found!");
			Config.HOLOGRAPHICDISPLAYS_ENABLED.set(false);
			Config.BARAPI_ENABLED.set(false);
			return false;
		}
		LoggerUtils.info("Vault hooked");

		if(!setupEconomy()) {
			LoggerUtils.error("Could not setup Vault's economy, disabling");
			Config.HOLOGRAPHICDISPLAYS_ENABLED.set(false);
			Config.BARAPI_ENABLED.set(false);
			return false;
		}
		LoggerUtils.info("Vault's Economy hooked");

		//HolographicDisplays
		if(Config.HOLOGRAPHICDISPLAYS_ENABLED.getBoolean()) {
			if(getServer().getPluginManager().getPlugin("HolographicDisplays") == null) {
				LoggerUtils.error("Couldn't find HolographicDisplays plugin, disabling this feature.");
				Config.HOLOGRAPHICDISPLAYS_ENABLED.set(false);
			}
			else {
				LoggerUtils.info("HolographicDisplays hooked");
			}
		}

		//BarAPI
		if(Config.BARAPI_ENABLED.getBoolean()) {
			if(getServer().getPluginManager().getPlugin("BarAPI") == null) {
				LoggerUtils.error("Couldn't find BarAPI plugin, disabling this feature.");
				Config.BARAPI_ENABLED.set(false);
			}
			else {
				LoggerUtils.info("BarAPI hooked");
			}
		}

		//VanishNoPacket
		if(checkVanishNoPacket()) {
			LoggerUtils.info("VanishNoPacket hooked");
		}
		else {
			LoggerUtils.info("VanishNoPacket not found, support disabled");
			getConfigManager().disableVanishNoPacket();
		}

		return true;
	}

	public int getBuild() {
		return build;
	}

	public static String getLogPrefix() {
		return logPrefix;
	}

	public void setConfigManager(ConfigManager configManager) {
		this.configManager = configManager;
	}

	public void setCommandManager(CommandManager commandManager) {
		this.commandManager = commandManager;
	}

	public ScheduledExecutorService getWorker() {
		return worker;
	}

	public VanishPlugin getVanishNoPacket() {
		return vanishNoPacket;
	}
}
