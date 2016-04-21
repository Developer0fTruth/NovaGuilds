/*
 *     NovaGuilds - Bukkit plugin
 *     Copyright (C) 2016 Marcin (CTRL) Wieczorek
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package co.marcin.novaguilds.impl.util.guiinventory.guild.player;

import co.marcin.novaguilds.api.basic.NovaPlayer;
import co.marcin.novaguilds.api.basic.NovaRank;
import co.marcin.novaguilds.enums.GuildPermission;
import co.marcin.novaguilds.impl.util.guiinventory.guild.rank.GUIInventoryGuildRankList;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GUIInventoryGuildPlayerSettingsRank extends GUIInventoryGuildRankList {
	private final NovaPlayer nPlayer;

	/**
	 * The constructor
	 *
	 * @param nPlayer player who's rank are being set
	 */
	public GUIInventoryGuildPlayerSettingsRank(NovaPlayer nPlayer) {
		super(nPlayer.getGuild());
		this.nPlayer = nPlayer;
	}

	@Override
	public void onClick(InventoryClickEvent event) {
		if(getViewer().hasPermission(GuildPermission.RANK_SET)) {
			NovaRank rank = slotRanksMap.get(event.getRawSlot());

			if(rank != null) {
				nPlayer.setGuildRank(rank);
			}
		}
	}

	@Override
	public void onOpen() {
		getInventory().remove(addRankItem);
	}
}
