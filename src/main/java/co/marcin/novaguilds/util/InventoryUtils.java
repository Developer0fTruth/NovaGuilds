package co.marcin.novaguilds.util;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

public class InventoryUtils {
	public static void removeItems(Player player, List<ItemStack> items) {
		if(player.getGameMode() != GameMode.CREATIVE) {
			for(ItemStack item : items) {
				player.getInventory().removeItem(item);
			}
		}
	}

	public static boolean containsItems(Inventory inventory, List<ItemStack> items) {
		return getMissingItems(inventory, items).isEmpty();
	}

	public static List<ItemStack> getMissingItems(Inventory inventory, List<ItemStack> items) {
		List<ItemStack> missing = new ArrayList<>();

		if(items != null && inventory.getType() != InventoryType.CREATIVE) {
			for(ItemStack item : items) {
				if(!inventory.containsAtLeast(item, item.getAmount())) {
					ItemStack missingItemStack = item.clone();
					missingItemStack.setAmount(item.getAmount() - getTotalAmountOfItemStackInInventory(inventory, item));
					missing.add(missingItemStack);
				}
			}
		}

		return missing;
	}

	public static int getTotalAmountOfItemStackInInventory(Inventory inventory, ItemStack itemStack) {
		int amount = 0;

		for(ItemStack item : inventory.getContents()) {
			if(item != null && item.getType() != Material.AIR) {
				if(item.isSimilar(itemStack)) {
					amount += item.getAmount();
				}
			}
		}

		return amount;
	}

	public static boolean isEmpty(Inventory inventory) {
		for(ItemStack itemStack : inventory.getContents()) {
			if(itemStack!= null && itemStack.getType() != Material.AIR) {
				return false;
			}
		}
		return true;
	}

	public static Inventory getClickedInventory(InventoryClickEvent event) {
		int slot = event.getRawSlot();
		InventoryView view = event.getView();

		if(slot < 0) {
			return null;
		}
		else if(view.getTopInventory() != null && slot < view.getTopInventory().getSize()) {
			return view.getTopInventory();
		}
		else {
			return view.getBottomInventory();
		}
	}
}
