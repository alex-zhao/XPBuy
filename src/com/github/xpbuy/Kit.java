package com.github.xpbuy;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Kit extends XPBuy {
	public Kit() {}
	public static void giveKit(Player p, String kitName) {
		if (!isKit(kitName)) {
			p.sendMessage(prefix + ChatColor.RED + "Not a valid kit name!");
		} else { // give all items. armor equipped automatically
			PlayerInventory inv = p.getInventory();
			ArrayList<String> items = new ArrayList<String>(config.getStringList("kits." + kitName.toLowerCase()));
			int defaultItem = config.getInt("defaultitem");
            inv.setArmorContents(new ItemStack[4]);
			for (int i = 0; i < 36; i++) { // sets all slots to default item
				inv.setItem(i, new ItemStack(defaultItem, 1));
			}
			for (int i = 0; i < items.size(); i++) {
				int itemID = -1;
				if (hasDamageValue(items.get(i)) || hasEnchant(items.get(i))) {
					if (hasLevel(items.get(i)) && !hasEnchant(items.get(i))) {
						itemID = Integer.parseInt(items.get(i).substring(0, items.get(i).indexOf(";")));
						if (itemID > 297 && itemID < 318) {
							equipArmor(inv, items.get(i));
						} else {
							inv.setItem(i, parseEnchant(items.get(i).substring(0, items.get(i).indexOf("-"))));
						}
					} else if (hasDamageValue(items.get(i)) && hasEnchant(items.get(i))) {
						itemID = Integer.parseInt(items.get(i).substring(0, items.get(i).indexOf(":")));
						if (itemID > 297 && itemID < 318) {
							equipArmor(inv, items.get(i));
						} else {
							inv.setItem(i, parseBoth(items.get(i)));
						}
					} else if (hasDamageValue(items.get(i))) {
						itemID = Integer.parseInt(items.get(i).substring(0, items.get(i).indexOf(":")));
						if (itemID > 297 && itemID < 318) {
							equipArmor(inv, items.get(i));
						} else {
							inv.setItem(i, parseDamageValue(items.get(i)));
						}
					} else if (hasEnchant(items.get(i))) {
						itemID = Integer.parseInt(items.get(i).substring(0, items.get(i).indexOf(";")));
						if (itemID > 297 && itemID < 318) {
							equipArmor(inv, items.get(i));
						} else {
							inv.setItem(i, parseEnchant(items.get(i)));
						}
					}
				} else {
					itemID = Integer.parseInt(items.get(i));
					if (itemID > 297 && itemID < 318) {
						equipArmor(inv, items.get(i));
					} else {
						inv.setItem(i, new ItemStack(itemID, (new ItemStack(itemID).getMaxStackSize()))); // sets slot with max stack of item
					}
				}
			}
			p.sendMessage(prefix + ChatColor.GREEN + "Successfully purchased and applied " + kitName + " kit!");
		}
	}
	public static boolean hasDamageValue(String str) {
		if (str.contains(":")) {
			return true;
		}
		return false;
	}
	public static boolean hasEnchant(String str) {
		if (str.contains(";")) {
			return true;
		}
		return false;
	}
	public static boolean hasLevel(String str) {
		if (str.contains("-")) {
			return true;
		}
		return false;
	}
	public static ItemStack parseDamageValue(String value) {
		int itemID = Integer.parseInt(value.substring(0, value.indexOf(":")));
		int damageValue = Integer.parseInt(value.substring(value.indexOf(":") + 1, value.length()));
		ItemStack items =  new ItemStack(itemID, (new ItemStack(itemID).getMaxStackSize()));
		items.setDurability((short)damageValue);
		return items;
	}
	public static ItemStack parseEnchant(String value) {
		int itemID = Integer.parseInt(value.substring(0, value.indexOf(";")));
		int enchantValue = 0;
		if (hasLevel(value)) {
			enchantValue = Integer.parseInt(value.substring(value.indexOf(";") + 1, value.indexOf("-")));
		} else {
			enchantValue = Integer.parseInt(value.substring(value.indexOf(";") + 1, value.length()));
		}
		ItemStack items = new ItemStack(itemID, (new ItemStack(itemID).getMaxStackSize()));
		int level = -1;
		if (hasLevel(value)) {
			level = Integer.parseInt(value.substring(value.indexOf("-") + 1, value.length()));
		}
		if (level == -1) {
			items.addEnchantment(Enchantment.getById(enchantValue), 1);
		} else {
			items.addEnchantment(Enchantment.getById(enchantValue), level);
		}
		return items;
	}
	public static ItemStack parseBoth(String value) {
		int itemID = Integer.parseInt(value.substring(0, value.indexOf(":")));
		int damageValue = Integer.parseInt(value.substring(value.indexOf(":") + 1, value.indexOf(";")));
		int enchantValue = 0;
		ItemStack items =  new ItemStack(itemID, (new ItemStack(itemID).getMaxStackSize()));
		items.setDurability((short) damageValue);
		if (hasLevel(value)) {
			enchantValue = Integer.parseInt(value.substring(value.indexOf(";") + 1, value.indexOf("-")));
		} else {
			enchantValue = Integer.parseInt(value.substring(value.indexOf(";") + 1, value.length()));
		}
		int level = -1;
		if (hasLevel(value)) {
			level = Integer.parseInt(value.substring(value.indexOf("-") + 1, value.length()));
		}
		if (level == -1) {
			items.addEnchantment(Enchantment.getById(enchantValue), 1);
		} else {
			items.addEnchantment(Enchantment.getById(enchantValue), level);
		}
		return items;
	}
	public static boolean isKit(String kitName) {
		for (int i = 0; i < kits.size(); i++) {
			if (kitName.equalsIgnoreCase(kits.get(i))) {
				return true;
			}
		}
		return false;
	}
	private static void equipArmor(PlayerInventory inv, String id) {
		int itemID = -1;
		if (hasDamageValue(id)) {
			itemID = Integer.parseInt(id.substring(0, id.indexOf(":")));
			String type = armorType(itemID);
			if (type.equals("helmet")) {
				inv.setHelmet(parseDamageValue(id));
			} else if (type.equals("chestplate")) {
				inv.setChestplate(parseDamageValue(id));
			} else if (type.equals("leggings")) {
				inv.setLeggings(parseDamageValue(id));
			} else if (type.equals("boots")) {
				inv.setBoots(parseDamageValue(id));
			}
			return;
		}
		if (hasEnchant(id)) {
			itemID = Integer.parseInt(id.substring(0, id.indexOf(";")));
			String type = armorType(itemID);
			if (type.equals("helmet")) {
				inv.setHelmet(parseEnchant(id));
			} else if (type.equals("chestplate")) {
				inv.setChestplate(parseEnchant(id));
			} else if (type.equals("leggings")) {
				inv.setLeggings(parseEnchant(id));
			} else if (type.equals("boots")) {
				inv.setBoots(parseEnchant(id));
			}
			return;
		}
		itemID = Integer.parseInt(id);
		String type = armorType(itemID);
		if (type.equals("helmet")) {
			inv.setHelmet(new ItemStack(itemID, 1));
		} else if (type.equals("chestplate")) {
			inv.setChestplate(new ItemStack(itemID, 1));
		} else if (type.equals("leggings")) {
			inv.setLeggings(new ItemStack(itemID, 1));
		} else if (type.equals("boots")) {
			inv.setBoots(new ItemStack(itemID, 1));
		}
	}
	private static String armorType(int id) {
		if (id > 297 && id < 318) {
			if (id == 298 || id == 302 || id == 306 || id == 310 || id == 314) {
				return "helmet";
			} else if (id == 299 || id == 303 || id == 307 || id == 311 || id == 315) {
				return "chestplate";
			} else if (id == 300 || id == 304 || id == 308 || id == 312 || id == 316) {
				return "leggings";
			} else {
				return "boots";
			}
		}
		return "-1";
	}
	public static int getPrice(String kitName) {
		//int price = XPBuy.config.getInt("prices." + kitName.toLowerCase());
		String priceName = XPBuy.config.getString("prices." + kitName);
		if (priceName == null) {
			return 20;
		} else {
			return Integer.parseInt(priceName);
		}
	}
	public static void updateKits() {
		kits = new ArrayList<String>(config.getConfigurationSection("kits").getKeys(false));
	}
	public static boolean isDonator(String kitName) {
		return config.getBoolean("isdonator." + kitName.toLowerCase());
	}
	public static boolean hasKitPerm(Player p, String kitName) {
		ArrayList<String> players = new ArrayList<String>(config.getStringList("kitperms." + kitName.toLowerCase()));
		for (int i = 0; i < players.size(); i++) {
			if (p.getName().equalsIgnoreCase(players.get(i))) {
				return true;
			}
		}
		return false;
	}
	public static boolean giveKitPerm(Player sender, Player p, String kitName) {
		if (isKit(kitName)) {
			if (isDonator(kitName)) {
				List<OfflinePlayer> players = Arrays.asList(Bukkit.getServer().getOfflinePlayers());
				if (players.contains(p)) {
					if (sender.hasPermission("xpbuy.giveperm")) {
						ArrayList<String> permList = new ArrayList<String>(config.getStringList("kitperms." + kitName.toLowerCase()));
						permList.add(p.getName());
						config.set("kitperms." + kitName.toLowerCase(), permList);
						return true;								
					} else {
						sender.sendMessage(prefix + ChatColor.RED + "You don't have permission!");
						return false;
					}
				} else {
					sender.sendMessage(prefix + ChatColor.RED + "That isn't a valid player!");
					return false;
				}
			} else {
				sender.sendMessage(prefix + ChatColor.RED + "This isn't a donator kit!");
				return false;
			}
		} else {
			sender.sendMessage(prefix + ChatColor.RED + "Not a valid kit name!");
			return false;
		}
	}
	public static boolean delKitPerm(Player p, String kitName) {
		if (isKit(kitName)) {
			if (isDonator(kitName)) {
				if (p.hasPermission("xpbuy.delperm")) {
					ArrayList<String> permList = new ArrayList<String>(config.getStringList("kitperms." + kitName.toLowerCase()));
					permList.remove(p.getName());
					config.set("kitperms." + kitName.toLowerCase(), permList);
					return true;								
				} else {
					p.sendMessage(prefix + ChatColor.RED + "You don't have permission!");
					return false;
				}
			} else {
				p.sendMessage(prefix + ChatColor.RED + "This isn't a donator kit!");
				return false;
			}
		} else {
			p.sendMessage(prefix + ChatColor.RED + "Not a valid kit name!");
			return false;
		}
	}
	public static boolean setSignOnly(String kitName) { // returns false if value was set to false and vice versa
		if (config.getBoolean("signonly." + kitName.toLowerCase()) == true) {
			config.set("signonly." + kitName.toLowerCase(), false);
			return false;
		} else {
			config.set("signonly." + kitName.toLowerCase(), true);
			return true;
		}
	}
	public static boolean isSignOnly(String kitName) {
		return config.getBoolean("signonly." + kitName.toLowerCase());
	}
}