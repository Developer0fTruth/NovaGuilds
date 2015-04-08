package co.marcin.NovaGuilds.Listeners;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import co.marcin.NovaGuilds.NovaGuilds;
import co.marcin.NovaGuilds.NovaPlayer;
import co.marcin.NovaGuilds.Utils;

public class PvpListener implements Listener {
	private NovaGuilds pl;
	
	public PvpListener(NovaGuilds novaGuilds) {
		pl = novaGuilds;
	}

	@EventHandler
	public void onPlayerAttack(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player) {
			Player attacker = null;
			Player player = (Player)event.getEntity();
			
			if(event.getDamager() instanceof Player) {
				attacker = (Player)event.getDamager();
			}
			else if(event.getDamager().getType().equals(EntityType.ARROW)) {
				Arrow arrow = (Arrow)event.getDamager();
				
				if(arrow.getShooter() instanceof Player) {
					attacker = (Player)arrow.getShooter();
				}
			}
			
			if(attacker != null) {
				NovaPlayer novaPlayer = pl.getPlayerManager().getPlayerByName(player.getName());
				NovaPlayer novaPlayerAttacker = pl.getPlayerManager().getPlayerByName(attacker.getName());
				//teampvp
				if(!novaPlayerAttacker.getName().equals(novaPlayer.getName())) {
					if(novaPlayerAttacker.hasGuild() && novaPlayer.hasGuild()) {
						if(novaPlayerAttacker.getGuild().equals(novaPlayer.getGuild())) {
							attacker.sendMessage(Utils.fixColors(pl.prefix+pl.getMessages().getString("chat.teampvp")));
							event.setCancelled(true);
							
							//remove the arrow
							if(event.getDamager().getType().equals(EntityType.ARROW)) {
								event.getDamager().remove();
							}
							return;
						}
						else if(novaPlayerAttacker.getGuild().isAlly(novaPlayer.getGuild())) {
							attacker.sendMessage(Utils.fixColors(pl.prefix+pl.getMessages().getString("chat.allypvp")));
							event.setCancelled(true);
							
							//remove the arrow
							if(event.getDamager().getType().equals(EntityType.ARROW)) {
								event.getDamager().remove();
							}
							return;
						}
					}
				}
			}
		}
	}
}
