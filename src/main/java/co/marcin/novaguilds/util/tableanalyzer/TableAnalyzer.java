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

package co.marcin.novaguilds.util.tableanalyzer;

import co.marcin.novaguilds.util.LoggerUtils;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableAnalyzer {
	private final Connection connection;
	private final HashMap<String, String> sqlStructure = new HashMap<>();
	private final HashMap<Integer, String> sqlNames = new HashMap<>();
	private final HashMap<Integer, String> tableNames = new HashMap<>();
	private final HashMap<String, String> tableStructure = new HashMap<>();
	private final List<Missmatch> missmatches = new ArrayList<>();

	public TableAnalyzer(Connection connection) {
		this.connection = connection;
	}

	public void analyze(String table, String sql) {
		if(!existsTable(table)) {
			addTable(sql);
		}

		missmatches.clear();
		getSqlStructure(sql);
		getTableStructure(table);

		List<String> sqlKeys = new ArrayList<>();
		List<String> tableKeys = new ArrayList<>();

		for(Map.Entry<Integer, String> sqlEntry : sqlNames.entrySet()) {
			sqlKeys.add(sqlEntry.getValue());
		}

		for(Map.Entry<Integer, String> tableEntry : tableNames.entrySet()) {
			tableKeys.add(tableEntry.getValue());
		}

		//search for ADD
		if(tableStructure.size() < sqlKeys.size()) {
			int newindex = tableStructure.size();

			for(int i=newindex; i<sqlStructure.size(); i++) {
				Missmatch missmatch = new Missmatch(ModificationType.ADD);
				missmatch.setTable(table);
				String name = sqlKeys.get(i);

				if(!tableStructure.keySet().contains(name)) {
					missmatch.setColumnName(name);
					missmatch.setColumnType(sqlStructure.get(name));
					missmatch.setIndex(i);

					missmatches.add(missmatch);
				}
			}
		}
	}

	public void update() {
		sort();
		for(Missmatch missmatch : missmatches) {
			LoggerUtils.debug(missmatch.getModificationType().name()+": "+missmatch.getIndex()+" "+missmatch.getColumnName()+" "+missmatch.getColumnType());
			switch(missmatch.getModificationType()) {
				case ADD:
					addColumn(missmatch);
					break;
			}
		}
	}

	private void addColumn(Missmatch missmatch) {
		try {
			String sql = "ALTER TABLE `"+missmatch.getTable()+"` ADD COLUMN `"+missmatch.getColumnName()+"` "+missmatch.getColumnType()+" NOT NULL;";
			Statement statement = connection.createStatement();
			statement.execute(sql);
			LoggerUtils.info("Added new column "+missmatch.getColumnName()+" to table "+missmatch.getTable());
		}
		catch(SQLException e) {
			LoggerUtils.exception(e);
		}
	}

	private void addTable(String sql) {
		try {
			Statement statement = connection.createStatement();
			statement.execute(sql);
			LoggerUtils.info("Added new table");
		}
		catch(SQLException e) {
			LoggerUtils.exception(e);
		}
	}

	private void sort() {
		Collections.sort(missmatches, new Comparator<Missmatch>() {
			public int compare(Missmatch o1, Missmatch o2) {
				return o1.getIndex() - o2.getIndex();
			}
		});

	}

	public List<Missmatch> getMissmatches() {
		return missmatches;
	}

	private void getSqlStructure(String sql) {
		String[] cols = org.apache.commons.lang.StringUtils.split(sql, ",\r\n");
		HashMap<String, String> map = new HashMap<>();

		int i=0;
		for(String c : cols) {
			if(c.startsWith("  `")) {
				String[] split = StringUtils.split(c, ' ');
				String name = org.apache.commons.lang.StringUtils.replace(split[0], "`", "");
				map.put(name, split[1]);

				sqlNames.put(i, name);
				i++;
			}
		}

		sqlStructure.putAll(map);
	}

	private void getTableStructure(String table) {
		try {
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			ResultSet columns = databaseMetaData.getColumns(null, null, table, null);
			HashMap<String, String> map = new HashMap<>();

			int i = 0;
			while (columns.next()) {
				String columnName = columns.getString("COLUMN_NAME");
				String columnType = columns.getString("TYPE_NAME");

				map.put(columnName, columnType);

				tableNames.put(i, columnName);
				i++;
			}
			columns.close();

			tableStructure.putAll(map);
		}
		catch(SQLException e) {
			LoggerUtils.exception(e);
		}
	}

	private boolean existsTable(String table) {
		try {
			DatabaseMetaData md = connection.getMetaData();
			ResultSet rs = md.getTables(null, null, "%", null);
			while(rs.next()) {
				if(rs.getString(3).equalsIgnoreCase(table)) {
					return true;
				}
			}
		}
		catch(SQLException e) {
			LoggerUtils.exception(e);
			return false;
		}

		return false;
	}
}
