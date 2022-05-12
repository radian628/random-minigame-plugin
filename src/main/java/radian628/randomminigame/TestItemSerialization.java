package radian628.randomminigame;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TestItemSerialization implements CommandExecutor {

    private File dataFolder;
    public TestItemSerialization(File df) {
        dataFolder = df;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player)sender;

            File fileConfigFile = new File(dataFolder, "items-test.yml");
            FileConfiguration itemSource = YamlConfiguration.loadConfiguration(fileConfigFile);
            List<Object> itemList = (List<Object>)itemSource.getList("root");
            for (int i = 0; i < 36; i++) {
                ItemStack items = player.getInventory().getItem(i);
                if (items != null) itemList.add(items.serialize());
            }
            itemSource.set("root", itemList);
            try {
                itemSource.save(fileConfigFile);
            } catch (IOException except) {
                return false;
            }

            return true;
        } else {
            return false;
        }
    }
}
