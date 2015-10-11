package co.marcin.novaguilds.enums;

import co.marcin.novaguilds.NovaGuilds;
import co.marcin.novaguilds.manager.ConfigManager;
import co.marcin.novaguilds.util.StringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@SuppressWarnings("unchecked")
public enum Config {
	MYSQL_HOST,
	MYSQL_PORT,
	MYSQL_USERNAME,
	MYSQL_PASSWORD,
	MYSQL_DATABASE,
	MYSQL_PREFIX,

	DATASTORAGE_PRIMARY,
	DATASTORAGE_SECONDARY,

	LANG,
	DEBUG,

	BARAPI_ENABLED,
	BARAPI_RAIDBAR,

	TABAPI_COLORTAGS,
	TABAPI_RANKPREFIX,

	TAGAPI_RANKPREFIX,
	TAGAPI_ALLYCOLOR_ENABLED,
	TAGAPI_ALLYCOLOR_COLOR,
	TAGAPI_GUILDCOLOR_ENABLED,
	TAGAPI_GUILDCOLOR_COLOR,
	TAGAPI_WARCOLOR_ENABLED,
	TAGAPI_WARCOLOR_COLOR,
	TAGAPI_COLORTAGS,

	HOLOGRAPHICDISPLAYS_ENABLED,

	PACKETS_ENABLED,

	WWW_ENABLED,
	WWW_PORT,

	LIVEREGENERATION_REGENTIME,

	CHAT_DISPLAYNAMETAGS,
	CHAT_ALLY_PREFIX,
	CHAT_ALLY_ENABLED,
	CHAT_ALLY_COLORTAGS,
	CHAT_ALLY_LEADERPREFIX,
	CHAT_ALLY_FORMAT,
	CHAT_ALLY_MSGPREFIX,
	CHAT_GUILD_PREFIX,
	CHAT_GUILD_ENABLED,
	CHAT_GUILD_LEADERPREFIX,
	CHAT_GUILD_FORMAT,
	CHAT_GUILD_MSGPREFIX,

	GUILD_INACTIVETIME,
	GUILD_FROMSPAWN,
	GUILD_ALLOWEDCHARS,
	GUILD_SETTINGS_TAG_COLOR,
	GUILD_SETTINGS_TAG_MIN,
	GUILD_SETTINGS_TAG_MAX,
	GUILD_SETTINGS_NAME_MIN,
	GUILD_SETTINGS_NAME_MAX,
	GUILD_KILLPOINTS,
	GUILD_DEATHPOINTS,
	GUILD_EFFECT_DURATION,
	GUILD_EFFECT_LIST,

	RAID_ENABLED,
	RAID_TIMEREST,
	RAID_TIMEINACTIVE,
	RAID_MINONLINE,
	RAID_POINTSTAKE,

	LIVEREGENERATION_TASKINTERVAL,

	SAVEINTERVAL,

	CLEANUP_ENABLED,
	CLEANUP_INACTIVETIME,
	CLEANUP_INTERVAL,

	VAULT_ENABLED,
	VAULT_ITEM,
	VAULT_ONLYLEADERTAKE,
	VAULT_HOLOGRAM_ENABLED,
	VAULT_HOLOGRAM_LINES,
	VAULT_DENYRELATIVE,

	USETITLES,

	REGION_AUTOREGION,
	REGION_MINSIZE,
	REGION_MAXSIZE,
	REGION_MINDISTANCE,
	REGION_TOOL,
	REGION_BLOCKEDCMDS,
	REGION_WATERFLOW,
	REGION_ALLYINTERACT,
	REGION_ADMINAUTOSIZE,
	REGION_BORDERPARTICLES,
	REGION_DENYINTERACT,
	REGION_DENYUSE,
	REGION_DENYMOBDAMAGE,
	REGION_DENYRIDING,

	REGION_MATERIALS_CHECK_HIGHLIGHT,
	REGION_MATERIALS_SELECTION_CORNER,
	REGION_MATERIALS_SELECTION_RECTANGLE,
	REGION_MATERIALS_RESIZE_CORNER,

	GUILD_CREATEPROTECTION,
	GUILD_MAXPLAYERS,
	GUILD_TAG,
	GUILD_HOMEFLOOR_ENABLED,
	GUILD_HOMEFLOOR_MATERIAL,

	GUILD_STARTPOINTS,
	GUILD_STARTLIVES,
	GUILD_STARTMONEY,
	GUILD_STARTSLOTS,

	GUILD_DISABLEDWORLDS,

	KILLING_STARTPOINTS,
	KILLING_RANKPERCENT,
	KILLING_COOLDOWN,
	KILLING_MONEYFORKILL,
	KILLING_MONEYFORREVENGE,

	TABLIST_ENABLED,
	TABLIST_SCHEME;

	private static final ConfigManager cM = NovaGuilds.getInstance()==null ? null : NovaGuilds.getInstance().getConfigManager();
	private final String path;

	Config() {
		path = StringUtils.replace(name(), "_", ".").toLowerCase();
	}

	public String getString() {
		String r = cM.isInCache(this) ? (String) cM.getEnumConfig(this) : cM.getString(path);
		cM.putInCache(this, r);
		return r;
	}

	public List<String> getStringList() {
		List<String> r = cM.isInCache(this) ? (List<String>) cM.getEnumConfig(this) : cM.getStringList(path);
		cM.putInCache(this, r);
		return r;
	}

	public List<ItemStack> getItemStackList() {
		List<ItemStack> r = cM.isInCache(this) ? (List<ItemStack>) cM.getEnumConfig(this) : cM.getItemStackList(path);
		cM.putInCache(this, r);
		return r;
	}

	public List<Material> getMaterialList() {
		List<Material> r = cM.isInCache(this) ? (List<Material>) cM.getEnumConfig(this) : cM.getMaterialList(path);
		cM.putInCache(this, r);
		return r;
	}

	public long getLong() {
		long r = cM.isInCache(this) ? (long) cM.getEnumConfig(this) : cM.getLong(path);
		cM.putInCache(this, r);
		return r;
	}

	public double getDouble() {
		double r = cM.isInCache(this) ? (double) cM.getEnumConfig(this) : cM.getDouble(path);
		cM.putInCache(this, r);
		return r;
	}

	public int getInt() {
		int r = cM.isInCache(this) ? (int) cM.getEnumConfig(this) : cM.getInt(path);
		cM.putInCache(this, r);
		return r;
	}

	public boolean getBoolean() {
		boolean r = cM.isInCache(this) ? (boolean) cM.getEnumConfig(this) : cM.getBoolean(path);
		cM.putInCache(this, r);
		return r;
	}

	public int getSeconds() {
		int r = cM.isInCache(this) ? (int) cM.getEnumConfig(this) : cM.getSeconds(path);
		cM.putInCache(this, r);
		return r;
	}

	public ItemStack getItemStack() {
		ItemStack r = cM.isInCache(this) ? (ItemStack) cM.getEnumConfig(this) : cM.getItemStack(path);
		cM.putInCache(this, r);
		return r;
	}

	public Material getMaterial() {
		Material r = cM.isInCache(this) ? (Material) cM.getEnumConfig(this) : cM.getMaterial(path);
		cM.putInCache(this, r);
		return r;
	}

	public static ConfigManager getManager() {
		return cM;
	}

	public void set(Object obj) {
		cM.set(path, obj);
	}

	public static Config fromPath(String path) {
		try {
			return Config.valueOf(StringUtils.replace(path, ".", "_").toUpperCase());
		}
		catch(Exception e) {
			return null;
		}
	}
}
