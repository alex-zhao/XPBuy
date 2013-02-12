package com.github.xpbuy;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Kit {
	public Kit() {}
	public void giveKit(Player p, String kit) {
		if (!isKit(kit)) {
			p.sendMessage(XPBuy.prefix + ChatColor.RED + "Not a valid kit name!");
		} else { // give all items. armor equipped automatically
			PlayerInventory inv = p.getInventory();
			ArrayList<Integer> items = new ArrayList<Integer>(XPBuy.config.getIntegerList("kits." + kit.toLowerCase()));
			int defaultItem = XPBuy.config.getInt("defaultitem");
			for (int i = 0; i < 36; i++) { // sets all slots to default item
				inv.setItem(i, new ItemStack(defaultItem, 1));
			}
			for (int i = 0; i < items.size(); i++) {
				if (items.get(i) > 297 && items.get(i) < 318) {
					equipArmor(inv, items.get(i));
				} else {
					inv.setItem(i, new ItemStack(items.get(i), (new ItemStack(items.get(i)).getMaxStackSize()))); // sets slot with max stack of item
				}
			}
			p.sendMessage(XPBuy.prefix + ChatColor.GREEN + "Successfully purchased and applied " + kit + " kit!");
		}
	}
	public boolean isKit(String kit) {
		for (int i = 0; i < XPBuy.kits.size(); i++) {
			if (kit.equalsIgnoreCase(XPBuy.kits.get(i))) {
				return true;
			}
		}
		return false;
	}
	private void equipArmor(PlayerInventory inv, int id) {
		String type = armorType(id);
		if (type.equals("helmet")) {
			inv.setHelmet(new ItemStack(id, 1));
		} else if (type.equals("chestplate")) {
			inv.setChestplate(new ItemStack(id, 1));
		} else if (type.equals("leggings")) {
			inv.setLeggings(new ItemStack(id, 1));
		} else if (type.equals("boots")) {
			inv.setBoots(new ItemStack(id, 1));
		}
	}
	private String armorType(int id) {
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
	public int getPrice(String kit) {
		int price = XPBuy.config.getInt("prices." + kit.toLowerCase());
		if (price == 0) {
			return 20;
		} else {
			return price;
		}
	}
	public void updateKits() {
		XPBuy.kits = new ArrayList<String>(XPBuy.config.getConfigurationSection("kits").getKeys(false));
	}
	public boolean isDonator(String kit) {
		return XPBuy.config.getBoolean("isdonator." + kit.toLowerCase());
	}
	public boolean hasKitPermission(Player p, String kit) {
		ArrayList<String> players = new ArrayList<String>(XPBuy.config.getStringList("kitpermission." + kit.toLowerCase()));
		for (int i = 0; i < players.size(); i++) {
			if (p.toString().equalsIgnoreCase(players.get(i))) {
				return true;
			}
		}
		return false;
	}
}