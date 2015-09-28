package co.marcin.novaguilds.listener;

import co.marcin.novaguilds.NovaGuilds;
import co.marcin.novaguilds.basic.NovaPlayer;
import co.marcin.novaguilds.basic.NovaRaid;
import co.marcin.novaguilds.basic.Tablist;
import co.marcin.novaguilds.enums.Config;
import co.marcin.novaguilds.enums.Message;
import co.marcin.novaguilds.util.VersionUtils;
import co.marcin.novaguilds.util.reflect.PacketExtension;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LoginListener implements Listener {
	private final NovaGuilds plugin;
	
	public LoginListener(NovaGuilds plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this,plugin);
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		//adding player
		plugin.getPlayerManager().addIfNotExists(player);

		NovaPlayer nPlayer = plugin.getPlayerManager().getPlayer(player);

		//scoreboard
		player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

		nPlayer.setPlayer(player);
		plugin.getPlayerManager().updateUUID(nPlayer);

		if(VersionUtils.updateAvailable && player.hasPermission("novaguilds.admin.updateavailable")) {
			Message.CHAT_UPDATE.send(player);
		}

		//TODO not working
		if(Config.CHAT_DISPLAYNAMETAGS.getBoolean()) {
			player.setDisplayName(plugin.tagUtils.getTag(player)+player.getDisplayName());
		}

		//Show bank hologram
		if(nPlayer.hasGuild()) {
			nPlayer.getGuild().showVaultHologram(player);
		}

		if(plugin.getRegionManager().getRegion(player.getLocation()) != null) {
			plugin.getRegionManager().playerEnteredRegion(player,player.getLocation());
		}

		//TabAPI
		plugin.tagUtils.updatePrefix(player);

		//Tab
		if(Config.TABLIST_ENABLED.getBoolean()) {
			Tablist.patch();
			nPlayer.getTablist().send();
		}

		//PacketExtension
		if(Config.PACKETS_ENABLED.getBoolean()) {
			PacketExtension.registerPlayer(player);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		NovaPlayer nPlayer = plugin.getPlayerManager().getPlayer(event.getPlayer());
		nPlayer.setPlayer(null);

		//remove player from raid
		if(nPlayer.isPartRaid()) {
			for(NovaRaid raid : plugin.getGuildManager().getRaidsTakingPart(nPlayer.getGuild())) {
				raid.removePlayerOccupying(nPlayer);
			}
		}
	}
}
