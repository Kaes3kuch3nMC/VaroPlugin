package de.kaes3kuch3n.varo.listeners;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import de.kaes3kuch3n.varo.VaroPlugin;

public class PlayerLoginListener implements Listener {
	
	private VaroPlugin main;
	
	public PlayerLoginListener(VaroPlugin instance) {
		main = instance;
		main.getServer().getPluginManager().registerEvents(this, main);
	}
	
	@EventHandler
	public void on(PlayerLoginEvent event) {
		Player p = event.getPlayer();
		
		File playerFile = new File(main.playerFilePath, p.getUniqueId() + ".yml");
		
		if(!playerFile.exists()) {
			try {
				new File(main.getDataFolder() + File.separator + "players").mkdirs();
				playerFile.createNewFile();
				main.getServer().getConsoleSender().sendMessage(main.PREFIX + "New playerfile created for player " + p.getName());
				
				FileConfiguration playerStats = YamlConfiguration.loadConfiguration(playerFile);
				playerStats.set("player.name", p.getName());
				playerStats.set("player.timeLeftForToday", (10 * 60 * 1000));
				playerStats.set("stats.kills", 0);
				playerStats.set("stats.deaths", 0);
				playerStats.set("stats.lives", 3);
				
				try {
					playerStats.save(playerFile);
					main.getServer().getConsoleSender().sendMessage(main.PREFIX + "Playerfile saved");
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		FileConfiguration playerStats = YamlConfiguration.loadConfiguration(playerFile);
		
		if(main.config.getBoolean("started") && playerStats.getInt("stats.lives") <= 0) {
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, 
					main.PREFIX + "\n\n" + 
						"Du hast keine Leben mehr übrig und kannst den Server nicht mehr betreten. \n\n" + 
						"Vote für unseren Server, um wieder Leben zu bekommen und weiterzuspielen.");
			return;
		}
	}
}
