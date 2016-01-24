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

package co.marcin.novaguilds;

import co.marcin.novaguilds.manager.MessageManager;
import co.marcin.novaguilds.util.StringUtils;
import co.marcin.novaguilds.yaml.YamlParseTest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;

import java.io.File;

public class TimeStringConversionTest {
	private final String[] strings = {
			"1s",
			"1m",
			"1h",
			"1d",
			"1w",
			"1mo",
			"1y",
			"1y 1mo 1w 1d 1h 1m 1s"
	};

	private final Long[] longs = {
			1L,
			60L,
			3600L,
			86400L,
			604800L,
			2678400L,
			31536000L,
			34909261L,
	};

	private final String[] strings2 = {
			"1 second",
			"1 minute",
			"1 hour",
			"1 day",
			"1 week",
			"1 month",
			"1 year",
			"1 year 1 month 1 week 1 day 1 hour 1 minute 1 second",
	};

	@Test
	public void testStringToSeconds() throws Exception {
		System.out.println("Testing stringToSeconds()");
		for(int i=0; i<strings.length; i++) {
			System.out.println(strings[i] + "->" + StringUtils.stringToSeconds(strings[i]) + " = " + longs[i]);
			if(StringUtils.stringToSeconds(strings[i]) != longs[i]) {
				throw new Exception("Values not equal!");
			}
		}

		System.out.println();
	}

	@Test
	public void testSecondsToString() throws Exception {
		System.out.println("Testing secondsToString()");

		MessageManager messageManager = new MessageManager();
		File langsDir = new File(YamlParseTest.resourcesDirectory, "/lang");
		File motherLangFile = new File(langsDir, "en-en.yml");
		YamlConfiguration motherConfiguration = YamlConfiguration.loadConfiguration(motherLangFile);
		messageManager.setMessages(motherConfiguration);

		for(int i=0; i<strings2.length; i++) {
			System.out.println(longs[i] + "->" + StringUtils.secondsToString(longs[i]) + " = " + strings2[i]);
			if(!StringUtils.secondsToString(longs[i]).equals(strings2[i])) {
				throw new Exception("Values not equal!");
			}
		}

		System.out.println();
	}
}
