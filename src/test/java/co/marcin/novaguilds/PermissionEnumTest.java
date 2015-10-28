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

import co.marcin.novaguilds.enums.Commands;
import co.marcin.novaguilds.enums.Permission;
import co.marcin.novaguilds.util.StringUtils;
import org.junit.Test;

public class PermissionEnumTest {
	@Test
	public void testPermissionEnum() throws Exception {
		boolean passed = true;
		for(Commands commands : Commands.values()) {
			Permission permission = Permission.fromPath(commands.getPermission());

			if(permission == null) {
				if(passed) {
					System.out.println("Missing enums:");
				}

				System.out.println(StringUtils.replace(commands.getPermission().toUpperCase(), ".", "_"));
				passed = false;
			}
		}

		if(!passed) {
			throw new Exception("Found missing Permission enums!");
		}
	}
}
