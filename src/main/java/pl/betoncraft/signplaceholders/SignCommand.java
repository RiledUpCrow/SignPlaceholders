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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * The main command used to control the plugin: reloading, adding and removing
 * signs.
 *
 * @author Jakub Sapalski
 */
public class SignCommand implements CommandExecutor {
	
	private final Set<Material> transparent;
	
	public SignCommand() {
		Set<Material> transparent = Arrays.asList(Material.values()).stream()
				.filter(m -> !m.isSolid())
				.collect(Collectors.toSet());
		transparent.remove(Material.SIGN_POST);
		transparent.remove(Material.WALL_SIGN);
		this.transparent = Collections.unmodifiableSet(transparent);
		Bukkit.getPluginCommand("signplaceholders").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		SignPlaceholders sph = SignPlaceholders.getInstance();
		if (cmd.getName().equals("signplaceholders")) {
			// help message
			if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
				sender.sendMessage(ChatColor.GREEN + sph.getDescription().getFullName());
				sender.sendMessage(ChatColor.AQUA + "/" + label + 
						" reload " + ChatColor.YELLOW +
						"Reloads the plugin");
				sender.sendMessage(ChatColor.AQUA + "/" + label + 
						" add <1st line> | <2nd line> | <3rd line> | <4th line> " +
						ChatColor.YELLOW + "Registers the signs you're looking at with specified placeholders.");
				sender.sendMessage(ChatColor.AQUA + "/" + label + 
						" del " + ChatColor.YELLOW +
						"Deletes the sign you're looking at.");
				sender.sendMessage(ChatColor.AQUA + "/" + label +
						" interval <number> " + ChatColor.YELLOW +
						"Sets the update interval of the signs (in ticks).");
				sender.sendMessage(ChatColor.AQUA + "/" + label +
						" range <number> " + ChatColor.YELLOW +
						"Sets the range around players where the signs will be updated (in blocks).");
				return true;
			}
			// reloading the plugin
			if (args[0].equalsIgnoreCase("reload")) {
				sph.reload();
				sender.sendMessage(ChatColor.DARK_GREEN + sph.getDescription().getFullName() + " reloaded!");
				return true;
			}
			// adding the sign
			if (args[0].equalsIgnoreCase("add")) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.DARK_RED + "Must be a player to use this command!");
					return true;
				}
				Player player = (Player) sender;
				String[] lines = String.join(" ", args).substring(label.length()).split("\\|");
				List<String> linesList = new ArrayList<>(4);
				for (int i = 0; i < 4; i++) {
					linesList.add(lines.length > i ? lines[i].trim() : "");
				}
				Block block = player.getTargetBlock(transparent, 32);
				if (!(block.getState() instanceof Sign)) {
					sender.sendMessage(ChatColor.DARK_RED + "You're not looking at any sign!");
					return true;
				}
				Location loc = block.getLocation();
				ConfigurationSection signsSection = sph.getConfig().getConfigurationSection("signs");
				if (signsSection == null) {
					signsSection = sph.getConfig().createSection("signs");
				}
				Set<String> keys = signsSection.getKeys(false);
				ConfigurationSection newSign = null;
				for (String key : signsSection.getKeys(false)) {
					ConfigurationSection signSection = signsSection.getConfigurationSection(key);
					SignData signData = new SignData(signSection);
					if (loc.equals(signData.getLocation())) {
						newSign = signSection;
						break;
					}
				}
				if (newSign == null) {
					int i = 0;
					while (keys.contains(String.valueOf(i))) {
						i++;
					}
					newSign = signsSection.createSection(String.valueOf(i));
				}
				String worldName = loc.getWorld().getName();
				int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
				newSign.set("loc.world", worldName);
				newSign.set("loc.x", x);
				newSign.set("loc.y", y);
				newSign.set("loc.z", z);
				newSign.set("lines", linesList);
				sph.saveConfig();
				sph.reload();
				sender.sendMessage(ChatColor.DARK_GREEN + "Sign added!");
				return true;
			}
			// deleting the sign
			if (args[0].equalsIgnoreCase("del")) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.DARK_RED + "Must be a player to use this command!");
					return true;
				}
				Player player = (Player) sender;
				Block block = player.getTargetBlock(transparent, 32);
				if (!(block.getState() instanceof Sign)) {
					sender.sendMessage(ChatColor.DARK_RED + "You're not looking at any sign!");
					return true;
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
						for (Player p : Bukkit.getOnlinePlayers()) {
							if (p.getWorld().equals(signData.getLocation().getWorld()) &&
									p.getLocation().distanceSquared(signData.getLocation()) <=
									Math.pow(sph.getRange(), 2)) {
								p.sendSignChange(signData.getLocation(), ((Sign) block.getState()).getLines());
							}
						}
						sender.sendMessage(ChatColor.DARK_GREEN + "Sign removed!");
						return true;
					}
				}
				sender.sendMessage(ChatColor.DARK_RED + "This sign is not registered yet!");
				return true;
			}
			// change interval
			if (args[0].equalsIgnoreCase("interval")) {
				if (args.length < 2) {
					sender.sendMessage(ChatColor.DARK_RED + "Specify update interval!");
					return true;
				}
				try {
					int interval = Integer.parseInt(args[1]);
					if (interval <= 0) {
						throw new NumberFormatException();
					}
					sph.getConfig().set("interval", interval);
					sph.saveConfig();
					sph.reload();
					sender.sendMessage(ChatColor.DARK_GREEN + "Interval updated!");
					return true;
				} catch (NumberFormatException e) {
					sender.sendMessage(ChatColor.DARK_RED + "Update interval must be a positive integer!");
					return true;
				}
			}
			// change range
			if (args[0].equalsIgnoreCase("range")) {
				if (args.length < 2) {
					sender.sendMessage(ChatColor.DARK_RED + "Specify range!");
					return true;
				}
				try {
					int range = Integer.parseInt(args[1]);
					if (range <= 0) {
						throw new NumberFormatException();
					}
					sph.getConfig().set("range", range);
					sph.saveConfig();
					sph.reload();
					sender.sendMessage(ChatColor.DARK_GREEN + "Range updated!");
					return true;
				} catch (NumberFormatException e) {
					sender.sendMessage(ChatColor.DARK_RED + "Range must be a positive integer!");
					return true;
				}
			}
			// unknown argument
			sender.sendMessage(ChatColor.RED + "Unknown argument. Use /" + label + " help");
			return true;
		}
		return false;
	}

}
