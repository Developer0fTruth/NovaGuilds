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

package co.marcin.novaguilds.yaml;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;

import java.io.File;
import java.util.*;

public class YamlIncompleteTranslationTest {
	@Test
	public void testTranslations() throws Exception {
		File langsDir = new File(YamlParseTest.resourcesDirectory, "/lang");

		//Mother lang setup
		File motherLangFile = new File(langsDir, "en-en.yml");
		YamlConfiguration motherConfiguration = YamlConfiguration.loadConfiguration(motherLangFile);
		List<String> motherKeys = new ArrayList<>();
		for(String key : motherConfiguration.getKeys(true)) {
			if(!motherConfiguration.isConfigurationSection(key)) {
				motherKeys.add(key);
			}
		}

		//List all languages and configuration sections
		Map<String, YamlConfiguration> configurationMap = new HashMap<>();
		if(langsDir.isDirectory()) {
			File[] list = langsDir.listFiles();

			if(list != null) {
				for(File langFile : list) {
					if(!langFile.getName().equals("en-en.yml")) {
						configurationMap.put(langFile.getName().replace(".yml", ""), YamlConfiguration.loadConfiguration(langFile));
					}
				}
			}
		}

		//Get keys from all langs
		System.out.println("Testing lang files for missing keys...");
		for(Map.Entry<String, YamlConfiguration> entry : configurationMap.entrySet()) {
			int missingCount = 0;
			String name = entry.getKey();
			YamlConfiguration configuration = entry.getValue();

			System.out.println("---");
			System.out.println();
			System.out.println("Testing lang: "+name);

			for(String mKey : motherKeys) {
				if(!configuration.contains(mKey)) {
					if(missingCount==0) {
						System.out.println("Missing keys:");
					}

					System.out.println(" - "+mKey);
					missingCount++;
				}
			}

			if(missingCount == 0) {
				System.out.println("Result: No missing keys");
			}
			else {
				throw new Exception("Found "+missingCount+" missing keys in lang "+name);
			}
		}
	}
}
