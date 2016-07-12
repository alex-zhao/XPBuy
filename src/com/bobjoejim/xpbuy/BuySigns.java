package com.bobjoejim.xpbuy;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;

public class BuySigns extends XPBuy implements Listener {
	Kit kit = new Kit();
	@EventHandler
	public void createClassSign(SignChangeEvent event) {
		String[] lines = event.getLines();
		if (lines[0].equalsIgnoreCase("[class]") || lines[0].equalsIgnoreCase("[xpbuy]")) {
			if (event.getPlayer().hasPermission("xpbuy.createsigns")) {
				String kitName = lines[1].toLowerCase();
				if (Kit.isKit(kitName)) {
					int price = Kit.getPrice(kitName);
					String isDonator;
					if (Kit.isDonator(kitName)) {
						isDonator = "Donator Kit";
					} else {
						isDonator = "XP Kit";
					}
					event.setLine(0, ChatColor.GREEN + "[Class]");
					event.setLine(1, kitName);
					if (price == -1) {
						event.setLine(2, "Price: Free");
					} else {
						event.setLine(2, "Price: " + price);
					}
					event.setLine(3, ChatColor.AQUA + isDonator);
				} else {
					event.setLine(1, ChatColor.RED + "Invalid Kit");
				}
			} else {
				event.getPlayer().sendMessage(prefix + ChatColor.RED + "You don't have permission!");
			}
		}
	}
	@EventHandler
	public void useClassSign(PlayerInteractEvent event) {
		Action action = event.getAction();
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();
		if (action == Action.RIGHT_CLICK_BLOCK && block.getType().equals(Material.SIGN_POST)
				|| action == Action.RIGHT_CLICK_BLOCK && block.getType().equals(Material.WALL_SIGN)) {
			Sign sign = (Sign) block.getState();
			if (sign.getLine(0).equals(ChatColor.GREEN + "[Class]")) {
				if (player.hasPermission("xpbuy.usesigns")) {
					String kitName = sign.getLine(1).toLowerCase();
					if (Kit.isKit(kitName)) {
						if (isUpdated(sign)) {
							if (Kit.isDonator(kitName)) {
								pay(player, kitName, true);
							} else {
								pay(player, kitName, false);
							}
						} else {
							player.sendMessage(prefix + ChatColor.RED + "This kit has been updated. Click again to purchase.");
							updateSign(sign);
							event.setCancelled(true);
						}
					} else {
						player.sendMessage(prefix + ChatColor.RED + "Sorry, this kit no longer exists.");
						updateSign(sign);
					}
				} else {
					player.sendMessage(prefix + ChatColor.RED + "You don't have permission!");
					event.setCancelled(true);
				}
			}
		}
	}
	public boolean isUpdated(Sign sign) {
		String kitName = sign.getLine(1);
		String priceName = sign.getLine(2).substring(7, sign.getLine(2).length());
		int price;
		if (priceName.equals("Free")) {
			price = -1;
		} else {
			price = Integer.parseInt(priceName);
		}
		boolean isDonator;
		if (sign.getLine(3).equals(ChatColor.AQUA + "Donator Kit")) {
			isDonator = true;
		} else {
			isDonator = false;
		}
		if (price == Kit.getPrice(kitName) && isDonator == Kit.isDonator(kitName)) {
			return true;
		} else {
			return false;
		}
	}
	public void updateSign(Sign sign) {
		String kitName = sign.getLine(1);
		String isDonator = "";
		if (Kit.isKit(kitName)) {
			if (Kit.getPrice(kitName) == -1) {
				sign.setLine(2, "Price: Free");
			} else {
				sign.setLine(2, "Price: " + Kit.getPrice(kitName));
			}
			if (Kit.isDonator(kitName)) {
				isDonator = "Donator Kit";
			} else {
				isDonator = "XP Kit";
			}
			sign.setLine(3, ChatColor.AQUA + isDonator);
			sign.update();
		} else {
			sign.setLine(1, ChatColor.RED + "Deleted");
			sign.setLine(2, null);
			sign.setLine(3, null);
			sign.update();
		}
	}
}