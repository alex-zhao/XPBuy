package com.github.xpbuy;

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
	public void onSignChange(SignChangeEvent event) { // making a sign
		String[] lines = event.getLines();
		if (lines[0].equalsIgnoreCase("[class]") || lines[0].equalsIgnoreCase("[xpbuy]")) {
			String kitName = lines[1].toLowerCase(); // it's null apparently
			if (kit.isKit(kitName)) {
				int price = kit.getPrice(kitName);
				String isDonator = "";
				if (kit.isDonator(kitName)) {
					isDonator = "Donator Kit";
				} else {
					isDonator = "Free: Use XP";
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
				event.setLine(1, ChatColor.RED + "Invalid kit");
			}
		}
	}
	@EventHandler
	public void onInteractEvent(PlayerInteractEvent event) { // using a sign
		Action action = event.getAction();
		Player p = event.getPlayer();
		Block block = event.getClickedBlock();
		if (action == Action.LEFT_CLICK_BLOCK && block.getType().equals(Material.SIGN_POST) 
				|| action == Action.LEFT_CLICK_BLOCK && block.getType().equals(Material.WALL_SIGN)) {
			Sign sign = (Sign) block.getState();
			if (sign.getLine(0).equals(ChatColor.GREEN + "[Class]")) {
				String kitName = sign.getLine(1).toLowerCase();
				if (kit.isKit(kitName)) {
					if (sign.getLine(3).equals(ChatColor.AQUA + "Donator Kit")) {
						buy(p, kitName, true);
					} else {
						buy(p, kitName, false);
					}
				} else {
					p.sendMessage(prefix + ChatColor.RED + "This kit no longer exists");
					sign.setLine(1, ChatColor.RED + "Deleted");
					sign.setLine(2, null);
					sign.setLine(3, null);
					sign.update();
				}
			}
		}
	}
}
