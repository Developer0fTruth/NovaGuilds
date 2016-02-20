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

package co.marcin.novaguilds.impl.basic;

import co.marcin.novaguilds.NovaGuilds;
import co.marcin.novaguilds.api.basic.GUIInventory;
import co.marcin.novaguilds.api.basic.NovaGuild;
import co.marcin.novaguilds.api.basic.NovaPlayer;
import co.marcin.novaguilds.api.basic.NovaRaid;
import co.marcin.novaguilds.api.basic.TabList;
import co.marcin.novaguilds.basic.NovaRank;
import co.marcin.novaguilds.basic.NovaRegion;
import co.marcin.novaguilds.enums.ChatMode;
import co.marcin.novaguilds.enums.Command;
import co.marcin.novaguilds.enums.Config;
import co.marcin.novaguilds.enums.GuildPermission;
import co.marcin.novaguilds.enums.Message;
import co.marcin.novaguilds.enums.RegionMode;
import co.marcin.novaguilds.runnable.CommandExecutorHandler;
import co.marcin.novaguilds.util.NumberUtils;
import co.marcin.novaguilds.util.RegionUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class NovaPlayerImpl implements co.marcin.novaguilds.api.basic.NovaPlayer {
	private int id;
	private Player player;
	private NovaGuild guild;
	private String name;
	private final UUID uuid;
	private int points;
	private int kills;
	private int deaths;

	private List<NovaGuild> invitedTo = new ArrayList<>();
	private RegionMode regionMode = RegionMode.CHECK;
	private boolean bypass = false;
	private NovaRegion selectedRegion;
	private NovaRegion atRegion;
	private NovaRaid partRaid;
	private boolean changed = false;
	private int resizingCorner = 0;
	private boolean compassPointingGuild = false;
	private final HashMap<UUID, Long> killingHistory = new HashMap<>();
	private TabList tabList;
	private CommandExecutorHandler commandExecutorHandler;
	private final List<Vehicle> vehicles = new ArrayList<>();
	private final List<GUIInventory> guiInventoryHistory = new ArrayList<>();
	private NovaRank guildRank;
	private ChatMode chatMode = ChatMode.NORMAL;
	private boolean spyMode = false;
	private final Location[] regionSelectedLocations = new Location[2];

	public NovaPlayerImpl(UUID uuid) {
		this.uuid = uuid;
	}

	public static NovaPlayer fromPlayer(Player player) {
		if(player != null) {
			NovaPlayer nPlayer = new NovaPlayerImpl(player.getUniqueId());
			nPlayer.setName(player.getName());
			nPlayer.setPlayer(player);
			return nPlayer;
		}
		return null;
	}

	//getters
	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public NovaGuild getGuild() {
		return guild;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<NovaGuild> getInvitedTo() {
		return invitedTo;
	}

	@Override
	public UUID getUUID() {
		return uuid;
	}

	@Override
	public Location getSelectedLocation(int index) {
		return regionSelectedLocations[index];
	}

	@Override
	public NovaRegion getSelectedRegion() {
		return selectedRegion;
	}

	@Override
	public boolean getBypass() {
		return bypass;
	}

	@Override
	public NovaRegion getAtRegion() {
		return atRegion;
	}

	@Override
	public int getResizingCorner() {
		return resizingCorner;
	}

	@Override
	public int getPoints() {
		return points;
	}

	@Override
	public int getDeaths() {
		return deaths;
	}

	@Override
	public int getKills() {
		return kills;
	}

	@Override
	public double getKillDeathRate() {
		return NumberUtils.roundOffTo2DecPlaces((double) getKills() / (getDeaths() == 0 ? 1 : (double) getDeaths()));
	}

	@Override
	public double getMoney() {
		return NovaGuilds.getInstance().econ.getBalance(name);
	}

	@Override
	public RegionMode getRegionMode() {
		return regionMode;
	}

	@Override
	public TabList getTabList() {
		return tabList;
	}

	@Override
	public CommandExecutorHandler getCommandExecutorHandler() {
		return commandExecutorHandler;
	}

	@Override
	public NovaRaid getPartRaid() {
		return partRaid;
	}

	@Override
	public GUIInventory getGuiInventory() {
		return guiInventoryHistory.isEmpty() ? null : guiInventoryHistory.get(guiInventoryHistory.size() - 1);
	}

	@Override
	public List<GUIInventory> getGuiInventoryHistory() {
		return guiInventoryHistory;
	}

	@Override
	public NovaRank getGuildRank() {
		return guildRank;
	}

	@Override
	public ChatMode getChatMode() {
		return chatMode;
	}

	@Override
	public boolean getSpyMode() {
		return spyMode;
	}

	@Override
	public int getId() {
		return id;
	}

	//setters
	@Override
	public void setGuild(NovaGuild guild) {
		this.guild = guild;
		changed = true;
	}

	@Override
	public void setPlayer(Player player) {
		this.player = player;
	}

	@Override
	public void setName(String name) {
		this.name = name;
		changed = true;
	}

	@Override
	public void setInvitedTo(List<NovaGuild> invitedTo) {
		this.invitedTo = invitedTo;
		changed = true;
	}

	@Override
	public void setRegionMode(RegionMode regionMode) {
		this.regionMode = regionMode;
	}

	@Override
	public void setSelectedLocation(int index, Location location) {
		regionSelectedLocations[index] = location;
	}

	@Override
	public void setSelectedRegion(NovaRegion region) {
		selectedRegion = region;
	}

	@Override
	public void setAtRegion(NovaRegion region) {
		atRegion = region;
	}

	@Override
	public void setUnchanged() {
		changed = false;
	}

	@Override
	public void setResizingCorner(int index) {
		resizingCorner = index;
	}

	@Override
	public void setPoints(int points) {
		this.points = points;
		changed = true;
	}

	@Override
	public void setCompassPointingGuild(boolean compassPointingGuild) {
		this.compassPointingGuild = compassPointingGuild;
	}

	@Override
	public void setDeaths(int deaths) {
		this.deaths = deaths;
		changed = true;
	}

	@Override
	public void setKills(int kills) {
		this.kills = kills;
		changed = true;
	}

	@Override
	public void setTabList(TabList tabList) {
		this.tabList = tabList;
	}

	@Override
	public void toggleBypass() {
		bypass = !bypass;
	}

	@Override
	public void setPartRaid(NovaRaid partRaid) {
		this.partRaid = partRaid;
	}

	@Override
	public void setGuiInventory(GUIInventory guiInventory) {
		if(guiInventory == null) {
			removeLastGUIInventoryHistory();
			return;
		}

		if(!guiInventory.equals(getGuiInventory())) {
			guiInventoryHistory.add(guiInventory);
		}
	}

	@Override
	public void setGuildRank(NovaRank guildRank) {
		if(this.guildRank != null) {
			this.guildRank.removeMember(this);
		}

		if(guildRank != null) {
			guildRank.addMember(this);

			if(!hasPermission(GuildPermission.REGION_CREATE) && !hasPermission(GuildPermission.REGION_RESIZE)) {
				cancelToolProgress();
			}
		}

		this.guildRank = guildRank;
	}

	@Override
	public void setChatMode(ChatMode chatMode) {
		this.chatMode = chatMode;
	}

	@Override
	public void setSpyMode(boolean spyMode) {
		this.spyMode = spyMode;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}
	
	//check stuff
	@Override
	public boolean isCompassPointingGuild() {
		return compassPointingGuild;
	}

	@Override
	public boolean hasGuild() {
		return getGuild() != null;
	}

	@Override
	public boolean isOnline() {
		return player != null;
	}

	@Override
	public boolean isChanged() {
		return changed;
	}

	@Override
	public boolean isInvitedTo(NovaGuild guild) {
		return invitedTo.contains(guild);
	}

	@Override
	public boolean isPartRaid() {
		return !(partRaid == null);
	}

	@Override
	public boolean isVehicleListed(Vehicle vehicle) {
		return vehicles.contains(vehicle);
	}

	@Override
	public boolean isLeader() {
		return hasGuild() && getGuild().isLeader(this);
	}

	@Override
	public boolean isAtRegion() {
		return atRegion != null;
	}

	@Override
	public boolean hasMoney(double money) {
		return getMoney() >= money;
	}

	@Override
	public boolean hasPermission(GuildPermission permission) {
		return guildRank != null && guildRank.hasPermission(permission);
	}

	@Override
	public boolean hasTabList() {
		return tabList != null;
	}

	@Override
	public boolean canGetKillPoints(Player player) {
		return !killingHistory.containsKey(player.getUniqueId()) || NumberUtils.systemSeconds() - killingHistory.get(player.getUniqueId()) > Config.KILLING_COOLDOWN.getSeconds();
	}
	
	//add stuff
	@Override
	public void addInvitation(NovaGuild guild) {
		if(!isInvitedTo(guild)) {
			invitedTo.add(guild);
			changed = true;
		}
	}

	@Override
	public void addPoints(int points) {
		this.points += points;
		changed = true;
	}

	@Override
	public void addKill() {
		kills++;
		changed = true;
	}

	@Override
	public void addDeath() {
		deaths++;
		changed = true;
	}

	@Override
	public void addMoney(double money) {
		NovaGuilds.getInstance().econ.depositPlayer(name, money);
	}

	@Override
	public void addKillHistory(Player player) {
		if(killingHistory.containsKey(player.getUniqueId())) {
			killingHistory.remove(player.getUniqueId());
		}

		killingHistory.put(player.getUniqueId(), NumberUtils.systemSeconds());
	}

	@Override
	public void addVehicle(Vehicle vehicle) {
		if(!isVehicleListed(vehicle)) {
			vehicles.add(vehicle);
		}
	}

	@Override
	public void newCommandExecutorHandler(Command command, String[] args) {
		commandExecutorHandler = new CommandExecutorHandler(command, getPlayer(), args);
		Message.CHAT_CONFIRM_NEEDCONFIRM.send(player);
	}
	
	//delete stuff
	@Override
	public void deleteInvitation(NovaGuild guild) {
		invitedTo.remove(guild);
		changed = true;
	}

	@Override
	public void takePoints(int points) {
		this.points -= points;
		changed = true;
	}

	@Override
	public void takeMoney(double money) {
		NovaGuilds.getInstance().econ.withdrawPlayer(name, money);
	}

	@Override
	public void cancelToolProgress() {
		if(isOnline()) {
			RegionUtils.sendRectangle(getPlayer(), getSelectedLocation(0), getSelectedLocation(1), null, (byte) 0);
			RegionUtils.setCorner(getPlayer(), getSelectedLocation(0), null, (byte) 0);
			RegionUtils.setCorner(getPlayer(), getSelectedLocation(1), null, (byte) 0);
			RegionUtils.highlightRegion(getPlayer(), getSelectedRegion(), null);

			setResizingCorner(0);
			setSelectedRegion(null);
			setSelectedLocation(0, null);
			setSelectedLocation(1, null);

			if(getRegionMode() == RegionMode.RESIZE) {
				setRegionMode(RegionMode.CHECK);
			}
		}
	}

	@Override
	public void removeCommandExecutorHandler() {
		commandExecutorHandler = null;
	}

	@Override
	public void removeLastGUIInventoryHistory() {
		getGuiInventoryHistory().remove(getGuiInventoryHistory().size() - 1);
	}
}
