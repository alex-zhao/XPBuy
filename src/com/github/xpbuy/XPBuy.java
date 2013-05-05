package com.github.xpbuy;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;

public class XPBuy extends JavaPlugin {
	/*
	 * TODO: real permissions for donator kits
	 * TODO: fix kit overriding (create same name kit)
	 * TODO: more admin features?
	 * TODO: potions
	 * TODO: rewrite kit perms? (see first todo)
	 * TODO: functionality for damage values and enchants at once
	 */
	public static ArrayList<String> kits;
	public static ArrayList<Player> adminList = new ArrayList<Player>();
	public static FileConfiguration config;
	public static String prefix = ChatColor.GOLD + "[XPBuy] ";
	@Override
	public void onEnable() {
		saveDefaultConfig();
		config = getConfig();
		//kit = new Kit();
		kits = new ArrayList<String>(config.getConfigurationSection("kits").getKeys(false));
		initKitPerms();
		getServer().getPluginManager().registerEvents(new BuySigns(), this);
		getLogger().info("XPBuy has been enabled!");
	}
	@Override
	public void onDisable() {
		getLogger().info("XPBuy has been disabled!");
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("xpbuy") || cmd.getName().equalsIgnoreCase("xpb")) { //opens up the buy menu
			if (args.length == 0 || args[0].equalsIgnoreCase("help")) { // displays help
				if (sender.hasPermission("xpbuy.help")) {
					sender.sendMessage(prefix + ChatColor.GREEN + "XPBUY V" + getDescription().getVersion() + " HELP:");
					sender.sendMessage(ChatColor.GREEN + "/xpbuy or /xpbuy help: Displays this help message");
					sender.sendMessage(ChatColor.GREEN + "/xpbuy <kit>: Buys a kit");
					sender.sendMessage(ChatColor.GREEN + "/xpbuy list: Lists all available kits and their prices");
					if (sender.hasPermission("xpbuy.admin")) {
						sender.sendMessage(ChatColor.GREEN + "-- ADMIN COMMANDS --");
						sender.sendMessage(ChatColor.GREEN + "/xpbuy create <name> <price> <isdonator> <item> <item>: Creates a kit with specified items (at least 1)");
						sender.sendMessage(ChatColor.GREEN + "/xpbuy remove <name>: Removes a kit");
						sender.sendMessage(ChatColor.GREEN + "/xpbuy edit: Kit editing help. Not yet implemented, use /xpbuy create for now");
						sender.sendMessage(ChatColor.GREEN + "/xpbuy giveperm <player> <kit>: Gives a player permission to use a donator kit");
						sender.sendMessage(ChatColor.GREEN + "/xpbuy delperm <player> <kit>: Deletes a player's permission to use a donator kit");		
						sender.sendMessage(ChatColor.GREEN + "/xpbuy admin: Gives admin access (All kits free, donator kit access)");
						sender.sendMessage(ChatColor.GREEN + "/xpbuy signonly <kit>: Toggles if a kit can be bought with the command");
					}
					sender.sendMessage(ChatColor.DARK_GREEN + "Note: /xpbuy can be substituted with /xpb");
					return true;
				} else {
					sender.sendMessage(prefix + ChatColor.RED + "You don't have permission!");
					return false;
				}
			} else if (args.length == 1) {
				if (args[0].equalsIgnoreCase("list")) { // lists out all kits
					if (sender.hasPermission("xpbuy.list")) {
						sender.sendMessage(prefix + ChatColor.GREEN + "Available kits:");
						for (int i = 0; i < kits.size(); i++) {
							String price;
							if (Kit.getPrice(kits.get(i)) == -1 || adminList.contains((Player) sender)) {
								price = "Free";
							} else {
								price = String.valueOf(Kit.getPrice(kits.get(i)));
							}
							String isDonator = "";
							if (Kit.isDonator(kits.get(i))) {
								isDonator = ChatColor.AQUA + "donator";
							}
							sender.sendMessage(ChatColor.GREEN + kits.get(i) + " (" + price + ") " + isDonator);
						}
						return true;
					} else {
						sender.sendMessage(prefix + ChatColor.RED + "You don't have permission!");
						return false;
					}
				} else if (args[0].equalsIgnoreCase("admin")) {
					if (sender.hasPermission("xpbuy.adminmode")) {
						if (!adminList.contains((Player) sender)) {
							adminList.add((Player) sender);
							sender.sendMessage(prefix + ChatColor.GREEN + "Admin mode enabled.");
							return true;
						} else {
							adminList.remove((Player) sender);
							sender.sendMessage(prefix + ChatColor.GREEN + "Admin mode disabled.");
							return true;
						}
					} else {
						sender.sendMessage(prefix + ChatColor.RED + "You don't have permission!");
						return false;
					}
				} else { // everything else will be a kit or an error.
					if (sender.hasPermission("xpbuy.buy")) {
						if (Kit.isKit(args[0])) {
							if (Kit.isSignOnly(args[0])) {
								pay((Player) sender, args[0], Kit.isDonator(args[0]));
								return true;
							} else {
								sender.sendMessage(prefix + ChatColor.RED + "Sorry, this kit can only be used with a sign");
								return false;
							}
						} else {
							if (args[0].equalsIgnoreCase("create")) {
								sender.sendMessage(prefix + ChatColor.RED + "Review your arguments count! /xpbuy create <name> <price> <isdonator> <item> <item>");
								return false;
							} else if (args[0].equalsIgnoreCase("remove")) {
								sender.sendMessage(prefix + ChatColor.RED + "Review your arguments count! /xpbuy remove <name>");
								return false;
							} else if (args[0].equalsIgnoreCase("edit")) {
								sender.sendMessage(prefix + ChatColor.RED + "Review your arguments count!");
								return false;
							} else if (args[0].equalsIgnoreCase("giveperm")) {
								sender.sendMessage(prefix + ChatColor.RED + "Review your arguments count! /xpbuy giveperm <player> <kit>");
							} else if (args[0].equalsIgnoreCase("delperm")) {
								sender.sendMessage(prefix + ChatColor.RED + "Review your arguments count! /xpbuy delperm <player> <kit>");
							} else if (args[0].equalsIgnoreCase("signonly")) {
								sender.sendMessage(prefix + ChatColor.RED + "Review your arguments count! /xpbuy signonly <kit>");
							} else {
								sender.sendMessage(prefix + ChatColor.RED + "Not a valid kit name!");
								return false;
							}
						}
					} else {
						sender.sendMessage(prefix + ChatColor.RED + "You don't have permission!");
						return false;
					}
				}
			} else {
				if (args[0].equalsIgnoreCase("create")) {
					if (sender.hasPermission("xpbuy.create")) {
						if (args.length < 5) {
							sender.sendMessage(prefix + ChatColor.RED + "Review your arguments count! /xpbuy create <name> <price> <isdonator> <item> <item>");
							return false;
						} else {
							String derp = "";
							for (int i = 4; i < args.length; i++) {
								derp = args[i];
								if (args[i].contains(":")) {
									derp = derp.replace(':', '0'); // replace non-integer with integer for integer check
								}
								if (args[i].contains(";")) {
									derp = derp.replace(';', '0');
								}
								if (args[i].contains("-")) { // TODO: check if enchant + level format is valid
									derp = derp.replace('-', '0');
								}
								try {
									Integer.parseInt(args[2]);
									Integer.parseInt(derp);
								} catch (NumberFormatException nfe) {
									sender.sendMessage(prefix + ChatColor.RED + "One or more of your integer arguments is not a number!");
									return false;
								}
							}
							config.createSection("kits." + args[1].toLowerCase());
							config.createSection("prices." + args[1].toLowerCase());
							config.createSection("isdonator." + args[1].toLowerCase());
							config.createSection("signonly." + args[1].toLowerCase());
							config.set("prices." + args[1].toLowerCase(), Integer.parseInt(args[2]));
							config.set("isdonator." + args[1].toLowerCase(), Boolean.parseBoolean(args[3]));
							config.set("signonly." + args[1].toLowerCase(), false);
							ArrayList<String> items = new ArrayList<String>();
							for (int i = 4; i < args.length; i++) {
								items.add(args[i]); // change
							}
							config.set("kits." + args[1].toLowerCase(), items);
							config.set("signonly." + args[1].toLowerCase(), false);
							this.saveConfig();
							sender.sendMessage(prefix + ChatColor.GREEN + "Successfully created kit " + args[1].toLowerCase() + "!");
							Kit.updateKits();
							initKitPerms();
							return true;
						}
					} else {
						sender.sendMessage(prefix + ChatColor.RED + "You don't have permission!");
						return false;
					}
				} else if (args[0].equalsIgnoreCase("remove")) {
					if (sender.hasPermission("xpbuy.remove")) {
						if (args.length > 2) {
							sender.sendMessage(prefix + ChatColor.RED + "Too many arguments! /xpbuy remove <name>");
							return false;
						} else {
							if (Kit.isKit(args[1])) {
								config.set("kits." + args[1].toLowerCase(), null);
								config.set("prices." + args[1].toLowerCase(), null);
								config.set("isdonator." + args[1].toLowerCase(), null);
								config.set("kitperms." + args[1].toLowerCase(), null);
								config.set("signonly." + args[1].toLowerCase(), null);
								this.saveConfig();
								sender.sendMessage(prefix + ChatColor.GREEN + "Successfully deleted kit " + args[1].toLowerCase() + "!");
								Kit.updateKits();
								return true;
							} else {
								sender.sendMessage(prefix + ChatColor.RED + "Not a valid kit!");
								return false;
							}
						}
					} else {
						sender.sendMessage(prefix + ChatColor.RED + "You don't have permission!");
						return false;
					}
				} else if (args[0].equalsIgnoreCase("edit")) {
					if (sender.hasPermission("xpbuy.edit")) {
						sender.sendMessage(prefix + ChatColor.GREEN + "Will be implemented in the future");
						return true;
					} else {
						sender.sendMessage(prefix + ChatColor.RED + "You don't have permission!");
						return false;
					}
				} else if (args[0].equalsIgnoreCase("giveperm")) {
					if (args.length != 3) {
						sender.sendMessage(prefix + ChatColor.RED + "Review your arguments count! /xpbuy giveperm <player> <kit>");
						return false;
					} else {
						if (Kit.giveKitPerm((Player) sender, Bukkit.getServer().getPlayer(args[1]), args[2])) {
							sender.sendMessage(prefix + ChatColor.GREEN + "Successfully added " + args[1] + " to kit " + args[2] + "!");
							Bukkit.getServer().getPlayer(args[1]).sendMessage(prefix + ChatColor.GREEN + "You now have permission to use the " + args[2] + " kit!");
							return true;
						} else {
							return false;
						}
					}
				} else if (args[0].equalsIgnoreCase("delperm")) {
					if (args.length != 3) {
						sender.sendMessage(prefix + ChatColor.RED + "Review your arguments count! /xpbuy delperm <player> <kit>");
						return false;
					} else {
						if (Kit.delKitPerm(Bukkit.getServer().getPlayer(args[1]), args[2])) {
							sender.sendMessage(prefix + ChatColor.GREEN + "Successfully deleted " + args[1] + " from kit " + args[2] + "!");
							return true;
						} else {
							return false;
						}
					}
				} else if (args[0].equalsIgnoreCase("signonly")) { // implement
					if (Kit.setSignOnly(args[1])) {
						sender.sendMessage(prefix + ChatColor.GREEN + "Kit " + args[1] + " is now sign-only!");
						return true;
					} else {
						sender.sendMessage(prefix + ChatColor.GREEN + "Kit " + args[1] + " is no longer sign-only!");
						return true;
					}
				} else {
					sender.sendMessage(prefix + ChatColor.RED + "Too many arguments!");
					return false;
				}
			}
		}
		return false; 
	}
	public void pay(Player p, String kitName, boolean isDonator) {
		if (!isDonator) {
			int level = p.getLevel();
			int price = Kit.getPrice(kitName);
			if (adminList.contains(p)) {
				p.getActivePotionEffects().clear();
				Kit.giveKit(p, kitName);
			} else {
				if (level >= price) {
					if (price == -1) {
						p.getActivePotionEffects().clear();
						Kit.giveKit(p, kitName);
					} else {
						p.setLevel(level - price);
						p.getActivePotionEffects().clear();
						Kit.giveKit(p, kitName);
					}
				} else {
					p.sendMessage(prefix + ChatColor.RED + "You don't have enough levels!");
				}
			}
		} else {
			if (Kit.hasKitPerm(p, kitName) || adminList.contains(p)) {
				p.getActivePotionEffects().clear();
				Kit.giveKit(p, kitName);
			} else {
				p.sendMessage(prefix + ChatColor.RED + "You don't have permission!");
			}
		}
	}
	public void initKitPerms() {
		for (int i = 0; i < kits.size(); i++) {
 			if (!config.getConfigurationSection("kitperms").getKeys(false).contains(kits.get(i).toLowerCase()) && Kit.isDonator(kits.get(i).toLowerCase())) {
 				config.createSection("kitperms." + kits.get(i).toLowerCase());
 				config.set("kitperms." + kits.get(i).toLowerCase(), "Roxas0321");
 			}
		}
	}
}
