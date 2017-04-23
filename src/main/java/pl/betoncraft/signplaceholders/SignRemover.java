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
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * A Listener which removes sign data from the plugin upon sign breaking.
 *
 * @author Jakub Sapalski
 */
public class SignRemover implements Listener {

	private SignPlaceholders sph = SignPlaceholders.getInstance();

	/**
	 * Starts a new Listener which will remove signs from configuration files
	 * when they are broken in the game.
	 */
	public SignRemover() {
		Bukkit.getPluginManager().registerEvents(this, sph);
	}

	@EventHandler
	public void onSignBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		if (!(block.getState() instanceof Sign)) {
			return;
		}
		Location loc = block.getLocation();
		ConfigurationSection signsSection = sph.getConfig().getConfigurationSection("signs");
		if (signsSection == null) {
			signsSection = sph.getConfig().createSection("signs");
		}
		for (String key : signsSection.getKeys(false)) {
			SignData signData = new SignData(signsSection.getConfigurationSection(key));
			if (loc.equals(signData.getLocation())) {
				signsSection.set(key, null);
				sph.saveConfig();
				sph.reload();
				event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "Sign removed!");
				return;
			}
		}
	}

}
