package radian628.randomminigame;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SetMinimumPlayersCommand implements CommandExecutor {
    
    @Override 
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            try {
                MinigameUtils.minPlayers = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        }
        return false;
    }
}
