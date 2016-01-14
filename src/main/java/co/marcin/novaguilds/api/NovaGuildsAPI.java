/*
 *     NovaGuilds - Bukkit plugin
 *     Copyright (C) 2015 Marcin (CTRL) Wieczorek
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

package co.marcin.novaguilds.api;

import co.marcin.novaguilds.manager.CommandManager;
import co.marcin.novaguilds.manager.ConfigManager;
import co.marcin.novaguilds.manager.FlatDataManager;
import co.marcin.novaguilds.manager.GroupManager;
import co.marcin.novaguilds.manager.GuildManager;
import co.marcin.novaguilds.manager.HologramManager;
import co.marcin.novaguilds.manager.MessageManager;
import co.marcin.novaguilds.manager.PlayerManager;
import co.marcin.novaguilds.manager.RankManager;
import co.marcin.novaguilds.manager.RegionManager;

/**
 * NovaGuilds API
 * @author Marcin Wieczorek
 */
public interface NovaGuildsAPI {
	/**
	 * Returns the instance of RegionManager
	 * @return RegionManager
	 */
	RegionManager getRegionManager();

	/**
	 * Returns the instance of GuildManager
	 * @return GuildManager
	 */
	GuildManager getGuildManager();

	/**
	 * Returns the instance of PlayerManager
	 * @return PlayerManager
	 */
	PlayerManager getPlayerManager();

	/**
	 * Returns the instance of MessageManager
	 * @return MessageManager
	 */
	MessageManager getMessageManager();

	/**
	 * Returns the instance of CommandManager
	 * @return CommandManager
	 */
	CommandManager getCommandManager();

	/**
	 * Returns the instance of ConfigManager
	 * @return ConfigManager
	 */
	ConfigManager getConfigManager();

	/**
	 * Returns the instance of GroupManager
	 * @return GroupManager
	 */
	GroupManager getGroupManager();

	/**
	 * Returns the instance of FlatDataManager
	 * @return FlatDataManager
	 */
	FlatDataManager getFlatDataManager();

	/**
	 * Returns the instance of HologramManager
	 * @return HologramManager
	 */
	HologramManager getHologramManager();

	/**
	 * Returns the instance of RankManager
	 * @return RankManager
	 */
	RankManager getRankManager();

	/**
	 * Returns the build of the plugin
	 * @return current plugin version
	 */
	int getBuild();
}
