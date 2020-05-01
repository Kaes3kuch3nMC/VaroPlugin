package de.kaes3kuch3n.varo;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import de.kaes3kuch3n.varo.listeners.PlayerDeathListener;
import de.kaes3kuch3n.varo.listeners.PlayerLoginListener;

public class VaroPlugin extends JavaPlugin {
	
	public final String PREFIX = ChatColor.DARK_GREEN + "[" + getDescription().getName() + "] " + ChatColor.GRAY;
	public FileConfiguration config = getConfig();
	public final File playerFilePath = new File(getDataFolder() + File.separator + "players");
	
	@Override
	public void onEnable() {
		
		//Config
		config.addDefault("started", false);
		config.options().copyDefaults(true);
		saveConfig();
		
		getServer().getConsoleSender().sendMessage(PREFIX + "Plugin loaded.");
		new VaroCommand(this);
		new PlayerLoginListener(this);
		new PlayerDeathListener(this);
	}
	
	@Override
	public void onDisable() {
		getServer().getConsoleSender().sendMessage(PREFIX + "Plugin stopped.");
		
		Bukkit.getScheduler().cancelAllTasks();
		Bukkit.getScheduler().cancelTasks(this);
		
		saveConfig();
	}
	
}
