package de.kaes3kuch3n.varo.listeners;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import de.kaes3kuch3n.varo.VaroPlugin;

public class PlayerDeathListener implements Listener {
	
	private VaroPlugin main;
	
	public PlayerDeathListener(VaroPlugin instance) {
		main = instance;
		main.getServer().getPluginManager().registerEvents(this, main);
	}
	
	@EventHandler
	public void on(PlayerDeathEvent event) {
		
		Player p = event.getEntity();
		
		if(main.config.getBoolean("started")) {
			File playerFile = new File(main.playerFilePath, p.getUniqueId() + ".yml");
			FileConfiguration playerStats = YamlConfiguration.loadConfiguration(playerFile);
			int playerLives = playerStats.getInt("stats.lives");
			playerStats.set("stats.deaths", (playerStats.getInt("stats.deaths") + 1));
			
			playerLives--;
			
			if(p.getKiller().getType() == EntityType.PLAYER) {
				File killerFile = new File(main.playerFilePath, p.getKiller().getUniqueId() + ".yml");
				FileConfiguration killerStats = YamlConfiguration.loadConfiguration(killerFile);
				killerStats.set("stats.kills", (killerStats.getInt("stats.kills") + 1));
				try {
					killerStats.save(killerFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if(playerLives <= 0) {
				p.kickPlayer(main.PREFIX + "\n\n" + 
						"Du hast keine Leben mehr übrig. Das Spiel ist hiermit für Dich vorbei. \n\n" + 
						"Vote für unseren Server, um wieder Leben zu bekommen und weiterzuspielen.");
			}
			
			playerStats.set("stats.lives", playerLives);
			try {
			playerStats.save(playerFile);
			} catch (IOException e) {
			e.printStackTrace();
			}
		}
	}
	
}
