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

			//Guilds insert
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
			String guildsUpdateSQL = "UPDATE `" + plugin.getConfigManager().getDatabasePrefix() + "guilds` SET `tag`='?', `name`='?', `leader`='?', `spawn`='?', `allies`='?', `alliesinv`='?', `war`='?', `nowarinv`='?', `money`='?', `points`=?, `lives`=?, `timerest`=?, `lostlive`=?, `activity`=?, `bankloc`='?' WHERE `id`=?";
			PreparedStatement guildsUpdate = getConnection().prepareStatement(guildsUpdateSQL);
			preparedStatementMap.put(PreparedStatements.GUILDS_UPDATE, guildsUpdate);


			//Players insert
			String playersInsertSQL = "INSERT INTO `" + plugin.getConfigManager().getDatabasePrefix() + "players` VALUES(0,'?','?','','')";
			PreparedStatement playersInsert = getConnection().prepareStatement(playersInsertSQL, Statement.RETURN_GENERATED_KEYS);
			preparedStatementMap.put(PreparedStatements.PLAYERS_INSERT, playersInsert);

			//Players select
			String playerSelectSQL = "SELECT * FROM `" + plugin.getConfigManager().getDatabasePrefix() + "players`";
			PreparedStatement playersSelect = getConnection().prepareStatement(playerSelectSQL);
			preparedStatementMap.put(PreparedStatements.PLAYERS_SELECT, playersSelect);

			//Players update
			// TODO UUID is changeable, the username is not!
			String playersUpdateSQL = "UPDATE `" + plugin.getConfigManager().getDatabasePrefix() + "players` SET `invitedto`='?', `guild`='?' WHERE `uuid`='?'";
			PreparedStatement playersUpdate = getConnection().prepareStatement(playersUpdateSQL);
			preparedStatementMap.put(PreparedStatements.PLAYERS_UPDATE, playersUpdate);


			//Regions insert
			String regionsInsertSQL = "INSERT INTO `" + plugin.getConfigManager().getDatabasePrefix() + "regions` VALUES(0,'?','?','?','?');";
			PreparedStatement regionsInsert = getConnection().prepareStatement(regionsInsertSQL, Statement.RETURN_GENERATED_KEYS);
			preparedStatementMap.put(PreparedStatements.REGIONS_INSERT, regionsInsert);

			//Regions select
			String regionsSelectSQL = "SELECT * FROM `" + plugin.getConfigManager().getDatabasePrefix() + "regions`";
			PreparedStatement regionsSelect = getConnection().prepareStatement(regionsSelectSQL);
			preparedStatementMap.put(PreparedStatements.REGIONS_SELECT, regionsSelect);

			//Regions delete
			String regionsDeleteSQL = "DELETE FROM `" + plugin.getConfigManager().getDatabasePrefix() + "regions` WHERE `guild`='?'";
			PreparedStatement regionsDelete = getConnection().prepareStatement(regionsDeleteSQL);
			preparedStatementMap.put(PreparedStatements.REGIONS_DELETE, regionsDelete);

			//Regions update
			String regionsUpdateSQL = "UPDATE `" + plugin.getConfigManager().getDatabasePrefix() + "regions` SET `loc_1`='?', `loc_2`='?', `guild`='?', `world`='?' WHERE `id`=?";
			PreparedStatement regionsUpdate = getConnection().prepareStatement(regionsUpdateSQL);
			preparedStatementMap.put(PreparedStatements.REGIONS_UPDATE, regionsUpdate);

			//Log
			LoggerUtils.info("Statements prepared in "+(System.nanoTime()-nanoTimeStart)+"ns");
		}
		catch(SQLException e) {
			LoggerUtils.exception(e);
		}
	}

	public PreparedStatement getPreparedStatement(PreparedStatements statement) {
		if(preparedStatementMap.isEmpty() || !preparedStatementMap.containsKey(statement)) {
			prepareStatements();
		}

		return preparedStatementMap.get(statement);
	}

	public void mysqlReload() {
		if(plugin.getConfigManager().getDataStorageType() != DataStorageType.MYSQL) {
			return;
		}

		long nanoTime = System.nanoTime();

		if( System.currentTimeMillis() - mySQLReconnectStamp > 3000) {
			try {
				mySQL.closeConnection();
				try {
					connection = mySQL.openConnection();
					connected = true;
					mySQLReconnectStamp = System.currentTimeMillis();
					LoggerUtils.info("MySQL reconnected in "+(System.nanoTime()-nanoTime)+"ns");
				}
				catch (ClassNotFoundException e) {
					connected = false;
					LoggerUtils.exception(e);
				}
			}
			catch (SQLException e1) {
				connected = false;
				LoggerUtils.exception(e1);
			}
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
				mySQL = new MySQL(plugin,
						Config.MYSQL_HOST.getString(),
						Config.MYSQL_PORT.getString(),
						Config.MYSQL_DATABASE.getString(),
						Config.MYSQL_USERNAME.getString(),
						Config.MYSQL_PASSWORD.getString()
				);

				connection = mySQL.openConnection();
				connected = true;
				prepareStatements();
				LoggerUtils.info("Connected to MySQL database");
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

		sqlString = StringUtils.replace(sqlString, "{SQLPREFIX}", plugin.getConfigManager().getDatabasePrefix());
		String[] actions = sqlString.split("--");

		try {
			for(String tableCode : actions) {
				createTable(tableCode);
				LoggerUtils.info("Table added to the database!");
			}
		}
		catch(SQLException e) {
			LoggerUtils.info("Could not create tables. Switching to secondary storage.");
			plugin.getConfigManager().setToSecondaryDataStorageType();
			LoggerUtils.exception(e);
		}
	}

	@Deprecated
	public void setupTablesOld() {
		if(!plugin.getDatabaseManager().isConnected()) {
			LoggerUtils.info("Connection is not estabilished, stopping current action");
			return;
		}

		try {
			if(plugin.getConfigManager().getDataStorageType() != DataStorageType.FLAT) {
				DatabaseMetaData md = getConnection().getMetaData();
				ResultSet rs = md.getTables(null, null, plugin.getConfigManager().getDatabasePrefix() + "%", null);
				if(!rs.next()) {
					LoggerUtils.info("Couldn't find tables in the base. Creating...");
					String[] SQLCreateCode = getSQLCreateCode();
					if(SQLCreateCode.length != 0) {
						try {
							for(String tableCode : SQLCreateCode) {
								createTable(tableCode);
								LoggerUtils.info("Tables added to the database!");
							}
						}
						catch(SQLException e) {
							LoggerUtils.info("Could not create tables. Switching to secondary storage.");
							plugin.getConfigManager().setToSecondaryDataStorageType();
							LoggerUtils.exception(e);
						}
					}
					else {
						LoggerUtils.info("Couldn't find SQL create code for tables!");
						plugin.getConfigManager().setDataStorageType(DataStorageType.FLAT);
					}
				}
				else {
					LoggerUtils.info("No database config required.");
				}
			}
		}
		catch(SQLException e) {
			LoggerUtils.exception(e);
		}
	}

	//true=mysql, false=sqlite
	private String[] getSQLCreateCode() {
		int index = plugin.getConfigManager().getDataStorageType()==DataStorageType.MYSQL ? 0 : 1;

		String url = "http://novaguilds.marcin.co/sqltables.txt";
		String sql = StringUtils.getContent(url);

		String[] types = sql.split("--TYPE--");
		return types[index].split("--");
	}

	private void createTable(String sql) throws SQLException {
		mysqlReload();
		Statement statement;
		sql = StringUtils.replace(sql, "{SQLPREFIX}", plugin.getConfigManager().getDatabasePrefix());
		statement = getConnection().createStatement();
		statement.executeUpdate(sql);
	}

	public Connection getConnection() {
		return connection;
	}

	public boolean isConnected() {
		return connected;
	}
}
