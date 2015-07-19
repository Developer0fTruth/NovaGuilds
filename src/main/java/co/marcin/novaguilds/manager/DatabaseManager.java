package co.marcin.novaguilds.manager;

import co.marcin.novaguilds.NovaGuilds;
import co.marcin.novaguilds.enums.Config;
import co.marcin.novaguilds.enums.DataStorageType;
import co.marcin.novaguilds.enums.PreparedStatements;
import co.marcin.novaguilds.util.LoggerUtils;
import co.marcin.novaguilds.util.StringUtils;
import code.husky.mysql.MySQL;
import code.husky.sqlite.SQLite;

import java.io.InputStream;
import java.sql.*;
import java.util.HashMap;

public class DatabaseManager {
	private final NovaGuilds plugin;
	private long mySQLReconnectStamp = System.currentTimeMillis();
	private MySQL mySQL;
	private Connection connection = null;
	private boolean connected = false;
	private final HashMap<PreparedStatements,PreparedStatement> preparedStatementMap = new HashMap<>();

	public DatabaseManager(NovaGuilds novaGuilds) {
		plugin = novaGuilds;
	}

	private void prepareStatements() {
		try {
			long nanoTimeStart = System.nanoTime();
			LoggerUtils.info("Preparing statements...");
			preparedStatementMap.clear();

			//Guilds insert (id, tag, name, leader, spawn, allies, alliesinv, war, nowarinv, money, points, lives, timerest, lostlive, activity, bankloc)
			String guildsInsertSQL = "INSERT INTO `" + plugin.getConfigManager().getDatabasePrefix() + "guilds` VALUES(0,?,?,?,?,'','','','',?,?,?,0,0,0,'');";
			PreparedStatement guildsInsert = getConnection().prepareStatement(guildsInsertSQL, Statement.RETURN_GENERATED_KEYS);
			preparedStatementMap.put(PreparedStatements.GUILDS_INSERT, guildsInsert);

			//Guilds select
			String guildsSelectSQL = "SELECT * FROM `" + plugin.getConfigManager().getDatabasePrefix() + "guilds`";
			PreparedStatement guildsSelect = getConnection().prepareStatement(guildsSelectSQL);
			preparedStatementMap.put(PreparedStatements.GUILDS_SELECT, guildsSelect);

			//Guilds delete
			String guildsDeleteSQL = "DELETE FROM `" + plugin.getConfigManager().getDatabasePrefix() + "guilds` WHERE `id`=?";
			PreparedStatement guildsDelete = getConnection().prepareStatement(guildsDeleteSQL);
			preparedStatementMap.put(PreparedStatements.GUILDS_DELETE, guildsDelete);

			//Guilds update
			String guildsUpdateSQL = "UPDATE `" + plugin.getConfigManager().getDatabasePrefix() + "guilds` SET `tag`=?, `name`=?, `leader`=?, `spawn`=?, `allies`=?, `alliesinv`=?, `war`=?, `nowarinv`=?, `money`=?, `points`=?, `lives`=?, `timerest`=?, `lostlive`=?, `activity`=?, `bankloc`=? WHERE `id`=?";
			PreparedStatement guildsUpdate = getConnection().prepareStatement(guildsUpdateSQL);
			preparedStatementMap.put(PreparedStatements.GUILDS_UPDATE, guildsUpdate);


			//Players insert (id, uuid, name, guild, invitedto, points, kills, deaths)
			String playersInsertSQL = "INSERT INTO `" + plugin.getConfigManager().getDatabasePrefix() + "players` VALUES(0,?,?,'','',?,0,0)";
			PreparedStatement playersInsert = getConnection().prepareStatement(playersInsertSQL, Statement.RETURN_GENERATED_KEYS);
			preparedStatementMap.put(PreparedStatements.PLAYERS_INSERT, playersInsert);
			LoggerUtils.info("PlayerInsert param count: "+playersInsert.getParameterMetaData().getParameterCount());

			//Players select
			String playerSelectSQL = "SELECT * FROM `" + plugin.getConfigManager().getDatabasePrefix() + "players`";
			PreparedStatement playersSelect = getConnection().prepareStatement(playerSelectSQL);
			preparedStatementMap.put(PreparedStatements.PLAYERS_SELECT, playersSelect);

			//Players update
			// TODO UUID is changeable, the username is not!
			// TODO Dunno how drunk I was, but it's the opposite, right?
			String playersUpdateSQL = "UPDATE `" + plugin.getConfigManager().getDatabasePrefix() + "players` SET `invitedto`=?, `guild`=?, `points`=?, `kills`=?, `deaths`=? WHERE `uuid`=?";
			PreparedStatement playersUpdate = getConnection().prepareStatement(playersUpdateSQL);
			preparedStatementMap.put(PreparedStatements.PLAYERS_UPDATE, playersUpdate);


			//Regions insert (id, loc_1, loc_2, guild, world)
			String regionsInsertSQL = "INSERT INTO `" + plugin.getConfigManager().getDatabasePrefix() + "regions` VALUES(0,?,?,?,?);";
			PreparedStatement regionsInsert = getConnection().prepareStatement(regionsInsertSQL, Statement.RETURN_GENERATED_KEYS);
			preparedStatementMap.put(PreparedStatements.REGIONS_INSERT, regionsInsert);

			//Regions select
			String regionsSelectSQL = "SELECT * FROM `" + plugin.getConfigManager().getDatabasePrefix() + "regions`";
			PreparedStatement regionsSelect = getConnection().prepareStatement(regionsSelectSQL);
			preparedStatementMap.put(PreparedStatements.REGIONS_SELECT, regionsSelect);

			//Regions delete
			String regionsDeleteSQL = "DELETE FROM `" + plugin.getConfigManager().getDatabasePrefix() + "regions` WHERE `guild`=?";
			PreparedStatement regionsDelete = getConnection().prepareStatement(regionsDeleteSQL);
			preparedStatementMap.put(PreparedStatements.REGIONS_DELETE, regionsDelete);

			//Regions update
			String regionsUpdateSQL = "UPDATE `" + plugin.getConfigManager().getDatabasePrefix() + "regions` SET `loc_1`=?, `loc_2`=?, `guild`=?, `world`=? WHERE `id`=?";
			PreparedStatement regionsUpdate = getConnection().prepareStatement(regionsUpdateSQL);
			preparedStatementMap.put(PreparedStatements.REGIONS_UPDATE, regionsUpdate);

			//Log
			LoggerUtils.info("Statements prepared in "+(System.nanoTime()-nanoTimeStart)+"ns");
		}
		catch(SQLException e) {
			LoggerUtils.exception(e);
		}
	}

	public PreparedStatement getPreparedStatement(PreparedStatements statement) throws SQLException {
		if(preparedStatementMap.isEmpty() || !preparedStatementMap.containsKey(statement)) {
			prepareStatements();
		}

		PreparedStatement preparedStatement = preparedStatementMap.get(statement);
		preparedStatement.clearParameters();

		return preparedStatement;
	}

	public void mysqlReload() {
		if(plugin.getConfigManager().getDataStorageType() != DataStorageType.MYSQL) {
			return;
		}

		long millisTime = System.currentTimeMillis();

		try {
			if(getConnection().isClosed()) {
				mySQL.closeConnection();
				connection = mySQL.openConnection();
				connected = true;
				mySQLReconnectStamp = System.currentTimeMillis();
				prepareStatements();
				LoggerUtils.info("MySQL reconnected in " + (System.currentTimeMillis() - millisTime) + "ms");
			}
		}
		catch (SQLException|ClassNotFoundException e1) {
			connected = false;
			LoggerUtils.exception(e1);
		}
	}

	public void connectToMysql() {
		try {
			if(Config.MYSQL_HOST.getString().isEmpty()) {
				LoggerUtils.error("Please edit your MySQL connection info in config.yml");
				plugin.getConfigManager().setToSecondaryDataStorageType();
				connected = false;
			}
			else {
				long nanoTime = System.nanoTime();
				mySQL = new MySQL(plugin,
						Config.MYSQL_HOST.getString(),
						Config.MYSQL_PORT.getString(),
						Config.MYSQL_DATABASE.getString(),
						Config.MYSQL_USERNAME.getString(),
						Config.MYSQL_PASSWORD.getString()
				);

				connection = mySQL.openConnection();
				connected = true;
				mySQLReconnectStamp = System.currentTimeMillis();
				LoggerUtils.info("Connected to MySQL database in "+(System.nanoTime()-nanoTime)+"ns");
				prepareStatements();
			}
		}
		catch(SQLException|ClassNotFoundException e) {
			connected = false;
			plugin.getConfigManager().setToSecondaryDataStorageType();
			LoggerUtils.exception(e);
		}
	}

	public void connectToSQLite() {
		SQLite sqlite = new SQLite(plugin, "sqlite.db");
		try {
			connection = sqlite.openConnection();
			connected = true;
			prepareStatements();

			LoggerUtils.info("Connected to SQLite database");
		}
		catch(SQLException|ClassNotFoundException e) {
			plugin.getConfigManager().setToSecondaryDataStorageType();
			connected = false;
			LoggerUtils.exception(e);
		}
	}

	public boolean checkTables() {
		try {
			DatabaseMetaData md = getConnection().getMetaData();
			ResultSet rs = md.getTables(null, null, plugin.getConfigManager().getDatabasePrefix() + "%", null);
			return rs.next();
		}
		catch(SQLException e) {
			LoggerUtils.exception(e);
		}

		return false;
	}

	public void setupTables() {
		if(!plugin.getDatabaseManager().isConnected()) {
			LoggerUtils.error("Connection is not estabilished, stopping current action");
			return;
		}

		if(plugin.getConfigManager().getDataStorageType() == DataStorageType.FLAT) {
			LoggerUtils.error("Using FLAT, cannot create sql tables.");
			return;
		}

		InputStream inputStream = plugin.getResource("sql/" + (plugin.getConfigManager().getDataStorageType()==DataStorageType.MYSQL ? "mysql" : "sqlite") + ".sql");
		String sqlString = StringUtils.inputStreamToString(inputStream);

		if(sqlString==null || sqlString.isEmpty() || !sqlString.contains("--")) {
			LoggerUtils.error("Invalid SQL");
			return;
		}

		mysqlReload();
		sqlString = StringUtils.replace(sqlString, "{SQLPREFIX}", plugin.getConfigManager().getDatabasePrefix());
		String[] actions = sqlString.split("--");

		try {
			for(String tableCode : actions) {
				Statement statement = getConnection().createStatement();
				statement.executeUpdate(tableCode);
				LoggerUtils.info("Table added to the database!");
			}
		}
		catch(SQLException e) {
			LoggerUtils.info("Could not create tables. Switching to secondary storage.");
			plugin.getConfigManager().setToSecondaryDataStorageType();
			LoggerUtils.exception(e);
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public boolean isConnected() {
		return connected;
	}
}
