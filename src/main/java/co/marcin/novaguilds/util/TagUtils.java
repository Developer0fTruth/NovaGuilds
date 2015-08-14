package co.marcin.novaguilds.util;

import co.marcin.novaguilds.NovaGuilds;
import co.marcin.novaguilds.basic.NovaGuild;
import co.marcin.novaguilds.basic.NovaPlayer;
import co.marcin.novaguilds.enums.Config;
import co.marcin.novaguilds.enums.Message;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TagUtils {
	private final NovaGuilds plugin;

	public TagUtils(NovaGuilds novaGuilds) {
		plugin = novaGuilds;
	}

	public String getTag(Player namedplayer) { //TODO deleted second arg Player player
		String tag = Config.GUILD_TAG.getString();
		String guildTag;
		String rank = "";
		NovaPlayer nPlayer = plugin.getPlayerManager().getPlayer(namedplayer);

		if(namedplayer.hasPermission("novaguilds.chat.notag") || !nPlayer.hasGuild()) {
			return "";
		}

		guildTag = nPlayer.getGuild().getTag();

		if(!Config.TAGAPI_COLORTAGS.getBoolean()) {
			guildTag = StringUtils.removeColors(guildTag);
		}

		tag = StringUtils.replace(tag, "{TAG}", guildTag);

		if(plugin.getConfig().getBoolean("tabapi.rankprefix")) {
			if(nPlayer.getGuild().getLeader().getName().equalsIgnoreCase(namedplayer.getName())) {
				rank = Message.CHAT_GUILDINFO_LEADERPREFIX.get();
			}
		}

		tag = StringUtils.replace(tag, "{RANK}", rank);

		return StringUtils.fixColors(tag);
	}

	@SuppressWarnings("deprecation")
	private static void setPrefix(OfflinePlayer player, String tag, Player p) {
		Scoreboard board = p.getScoreboard();
		Team team = board.getPlayerTeam(player);
		if(team == null) {
			String tName = "ng_"+player.getName();
			if(tName.length() > 16) {
				tName = tName.substring(0, 16);
			}

			team = board.registerNewTeam(tName);
			team.addPlayer(player);
		}

		team.setPrefix(StringUtils.fixColors(tag));
	}

	public void updatePrefix(Player p) {
		for(Player of : Bukkit.getOnlinePlayers()) {
			setPrefix(of, getTag(of), p);
		}
	}

	public void refreshAll() {
		for(Player player : Bukkit.getOnlinePlayers()) {
			updatePrefix(player);
		}
	}

	public void refreshGuild(NovaGuild guild) {
		if(guild != null) {
			for(Player player : guild.getOnlinePlayers()) {
				updatePrefix(player);
			}
		}
	}
}
