/**
 * SignPlaceholders - PlaceholderAPI variables on signs
 * Copyright (C) 2017  Jakub "Co0sh" Sapalski
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.betoncraft.signplaceholders;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;

/**
 * Stores information about sign's location and lines to modify.
 *
 * @author Jakub Sapalski
 */
public class SignData {

	private Location location;
	private String[] lines;

	/**
	 * Loads the sign from supplied ConfigurationSection.
	 *
	 * @param section
	 *            ConfigurationSection with sign data
	 */
	public SignData(ConfigurationSection section) {
		ConfigurationSection locSec = section.getConfigurationSection("loc");
		location = new Location(Bukkit.getWorld(locSec.getString("world")),
				Integer.valueOf(locSec.getInt("x")),
				Integer.valueOf(locSec.getInt("y")),
				Integer.valueOf(locSec.getInt("z")));
		lines = section.getStringList("lines").toArray(new String[4]);
	}

	/**
	 * Returns the location at which the sign is placed.
	 *
	 * @return the Location
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Applies the placeholder modifications to this sign.
	 *
	 * @param player
	 *            the player for whom the placeholders should be resolved and
	 *            updated
	 */
	public void apply(Player player) {
		BlockState state = location.getBlock().getState();
		if (state instanceof Sign) {
			Sign sign = (Sign) state;
			String[] original = sign.getLines();
			String[] updated = new String[4];
			for (int i = 0; i < 4; i++) {
				updated[i] = lines[i].isEmpty() ? original[i] : PlaceholderAPI.setPlaceholders(player, lines[i]);
			}
			player.sendSignChange(location, updated);
		}
	}

}
