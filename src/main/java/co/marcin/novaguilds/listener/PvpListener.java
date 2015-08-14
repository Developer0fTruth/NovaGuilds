package co.marcin.novaguilds.listener;

import co.marcin.novaguilds.NovaGuilds;
import co.marcin.novaguilds.basic.NovaPlayer;
import co.marcin.novaguilds.enums.Message;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PvpListener implements Listener {
	private final NovaGuilds plugin;
	
	public PvpListener(NovaGuilds novaGuilds) {
		plugin = novaGuilds;
		plugin.getServer().getPluginManager().registerEvents(this,plugin);
	}

	@EventHandler
	public void onPlayerAttack(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player) {
			Player attacker = null;
			Player player = (Player)event.getEntity();
			
			if(event.getDamager() instanceof Player) {
				attacker = (Player)event.getDamager();
			}
			else if(event.getDamager().getType() == EntityType.ARROW) {
				Arrow arrow = (Arrow)event.getDamager();
				
				if(arrow.getShooter() instanceof Player) {
					attacker = (Player)arrow.getShooter();
				}
			}
			
			if(attacker != null) {
				NovaPlayer novaPlayer = plugin.getPlayerManager().getPlayer(player);
				NovaPlayer novaPlayerAttacker = plugin.getPlayerManager().getPlayer(attacker);
				//teampvp
				if(!novaPlayerAttacker.getName().equals(novaPlayer.getName())) {
					if(novaPlayerAttacker.hasGuild() && novaPlayer.hasGuild()) {
						if(plugin.getPlayerManager().isGuildMate(player,attacker)) { //same guild
							if(!novaPlayer.getGuild().getFriendlyPvp()) {
								Message.CHAT_PVP_TEAM.send(attacker);
								event.setCancelled(true);

								//remove the arrow
								if(event.getDamager().getType() == EntityType.ARROW) {
									event.getDamager().remove();
								}
							}
						}
						else if(plugin.getPlayerManager().isAlly(player,attacker)) { //ally
							if(!(novaPlayer.getGuild().getFriendlyPvp() && novaPlayerAttacker.getGuild().getFriendlyPvp())) {
								Message.CHAT_PVP_ALLY.send(attacker);
								event.setCancelled(true);

								//remove the arrow
								if(event.getDamager().getType() == EntityType.ARROW) {
									event.getDamager().remove();
								}
							}
						}
					}
				}
			}
		}
	}
}
