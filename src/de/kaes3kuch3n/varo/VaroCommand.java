package de.kaes3kuch3n.varo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class VaroCommand implements CommandExecutor {
	
	private VaroPlugin main;
	File playerFile;
	FileConfiguration playerStats;
	
	public VaroCommand(VaroPlugin instance) {
		main = instance;
		main.getServer().getPluginCommand("varo").setExecutor(this);
	}
	
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if(sender instanceof Player) {
			playerFile = new File(main.playerFilePath, ((Player) sender).getUniqueId() + ".yml");
			playerStats = YamlConfiguration.loadConfiguration(playerFile);
		}
		
		if(args.length == 0) {
			sendInfoMessage(sender);
			return true;
		}
		
		switch(args[0]) {
			case "help":
				if(args.length == 2) {
					sendHelpMessage(sender, Integer.parseInt(args[1]));
					return true;
				} else {
					sendHelpMessage(sender, 1);
					return true;
				}
				
			case "reload":
				if(sender instanceof Player && !sender.isOp() && !sender.hasPermission("varo.reload")) {
					sender.sendMessage(ChatColor.RED + "You don't have the permission to do this!");
					return true;
				}
				
				main.reloadConfig();
				sender.sendMessage(main.PREFIX + "Config reloaded.");
				return true;
				
			case "start":
				if(sender instanceof Player && !sender.isOp() && !sender.hasPermission("varo.startstop")) {
					sender.sendMessage(ChatColor.RED + "You don't have the permission to do this!");
					return true;
				}
				
				if(main.config.getBoolean("started")) {
					sender.sendMessage(main.PREFIX + "Das Spiel läuft bereits.");
					return true;
				}
				
				sender.sendMessage(main.PREFIX + "Das Spiel wird gestartet.");
				
				final int[] countdown = {10};
				
				new BukkitRunnable() {
					@Override
					public void run() {
						if(countdown[0] == 0) {
							main.getServer().getOnlinePlayers().forEach(player->{
								player.playSound(player.getLocation(), Sound.EXPLODE, 1.0F, 1.0F);
								player.sendMessage(main.PREFIX + "Das Spiel startet jetzt! Viel Erfolg!");
							});
							this.cancel();
							return;
						}
						
						main.getServer().getOnlinePlayers().forEach(player->{
							player.sendMessage(main.PREFIX + "Das Spiel startet in " + countdown[0] + " Sekunden!");
						});
						countdown[0]--;
					}
				}.runTaskTimerAsynchronously(main, 0L, 20L);
				
				new BukkitRunnable() {
					@Override
					public void run() {
						main.config.set("started", true);
						
						ItemStack item = new ItemStack(Material.WOOD_SWORD, 1);
						item.addEnchantment(Enchantment.DAMAGE_ALL, 1);
						ItemMeta meta = item.getItemMeta();
						meta.setDisplayName("Sword of Beginners");
						List<String> lore = new ArrayList<String>();
						lore.add("§8Just a basic sword");
						meta.setLore(lore);
						item.setItemMeta(meta);
						
						main.getServer().getOnlinePlayers().forEach(player->{
							player.getInventory().addItem(item);
						});
					}
				}.runTaskLater(main, 10 * 20L);
				return true;
				
			case "stop":
				if(sender instanceof Player && !sender.isOp() && !sender.hasPermission("varo.startstop")) {
					sender.sendMessage(ChatColor.RED + "You don't have the permission to do this!");
					return true;
				}
				
				if(!main.config.getBoolean("started")) {
					sender.sendMessage(main.PREFIX + "Das Spiel ist bereits gestoppt.");
				}
				
				main.config.set("started", false);
				main.getServer().getOnlinePlayers().forEach(player->{
					player.sendMessage(main.PREFIX + "Das Spiel wurde gestoppt.");
				});
				return true;
				
			case "lives":
				if(sender instanceof Player && !sender.isOp() && !sender.hasPermission("varo.lives.own")) {
					sender.sendMessage(ChatColor.RED + "You don't have the permission to do this!");
					return true;
				}
				
				if(args.length == 1) {
					
					if(!(sender instanceof Player)) {
						sender.sendMessage("Please specify a player: \n"
								+ "/varo lives <player>");
						return true;
					}
					
					sender.sendMessage(main.PREFIX + "Du hast noch " + playerStats.getInt("stats.lives") + " Leben.");
					return true;
					
				} else if(args.length == 2) {
					if(sender instanceof Player && !sender.isOp() && !sender.hasPermission("varo.lives.other")) {
						sender.sendMessage(ChatColor.RED + "You don't have the permission to do this!");
						return true;
					}
					
					playerFile = new File(main.playerFilePath, main.getServer().getOfflinePlayer(args[1]).getUniqueId() + ".yml");
					playerStats = YamlConfiguration.loadConfiguration(playerFile);
					sender.sendMessage(main.PREFIX + args[1] + " hat noch " + playerStats.getInt("stats.lives") + " Leben.");
					return true;
					
				} else if(args.length == 3) {
					
					if(sender instanceof Player && !sender.isOp() && !sender.hasPermission("varo.lives.edit.own")) {
						sender.sendMessage(ChatColor.RED + "You don't have the permission to do this!");
						return true;
					}
					
					if(!(sender instanceof Player)) {
						sender.sendMessage("Please specify a player: \n"
								+ "/varo lives <player> <add|remove|set> <lives>");
						return true;
					}
					
					if(args[1].equalsIgnoreCase("add")) {
						
						playerStats.set("stats.lives", playerStats.getInt("stats.lives") + Integer.parseInt(args[2]));
						
						try {
							playerStats.save(playerFile);
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						sender.sendMessage(main.PREFIX + args[2] + " Leben hinzugefügt.");
						return true;
						
					} else if(args[1].equalsIgnoreCase("remove")) {
						
						playerStats.set("stats.lives", playerStats.getInt("stats.lives") - Integer.parseInt(args[2]));
						
						try {
							playerStats.save(playerFile);
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						sender.sendMessage(main.PREFIX + args[2] + " Leben entfernt.");
						return true;
						
					} else if(args[1].equalsIgnoreCase("set")) {
						
						playerStats.set("stats.lives", Integer.parseInt(args[2]));
						
						try {
							playerStats.save(playerFile);
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						sender.sendMessage(main.PREFIX + "Leben auf " + args[2] + " gesetzt.");
						return true;
					}
					
				} else if(args.length == 4) {
					
					if(sender instanceof Player && !sender.isOp() && !sender.hasPermission("varo.lives.edit.other")) {
						sender.sendMessage(ChatColor.RED + "You don't have the permission to do this!");
						return true;
					}
					
					playerFile = new File(main.playerFilePath, main.getServer().getOfflinePlayer(args[1]).getUniqueId() + ".yml");
					playerStats = YamlConfiguration.loadConfiguration(playerFile);
					
					if(args[2].equalsIgnoreCase("add")) {
						
						playerStats.set("stats.lives", playerStats.getInt("stats.lives") + Integer.parseInt(args[3]));
						
						try {
							playerStats.save(playerFile);
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						sender.sendMessage(main.PREFIX + args[1] + " wurden " + args[3] + " Leben hinzugefügt.");
						return true;
						
					} else if(args[2].equalsIgnoreCase("remove")) {
						
						playerStats.set("stats.lives", playerStats.getInt("stats.lives") - Integer.parseInt(args[3]));
						
						try {
							playerStats.save(playerFile);
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						sender.sendMessage(main.PREFIX + args[1] + " wurden " + args[3] + " Leben entfernt.");
						return true;
						
					} else if(args[2].equalsIgnoreCase("set")) {
						
						playerStats.set("stats.lives", Integer.parseInt(args[3]));
						
						try {
							playerStats.save(playerFile);
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						sender.sendMessage(main.PREFIX + "Leben von " + args[1] + " wurden auf " + args[3] + " gesetzt.");
						return true;
					}
					
				} else {
					sender.sendMessage(ChatColor.RED + "Command Usage: /varo lives [player] <add|remove|set> <lives> \n"
							+ "or /varo lives [player]");
				}
				
			case "reset":
				if(sender instanceof Player && !sender.isOp() && !sender.hasPermission("varo.reset.own")) {
					sender.sendMessage(ChatColor.RED + "You don't have the permission to do this!");
					return true;
				}
				
				if(args.length != 1 && args.length != 2) {
					sender.sendMessage(ChatColor.RED + "Command Usage: /varo reset [player]");
				}
				
				if(args.length == 1 && !(sender instanceof Player)) {
					sender.sendMessage("Please specify a player: \n"
							+ "/varo reset <player>");
				}
				
				if(args.length == 2) {
					
					if(sender instanceof Player && !sender.isOp() && !sender.hasPermission("varo.reset.other")) {
						sender.sendMessage(ChatColor.RED + "You don't have the permission to do this!");
						return true;
					}
					
					playerFile = new File(main.playerFilePath, main.getServer().getOfflinePlayer(args[1]).getUniqueId() + ".yml");
					playerStats = YamlConfiguration.loadConfiguration(playerFile);
				}
				
				playerStats.set("stats.kills", 0);
				playerStats.set("stats.deaths", 0);
				playerStats.set("stats.lives", 3);
				try {
					playerStats.save(playerFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				
			default:
				sendHelpMessage(sender, 1);
				return true;
		}
	}
	
	public void sendInfoMessage(CommandSender sender) {
		sender.sendMessage(main.PREFIX + "Plugin loaded.");
		sender.sendMessage(ChatColor.GRAY + "Version: " + main.getDescription().getVersion());
		if(main.config.getBoolean("started")) {
			sender.sendMessage(ChatColor.GRAY + "Game running");
		} else {
			sender.sendMessage(ChatColor.GRAY + "Game stopped");
		}
	}
	
	public void sendHelpMessage(CommandSender sender, int page) {
		switch(page) {
			default: 
				sender.sendMessage("========== " + main.PREFIX + "(1/2) ==========");
				sender.sendMessage("/varo - Get plugin info");
				sender.sendMessage("/varo start - Start the game");
				sender.sendMessage("/varo stop - Stop the game");
				sender.sendMessage("/varo lives - Display your current amount of lives");
				sender.sendMessage("/varo lives [player] add <lives> - Add the given amount of lives \n" + 
						"        to your [the players] lives");
				sender.sendMessage("/varo lives remove <lives> - Remove the given amount of \n" + 
						"        lives from your [the players] lives");
				sender.sendMessage("");
				break;
				
			case 2:
				sender.sendMessage("========== " + main.PREFIX + "(2/2) ==========");
				sender.sendMessage("/varo lives set <lives> - Set your [the players] lives to the given \n" + 
						"        amount of lives");
				sender.sendMessage("/varo reset [player] - Reset your [the players] stats");
				sender.sendMessage("");
				sender.sendMessage("");
				sender.sendMessage("");
				sender.sendMessage("");
				sender.sendMessage("");
				sender.sendMessage("");
				break;
		}
	}
}
