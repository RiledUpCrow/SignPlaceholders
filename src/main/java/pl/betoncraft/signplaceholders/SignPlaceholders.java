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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class of the SignPlaceholders plugin.
 *
 * @author Jakub Sapalski
 */
public class SignPlaceholders extends JavaPlugin {

	private static SignPlaceholders instance;
	private List<SignData> signs = new ArrayList<>();
	private int interval = 20;
	private double range = 32;
	private SignUpdater updater;

	@Override
	public void onEnable() {
		instance = this;
		saveDefaultConfig();
		new SignCommand();
		new SignRemover();
		reload();
		getLogger().info("SignPlaceholders enabled!");
	}

	/**
	 * Returns the currently enabled instance of SignPlaceholders plugin.
	 *
	 * @return the current instance
	 */
	public static SignPlaceholders getInstance() {
		return instance;
	}

	/**
	 * Returns an unmodifiable list of loaded signs.
	 *
	 * @return the List of loaded signs
	 */
	public List<SignData> getSigns() {
		return Collections.unmodifiableList(signs);
	}
	
	/**
	 * Returns the interval at which all signs are updated.
	 *
	 * @return the interval in ticks
	 */
	public int getInterval() {
		return interval;
	}
	
	/**
	 * Returns the range around players where the signs will be updated.
	 *
	 * @return the range in blocks
	 */
	public double getRange() {
		return range;
	}

	/**
	 * Reloads all plugin data.
	 */
	public void reload() {
		// stop previously running updater
		if (updater != null) {
			updater.cancel();
		}
		// reload configuration values
		reloadConfig();
		interval = getConfig().getInt("interval", interval);
		range = getConfig().getDouble("range", range);
		// load all new signs
		signs.clear();
		ConfigurationSection sections = getConfig().getConfigurationSection("signs");
		if (sections != null) for (String key : sections.getKeys(false)) {
			signs.add(new SignData(sections.getConfigurationSection(key)));
		}
		// schedule new updater
		updater = new SignUpdater(signs, interval, range);
		updater.runTaskTimer(this, 0, 1);
		// done
		getLogger().info(String.format("Loaded %d signs!", signs.size()));
	}

}
