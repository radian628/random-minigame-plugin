package radian628.randomminigame;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class MinigameUtils {
    public static int ISLAND_SIZE = 3;
    public static double ISLAND_CIRCLE_RADIUS = 8.0;

    public static boolean isGameActive = false;
    public static HashSet<Player> playersRemaining;
    public static int minigameCounter = 0;
    public static int minPlayers = 2;
    public static ItemChooser itemChooser;
    public static void clearRegion(World world, Material material, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
        for (int z = zMin; z < zMax; z++) {
            for (int y = yMin; y < yMax; y++) {
                for (int x = xMin; x < xMax; x++) {
                    Block block = world.getBlockAt(x, y, z);
                    block.setType(material);
                }
            }
        }
    }
    public static void reloadItemConfig(JavaPlugin plugin) {
        File itemSourceFile = new File(plugin.getDataFolder(), "items.yml");
        FileConfiguration itemSource = YamlConfiguration.loadConfiguration(itemSourceFile);

        MinigameUtils.itemChooser = new ItemChooser();
        List<Map<String, Object>> itemConfigList = (List<Map<String, Object>>)itemSource.getList("items");
        for (Map<String, Object> itemChoice : itemConfigList) {
            List<Map<String, Object>> itemList = (List<Map<String, Object>>)itemChoice.get("items");
            List<ItemStack> itemListAsItems = new ArrayList<ItemStack>();
            for (int i = 0; i < itemList.size(); i++) {
                itemListAsItems.add(ItemStack.deserialize(itemList.get(i)));
            }
            MinigameUtils.itemChooser.addChoice(itemListAsItems, (Double)itemChoice.get("weight"));
        }

        MinigameUtils.ISLAND_CIRCLE_RADIUS = plugin.getConfig().getDouble("island-circle-radius", 10.0);
        MinigameUtils.ISLAND_SIZE = plugin.getConfig().getInt("island-size", 4);
    }
}
