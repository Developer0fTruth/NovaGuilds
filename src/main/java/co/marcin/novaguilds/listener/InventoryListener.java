package co.marcin.novaguilds.listener;

import co.marcin.novaguilds.NovaGuilds;
import co.marcin.novaguilds.basic.NovaPlayer;
import co.marcin.novaguilds.util.LoggerUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class InventoryListener implements Listener {
	private final NovaGuilds plugin;

	public InventoryListener(NovaGuilds novaGuilds) {
		plugin = novaGuilds;
		plugin.getServer().getPluginManager().registerEvents(this,plugin);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		String nameRequiredItems = plugin.getMessageManager().getMessagesString("inventory.requireditems.name");
		String nameGGUI = plugin.getMessageManager().getMessagesString("inventory.ggui.name");
		Player player = (Player) event.getWhoClicked();

		//1.8
		if(event.getClickedInventory() != null && event.getCurrentItem() != null && event.getCurrentItem().getType()!= Material.AIR) {
			if(event.getInventory().getName().equals(nameRequiredItems) || event.getInventory().getName().equals(nameGGUI)) {
				if(event.getClickedInventory().equals(event.getView().getTopInventory()) || event.isShiftClick()) {
					//gui
					if(event.getInventory().getTitle().equals(nameGGUI)) {
						ItemStack clickedItem = event.getCurrentItem();

						String menuCommand = plugin.getCommandManager().getGuiCommand(clickedItem);

						player.chat("/" + menuCommand);
						event.setCancelled(true);
					}
				}
			}
		}

		//1.7
//		if(event.getInventory() != null && event.getCurrentItem() != null && event.getCurrentItem().getType()!= Material.AIR) {
//			if(event.getInventory().getName().equals(nameRequiredItems) || event.getInventory().getName().equals(nameGGUI)) {
//				if(event.getInventory().getTitle().equals(nameGGUI)) {
//					ItemStack clickedItem = event.getCurrentItem();
//
//					String menuCommand = plugin.getCommandManager().getGuiCommand(clickedItem);
//					player.chat("/"+menuCommand);
//					player.closeInventory();
//				}

//				event.setCancelled(true);
//
//			}
//		}
	}
}
