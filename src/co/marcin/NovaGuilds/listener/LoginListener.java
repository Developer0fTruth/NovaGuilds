package co.marcin.NovaGuilds.listener;

import co.marcin.NovaGuilds.basic.NovaGuild;
import co.marcin.NovaGuilds.basic.NovaRaid;
import co.marcin.NovaGuilds.basic.NovaRegion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import co.marcin.NovaGuilds.NovaGuilds;
import co.marcin.NovaGuilds.basic.NovaPlayer;

import java.util.List;

public class LoginListener implements Listener {
	private final NovaGuilds plugin;
	
	public LoginListener(NovaGuilds plugin) {
		this.plugin = plugin;
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		//adding player
		plugin.getPlayerManager().addIfNotExists(player);

		NovaPlayer nPlayer = plugin.getPlayerManager().getPlayerByName(player.getName());

		//scoreboard
		player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());



		nPlayer.setPlayer(player);
		nPlayer.setOnline(true);

		//adding to raid TODO: not tested
		if(nPlayer.hasGuild()) {
			NovaRegion rgAtLocation = plugin.getRegionManager().getRegionAtLocation(player.getLocation());

			if(rgAtLocation != null) {
				NovaGuild guildAtRegion = plugin.getGuildManager().getGuildByRegion(rgAtLocation);

				List<NovaRaid> raidsTakingPart = plugin.getGuildManager().getRaidsTakingPart(nPlayer.getGuild());

				for(NovaRaid raid : raidsTakingPart) {
					if(raid.getGuildDefender().equals(guildAtRegion)){
						guildAtRegion.getRaid().addPlayerOccupying(nPlayer);
					}
				}
			}
		}
		
		//TabAPI
		plugin.tagUtils.updatePrefix(player);
		//plugin.sendTablistInfo(player); //TODO test
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		NovaPlayer nPlayer = plugin.getPlayerManager().getPlayerByPlayer(event.getPlayer());
		nPlayer.setOnline(false);
		nPlayer.setPlayer(null);

		//remove player from raid
		if(nPlayer.isPartRaid()) {
			for(NovaRaid raid : plugin.getGuildManager().getRaidsTakingPart(nPlayer.getGuild())) {
				raid.removePlayerOccupying(nPlayer);
			}
		}
	}
}
