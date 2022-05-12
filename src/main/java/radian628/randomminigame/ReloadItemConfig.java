package radian628.randomminigame;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ReloadItemConfig implements CommandExecutor {

    private JavaPlugin plugin;
    public ReloadItemConfig(JavaPlugin pg) {
        plugin = pg;
    }

    @Override 
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        MinigameUtils.reloadItemConfig(plugin);
        return true;
    }
}
