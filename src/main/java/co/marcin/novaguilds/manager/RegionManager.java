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

package co.marcin.novaguilds.manager;

import co.marcin.novaguilds.NovaGuilds;
import co.marcin.novaguilds.api.basic.NovaGuild;
import co.marcin.novaguilds.basic.NovaPlayer;
import co.marcin.novaguilds.basic.NovaRegion;
import co.marcin.novaguilds.enums.Config;
import co.marcin.novaguilds.enums.DataStorageType;
import co.marcin.novaguilds.enums.Message;
import co.marcin.novaguilds.enums.PreparedStatements;
import co.marcin.novaguilds.enums.RegionValidity;
import co.marcin.novaguilds.enums.VarKey;
import co.marcin.novaguilds.impl.basic.NovaGuildImpl;
import co.marcin.novaguilds.runnable.RunnableRaid;
import co.marcin.novaguilds.util.LoggerUtils;
import co.marcin.novaguilds.util.NumberUtils;
import co.marcin.novaguilds.util.RegionUtils;
import co.marcin.novaguilds.util.StringUtils;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RegionManager {
	private static final NovaGuilds plugin = NovaGuilds.getInstance();

	//getters
	public NovaRegion getRegion(Location l) {
		int x = l.getBlockX();
		int z = l.getBlockZ();
		
		for(NovaRegion r : getRegions()) {
			
			Location c1 = r.getCorner(0);
			Location c2 = r.getCorner(1);
			
			if((x >= c1.getBlockX() && x <= c2.getBlockX()) || (x <= c1.getBlockX() && x >= c2.getBlockX())) {
				if((z >= c1.getBlockZ() && z <= c2.getBlockZ()) || (z <= c1.getBlockZ() && z >= c2.getBlockZ())) {
					return r;
				}
			}
		}
		
		return null;
	}
	
	public Collection<NovaRegion> getRegions() {
		Collection<NovaRegion> regions = new HashSet<>();

		for(NovaGuild guild : plugin.getGuildManager().getGuilds()) {
			if(guild.hasRegion()) {
				regions.add(guild.getRegion());
			}
		}

		return regions;
	}
	
	public void load() {
		for(NovaGuild guild : plugin.getGuildManager().getGuilds()) {
			guild.setRegion(null);
		}

		if(plugin.getConfigManager().getDataStorageType() == DataStorageType.FLAT) {
			for(String guildName : plugin.getFlatDataManager().getRegionList()) {
				FileConfiguration regionData = plugin.getFlatDataManager().getRegionData(guildName);
				NovaRegion region = regionFromFlat(regionData);

				if(region != null) {
					NovaGuild guild = NovaGuildImpl.get(guildName);

					if(guild == null) {
						LoggerUtils.error("There's no guild matching region " + guildName);
						continue;
					}

					guild.setRegion(region);
				}
				else {
					LoggerUtils.info("Loaded region is null. name: " + guildName);
				}
			}
		}
		else {
			plugin.getDatabaseManager().mysqlReload();

			if(!plugin.getDatabaseManager().isConnected()) {
				LoggerUtils.info("Connection is not estabilished, stopping current action");
				return;
			}

			try {
				PreparedStatement statement = plugin.getDatabaseManager().getPreparedStatement(PreparedStatements.REGIONS_SELECT);

				ResultSet res = statement.executeQuery();
				while(res.next()) {
					World world = plugin.getServer().getWorld(res.getString("world"));
					String guildName = res.getString("guild");
					NovaGuild guild = NovaGuildImpl.get(guildName);

					if(guild == null) {
						LoggerUtils.error("There's no guild matching region " + guildName);
						continue;
					}

					if(world != null) {
						NovaRegion region = new NovaRegion();

						String loc1 = res.getString("loc_1");
						String[] loc1_split = loc1.split(";");

						String loc2 = res.getString("loc_2");
						String[] loc2_split = loc2.split(";");

						Location c1 = new Location(world, Integer.parseInt(loc1_split[0]), 0, Integer.parseInt(loc1_split[1]));
						Location c2 = new Location(world, Integer.parseInt(loc2_split[0]), 0, Integer.parseInt(loc2_split[1]));

						region.setCorner(0, c1);
						region.setCorner(1, c2);
						region.setWorld(world);
						region.setId(res.getInt("id"));
						guild.setRegion(region);
						region.setUnChanged();

					}
					else {
						LoggerUtils.info("Failed loading region for guild " + res.getString("guild") + ", world does not exist.");
					}
				}
			}
			catch(SQLException e) {
				LoggerUtils.exception(e);
			}
		}

		LoggerUtils.info("Loaded " + getRegions().size() + " regions.");
	}
	
	public void add(NovaRegion region) {
		NovaGuild guild = region.getGuild();

		if(plugin.getConfigManager().getDataStorageType() == DataStorageType.FLAT) {
			plugin.getFlatDataManager().add(region);
		}
		else {
			if(!plugin.getDatabaseManager().isConnected()) {
				LoggerUtils.info("Connection is not estabilished, stopping current action");
				return;
			}

			plugin.getDatabaseManager().mysqlReload();

			try {
				String loc1 = StringUtils.parseDBLocationCoords2D(region.getCorner(0));
				String loc2 = StringUtils.parseDBLocationCoords2D(region.getCorner(1));

				if(guild == null) {
					LoggerUtils.error("addRegion w/o guild attempt");
					return;
				}

				if(region.getWorld() == null) {
					region.setWorld(plugin.getServer().getWorlds().get(0));
				}

				PreparedStatement preparedStatement = plugin.getDatabaseManager().getPreparedStatement(PreparedStatements.REGIONS_INSERT);
				preparedStatement.setString(1, loc1);
				preparedStatement.setString(2, loc2);
				preparedStatement.setString(3, guild.getName());
				preparedStatement.setString(4, region.getWorld().getName());
				preparedStatement.executeUpdate();

				ResultSet keys = preparedStatement.getGeneratedKeys();
				keys.next();
				int id = keys.getInt(1);

				if(id != 0) {
					region.setId(id);
				}
				else {
					throw new UnsupportedOperationException("Added region's ID is 0!");
				}
			}
			catch(SQLException e) {
				LoggerUtils.exception(e);
			}
		}

		guild.setRegion(region);
		region.setUnChanged();
	}
	
	public void save(NovaRegion region) {
		if(region != null) {
			if(region.isChanged()) {
				if(plugin.getConfigManager().getDataStorageType() == DataStorageType.FLAT) {
					plugin.getFlatDataManager().save(region);
				}
				else {
					if(!plugin.getDatabaseManager().isConnected()) {
						LoggerUtils.info("Connection is not estabilished, stopping current action");
						return;
					}

					plugin.getDatabaseManager().mysqlReload();
					try {
						PreparedStatement preparedStatement = plugin.getDatabaseManager().getPreparedStatement(PreparedStatements.REGIONS_UPDATE);

						String loc1 = StringUtils.parseDBLocationCoords2D(region.getCorner(0));
						String loc2 = StringUtils.parseDBLocationCoords2D(region.getCorner(1));

						preparedStatement.setString(1, loc1);
						preparedStatement.setString(2, loc2);
						preparedStatement.setString(3, region.getGuild().getName());
						preparedStatement.setString(4, region.getWorld().getName());
						preparedStatement.setInt(5, region.getId());
						preparedStatement.executeUpdate();

						region.setUnChanged();
					}
					catch(SQLException e) {
						LoggerUtils.exception(e);
					}
				}
			}
		}
		else {
			LoggerUtils.info("null found while saving a region!");
		}
	}
	
	public void save() {
		long startTime = System.nanoTime();
		int count = 0;

		for(NovaRegion region : getRegions()) {
			if(region.isChanged()) {
				count++;
			}

			save(region);
		}

		LoggerUtils.info("Regions data saved in " + TimeUnit.MILLISECONDS.convert((System.nanoTime() - startTime), TimeUnit.NANOSECONDS) / 1000.0 + "s (" + count + " regions)");
	}
	
	//delete region
	public void remove(NovaRegion region) {
		if(plugin.getConfigManager().getDataStorageType() == DataStorageType.FLAT) {
			plugin.getFlatDataManager().delete(region);
		}
		else {
			if(!plugin.getDatabaseManager().isConnected()) {
				LoggerUtils.info("Connection is not estabilished, stopping current action");
				return;
			}

			plugin.getDatabaseManager().mysqlReload();

			try {
				PreparedStatement preparedStatement = plugin.getDatabaseManager().getPreparedStatement(PreparedStatements.REGIONS_DELETE);
				preparedStatement.setInt(1, region.getId());
				preparedStatement.executeUpdate();
			}
			catch(SQLException e) {
				LoggerUtils.info("An error occured while deleting a guild's region (" + region.getGuild().getName() + ")");
				LoggerUtils.exception(e);
			}
		}

		if(region.getGuild() != null) {
			region.getGuild().setRegion(null);
		}
	}
	
	public RegionValidity checkRegionSelect(Location l1, Location l2) {
		int x1 = l1.getBlockX();
		int x2 = l2.getBlockX();
		int z1 = l1.getBlockZ();
		int z2 = l2.getBlockZ();
		
		int dif_x = Math.abs(x1 - x2) + 1;
		int dif_z = Math.abs(z1 - z2) + 1;
		
		int minsize = Config.REGION_MINSIZE.getInt();
		int maxsize = Config.REGION_MAXSIZE.getInt();

		if(dif_x < minsize || dif_z < minsize) {
			return RegionValidity.TOOSMALL;
		}
		else if(dif_x > maxsize || dif_z > maxsize) {
			return RegionValidity.TOOBIG;
		}
		else if(!getRegionsInsideArea(l1, l2).isEmpty()) {
			return RegionValidity.OVERLAPS;
		}
		else if(!isFarEnough(l1, l2)) {
			return RegionValidity.TOOCLOSE;
		}
		else {
			return RegionValidity.VALID;
		}
	}
	
	public List<NovaRegion> getRegionsInsideArea(Location l1, Location l2) {
		ArrayList<NovaRegion> list = new ArrayList<>();
		int x1 = l1.getBlockX();
		int x2 = l2.getBlockX();
		int z1 = l1.getBlockZ();
		int z2 = l2.getBlockZ();
		
		boolean i1;
		boolean i2;
		boolean i3;
		boolean i4;
		
		boolean ov1;
		boolean ov2;
		boolean overlaps;
		
		for(NovaRegion region : getRegions()) {
			Location c1 = region.getCorner(0);
			Location c2 = region.getCorner(1);
			
			//c1
			i1 = (c1.getBlockX() <= x1 && c1.getBlockX() >= x2) || (c1.getBlockX() >= x1 && c1.getBlockX() <= x2);
			i2 = (c1.getBlockZ() <= z1 && c1.getBlockZ() >= z2) || (c1.getBlockZ() >= z1 && c1.getBlockZ() <= z2);

			//c2
			i3 = (c2.getBlockX() <= x1 && c2.getBlockX() >= x2) || (c2.getBlockX() >= x1 && c2.getBlockX() <= x2);
			i4 = (c2.getBlockZ() <= z1 && c2.getBlockZ() >= z2) || (c2.getBlockZ() >= z1 && c2.getBlockZ() <= z2);
			
			ov1 = i1 && i2;
			ov2 = i3 && i4;
			
			overlaps = ov1 || ov2;
			
			if(overlaps) {
				list.add(region);
			}
		}
		
		return list;
	}

	public boolean canInteract(Player player, Location location) {
		NovaRegion region = getRegion(location);
		NovaPlayer nPlayer = plugin.getPlayerManager().getPlayer(player);
		return region == null || nPlayer.getBypass() || (nPlayer.hasGuild() && region.getGuild().isMember(nPlayer));
	}

	public boolean canInteract(Player player, Block block) {
		return canInteract(player, block.getLocation());
	}

	public boolean canInteract(Player player, Entity entity) {
		return canInteract(player, entity.getLocation());
	}

	private boolean isFarEnough(Location l1, Location l2) {
		return getGuildsTooClose(l1, l2).isEmpty();
	}

	public List<NovaGuild> getGuildsTooClose(Location l1, Location l2) {
		List<NovaGuild> list = new ArrayList<>();

		int width = Math.abs(l1.getBlockX() - l2.getBlockX()) + 1;
		int height = Math.abs(l1.getBlockZ() - l2.getBlockZ()) + 1;
		int radius1 = Math.round((int) Math.sqrt((int) (Math.pow(width, 2) + Math.pow(height, 2))) / 2);

		int min = radius1 + Config.REGION_MINDISTANCE.getInt();
		Location centerLocation = RegionUtils.getCenterLocation(l1, l2);

		for(NovaGuild guildLoop : plugin.getGuildManager().getGuilds()) {
			if(guildLoop.getHome().getWorld().equals(l1.getWorld())) {
				int radius2 = 0;

				if(guildLoop.hasRegion()) {
					radius2 = guildLoop.getRegion().getDiagonal() / 2;
				}

				centerLocation.setY(guildLoop.getHome().getY());

				double distance = centerLocation.distance(guildLoop.getHome());
				if(distance < min + radius2) {
					list.add(guildLoop);
				}
			}
		}

		return list;
	}

	public void playerEnteredRegion(Player player, Location toLocation) {
		if(plugin.getConfigManager().useVanishNoPacket()) {
			if(plugin.getVanishNoPacket().getManager().isVanished(player)) {
				return;
			}
		}

		NovaRegion region = getRegion(toLocation);
		NovaPlayer nPlayer = plugin.getPlayerManager().getPlayer(player);

		//border particles
		if(Config.REGION_BORDERPARTICLES.getBoolean()) {
			List<Block> blocks = RegionUtils.getBorderBlocks(region);
			for(Block block : blocks) {
				block.getLocation().setY(block.getLocation().getY() + 1);
				block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 100);
			}
		}

		//Chat message
		Map<VarKey, String> vars = new HashMap<>();
		vars.put(VarKey.GUILDNAME, region.getGuild().getName());
		vars.put(VarKey.PLAYERNAME, player.getName());
		Message.CHAT_REGION_ENTERED.vars(vars).send(player);

		//Player is at region
		nPlayer.setAtRegion(region);

		if(!region.getGuild().isMember(nPlayer)) {
			checkRaidInit(player);

			Message.CHAT_REGION_NOTIFYGUILD_ENTERED.vars(vars).broadcast(region.getGuild());
		}

		//Vehicle protection system
		Entity vehicle = player.getVehicle();
		if(vehicle != null && vehicle instanceof Vehicle) {
			nPlayer.addVehicle((Vehicle) vehicle);
		}
	}

	public void playerExitedRegion(Player player) {
		NovaRegion region = getRegion(player.getLocation());
		NovaPlayer nPlayer = plugin.getPlayerManager().getPlayer(player);

		if(region == null) {
			return;
		}

		NovaGuild guild = region.getGuild();

		nPlayer.setAtRegion(null);
		Message.CHAT_REGION_EXITED.setVar(VarKey.GUILDNAME, region.getGuild().getName()).send(player);

		if(nPlayer.hasGuild()) {
			if(nPlayer.getGuild().isWarWith(guild)) {
				if(guild.isRaid()) {
					guild.getRaid().removePlayerOccupying(nPlayer);

					if(guild.getRaid().getPlayersOccupyingCount() == 0) {
						guild.getRaid().resetProgress();
						guild.removeRaidBar();
						nPlayer.getGuild().removeRaidBar();
						guild.getRaid().updateInactiveTime();
					}
				}
			}
		}
	}

	private NovaRegion regionFromFlat(FileConfiguration regionData) {
		NovaRegion region = null;

		if(regionData != null) {
			World world = plugin.getServer().getWorld(regionData.getString("world"));

			if(world != null) {
				region = new NovaRegion();
				region.setWorld(world);

				Location c1 = new Location(world, regionData.getInt("corner1.x"), 0, regionData.getInt("corner1.z"));
				Location c2 = new Location(world, regionData.getInt("corner2.x"), 0, regionData.getInt("corner2.z"));

				region.setCorner(0, c1);
				region.setCorner(1, c2);
				region.setUnChanged();
			}
		}

		return region;
	}

	public void checkRaidInit(Player player) {
		NovaPlayer nPlayer = NovaPlayer.get(player);

		if(!Config.RAID_ENABLED.getBoolean() || !nPlayer.hasGuild() || !nPlayer.isAtRegion()) {
			return;
		}

		NovaGuild guildDefender = nPlayer.getAtRegion().getGuild();

		if(nPlayer.getGuild().isWarWith(guildDefender)) {
			if(guildDefender.isRaid()) {
				nPlayer.setPartRaid(guildDefender.getRaid());
				guildDefender.getRaid().addPlayerOccupying(nPlayer);
			}
			else {
				if(NumberUtils.systemSeconds() - Config.RAID_TIMEREST.getSeconds() > guildDefender.getTimeRest()) {
					if(guildDefender.getOnlinePlayers().size() >= Config.RAID_MINONLINE.getInt() || guildDefender.getOnlinePlayers().size() == guildDefender.getPlayers().size()) {
						if(NumberUtils.systemSeconds() - guildDefender.getTimeCreated() > Config.GUILD_CREATEPROTECTION.getSeconds()) {
							guildDefender.createRaid(nPlayer.getGuild());
							plugin.guildRaids.add(guildDefender);

							if(!NovaGuilds.isRaidRunnableRunning()) {
								Runnable task = new RunnableRaid();
								plugin.worker.schedule(task, 1, TimeUnit.SECONDS);
								NovaGuilds.setRaidRunnableRunning(true);
							}
						}
						else {
							Message.CHAT_RAID_PROTECTION.send(player);
						}
					}
				}
				else {
					final long timeWait = Config.RAID_TIMEREST.getSeconds() - (NumberUtils.systemSeconds() - guildDefender.getTimeRest());

					Message.CHAT_RAID_RESTING.setVar(VarKey.TIMEREST, StringUtils.secondsToString(timeWait)).send(player);
				}
			}
		}
	}
}
