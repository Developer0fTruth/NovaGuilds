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

package co.marcin.novaguilds.enums;

import co.marcin.novaguilds.util.LoggerUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public enum Lang {
	EN_EN,
	PL_PL,
	DE_DE,
	ZH_CN(Charset.forName("GBK")),
	CUSTOM;

	private Charset charset;

	Lang() {
		charset = Charset.forName("UTF-8");
	}

	Lang(Charset charset) {
		this.charset = charset;
	}

	public Charset getCharset() {
		return charset;
	}

	private void setCharset(Charset charset) {
		this.charset = charset;
	}

	public static Lang fromFile(File file) throws IOException {
		try {
			String langName = StringUtils.replace(StringUtils.replace(file.getName().toUpperCase(), "-", "_"), ".YML", "");
			return Lang.valueOf(langName);
		}
		catch(Exception e) {
			try (BufferedReader brTest = new BufferedReader(new FileReader(file))) {
				String line = brTest.readLine();

				if (line.startsWith("#")) {
					line = line.substring(1);
					LoggerUtils.info("Detected custom enconding for file " + file.getName() + ": " + line);
					Lang lang = Lang.CUSTOM;
					lang.setCharset(Charset.forName(line));
					return lang;
				}

				LoggerUtils.info("Found custom translation, applying default translation UTF-8");
				LoggerUtils.info("Add #ENCODING to the first line of your language file");
				LoggerUtils.info("Please consider sharing your translation with the community");
			}
			return Lang.CUSTOM;
		}
	}

	public static YamlConfiguration loadConfiguration(File file) throws IOException {
		return YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(file), Lang.fromFile(file).getCharset()));
	}
}
