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

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Updates the signs around players.
 *
 * @author Jakub Sapalski
 */
public class SignUpdater extends BukkitRunnable {

	private List<SignData> signs;
	private double rangeSquared;
	private final int size;
	private int counter = 0;
	private double buffer = 0;
	private double step;

	/**
	 * Creates a new sign updating Runnable. It will update signs close to the
	 * players at specified interval. It needs to be started manually.
	 *
	 * @param signs
	 *            list of signs to update
	 * @param interval
	 *            amount of ticks between each update
	 * @param range
	 *            distance from the player where the update will be applied
	 */
	public SignUpdater(List<SignData> signs, int interval, double range) {
		this.signs = Collections.unmodifiableList(signs);
		rangeSquared = range * range;
		size = signs.size();
		step = (double) size / (double) interval;
	}

	@Override
	public void run() {
		if (size > 0) {
			// buffer stores current step - amount of sings to process in this tick
			buffer += step;
			// we can only step by whole numbers
			int move = (int) Math.floor(buffer);
			// what's left will be used on the next tick
			buffer -= move;
			// process calculated amount of signs
			for (int i = 0; i < move; i++) {
				// counter tells us where is the currently processed sign
				counter++;
				// counter can be larger than amount of signs, it will be trimmed
				SignData sign = signs.get(counter % size);
				// apply the sign for all nearby players
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (player.getWorld().equals(sign.getLocation().getWorld()) && 
							player.getLocation().distanceSquared(sign.getLocation()) <= rangeSquared) {
						sign.apply(player);
					}
				}
			}
		}
	}

}
