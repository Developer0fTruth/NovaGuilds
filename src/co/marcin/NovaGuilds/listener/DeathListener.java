package co.marcin.NovaGuilds.listener;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import co.marcin.NovaGuilds.NovaGuild;
import co.marcin.NovaGuilds.NovaGuilds;
import co.marcin.NovaGuilds.NovaPlayer;
import co.marcin.NovaGuilds.Utils;

public class DeathListener implements Listener {
	private final NovaGuilds plugin;
	
	public DeathListener(NovaGuilds novaGuilds) {
		plugin = novaGuilds;
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {

		if(event.getEntity().getKiller() instanceof Player) {
			Player player = event.getEntity();
			Player attacker = event.getEntity().getKiller();
			
			NovaPlayer novaPlayer = plugin.getPlayerManager().getPlayerByName(player.getName());
			NovaPlayer novaPlayerAttacker = plugin.getPlayerManager().getPlayerByName(attacker.getName());
			
			String tag1 = "";
			String tag2 = "";
			String tagscheme = plugin.getConfig().getString("guild.tag");
			tagscheme = Utils.replace(tagscheme, "{RANK}","");
			
			if(novaPlayer.hasGuild()) {
				tag1 = Utils.replace(tagscheme, "{TAG}",novaPlayer.getGuild().getTag());
			}

			if(novaPlayerAttacker.hasGuild()) {
				tag2 = Utils.replace(tagscheme, "{TAG}",novaPlayerAttacker.getGuild().getTag());
			}
			
			HashMap<String,String> vars = new HashMap<>();
			vars.put("PLAYER1",player.getName());
			vars.put("PLAYER2",attacker.getName());
			vars.put("TAG1",tag1);
			vars.put("TAG2",tag2);
			plugin.broadcastMessage("broadcast.pvp.killed",vars);
			
			if(novaPlayer.hasGuild()) {
				NovaGuild guildVictim = plugin.getGuildManager().getGuildByPlayer(novaPlayer);
				guildVictim.takePoints(plugin.getConfig().getInt("guild.deathpoints"));
				plugin.getGuildManager().saveGuildLocal(guildVictim);
			}
			
			if(novaPlayerAttacker.hasGuild()) {
				NovaGuild guildAttacker = plugin.getGuildManager().getGuildByPlayer(novaPlayerAttacker);
				guildAttacker.addPoints(plugin.getConfig().getInt("guild.killpoints"));
				plugin.getGuildManager().saveGuildLocal(guildAttacker);
			}
			
			//disable death message
			event.setDeathMessage(null);
		}
	}
}
