package radian628.randomminigame;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import radian628.randomminigame.MinigameUtils;


public class StartGameCommand implements CommandExecutor {

    final static double SIDE_LENGTH = 15.0;
    final static int ISLAND_HALF_SIZE = 3;
    private File dataFolder;
    public StartGameCommand(File df) {
        dataFolder = df;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (players.size() == 0) return false;

        MinigameUtils.isGameActive = true;
        MinigameUtils.minigameCounter = 0;

        World world = Bukkit.getWorlds().get(0);

        //clear region around random minigame spawn
        for (int z = -2; z < 2; z++) {
            for (int y = -2; y < 10; y++) {
                for (int x = -2; x < 2; x++) {
                    int z32 = z * 32;
                    int y32 = y * 32;
                    int x32 = x * 32;
                    int z32_2 = z * 32 + 31;
                    int y32_2 = y * 32 + 31;
                    int x32_2 = x * 32 + 31;
                    Bukkit.dispatchCommand(sender, 
                    "fill " 
                    + (x32) + " "
                    + (y32) + " "
                    + (z32) + " "
                    + (x32_2) + " "
                    + (y32_2) + " "
                    + (z32_2) + " "
                    + "minecraft:air"
                    );
                }
            }
        }
        
        double radius = SIDE_LENGTH / (2.0 * Math.sin(2.0 * Math.PI / players.size()));
        if (players.size() <= 2) radius = SIDE_LENGTH / 2.0;
        MinigameUtils.playersRemaining = new HashSet<Player>();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            player.getInventory().clear();
            player.setGameMode(GameMode.SURVIVAL);
            MinigameUtils.playersRemaining.add(player);
            double angle = 2.0 * Math.PI / players.size() * i;
            double playerXPos = (Math.cos(angle) * radius);
            double playerYPos = 295.0;
            double playerZPos = (Math.sin(angle) * radius);
            
            Location playerLocation = new Location(world, playerXPos, playerYPos, playerZPos);
            player.teleport(playerLocation);
            player.setBedSpawnLocation(playerLocation, true);

            int playerXPosI = (int)playerXPos;
            int playerYPosI = (int)playerYPos;
            int playerZPosI = (int)playerZPos;

            MinigameUtils.clearRegion(world, Material.STONE,
            playerXPosI - ISLAND_HALF_SIZE,
            playerYPosI - 2,
            playerZPosI - ISLAND_HALF_SIZE,
            playerXPosI + ISLAND_HALF_SIZE + 1,
            playerYPosI - 1,
            playerZPosI + ISLAND_HALF_SIZE + 1
            );
        }
        
        return false;
    }
}
