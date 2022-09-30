package radian628.randomminigame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class App extends JavaPlugin implements Listener {
    private void configureMinigame() {
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configureMinigame();
        MinigameUtils.ballLightnings = new HashMap<Entity, Integer>();
        MinigameUtils.reloadItemConfig(this);

        this.getCommand("startrandomminigame").setExecutor(new StartGameCommand(getDataFolder()));
        this.getCommand("setminimumplayers").setExecutor(new SetMinimumPlayersCommand());
        this.getCommand("testitemserialization").setExecutor(new TestItemSerialization(getDataFolder()));
        this.getCommand("reloaditemconfig").setExecutor(new ReloadItemConfig(this));
        getLogger().info("Random Minigame Plugin Initialized.");

        getServer().getPluginManager().registerEvents(this, this);

        // get items
        Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin)this, new Runnable() {
            @Override
            public void run() {
                if (MinigameUtils.isGameActive) {
                    if (MinigameUtils.minigameCounter < MinigameUtils.COUNTDOWN_LENGTH) {
                        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                            player.sendTitle(String.valueOf(MinigameUtils.COUNTDOWN_LENGTH - MinigameUtils.minigameCounter - 1),
                             "", 2, 16, 2);
                        }
                    }
                    MinigameUtils.minigameCounter++;
                    boolean shouldGiveItem = MinigameUtils.minigameCounter < (MinigameUtils.COUNTDOWN_LENGTH + 3) && MinigameUtils.minigameCounter > (MinigameUtils.COUNTDOWN_LENGTH - 1);
                    if (MinigameUtils.minigameCounter % 30 == 0) {
                        shouldGiveItem = true;
                    }
                    if (shouldGiveItem) {
                        getServer().broadcastMessage("Everyone received something!");
                        for (int i = 0; i < 3; i++) {
                            List<ItemStack> itemChoice = MinigameUtils.itemChooser.getRandomItems();
                            for (ItemStack items : itemChoice) {
                                for (Player remainingPlayer : MinigameUtils.playersRemaining) {
                                    remainingPlayer.getInventory().addItem(items);
                                }
                            }
                        }
                    }
                }
            }
        }, 0L, 20L);

        // ball lightning and other per-tick things
        Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin)this, new Runnable() {
            @Override
            public void run() {
                if (MinigameUtils.minigameCounter < MinigameUtils.COUNTDOWN_LENGTH) {
                    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                        if (MinigameUtils.initPlayerLocations.containsKey(player)) {
                            Location newLoc = MinigameUtils.initPlayerLocations.get(player);
                            player.teleport(newLoc);
                        }
                    }
                }
                for (Entry<Entity, Integer> p : MinigameUtils.ballLightnings.entrySet()) {
                  
                    if (p.getValue() > 5) {
                        p.getKey().getWorld().spawnEntity(p.getKey().getLocation(), EntityType.LIGHTNING);
                    }
                    p.setValue(p.getValue() + 1);
                }
                MinigameUtils.ballLightnings.keySet().removeAll(
                        MinigameUtils.ballLightnings.entrySet().stream()
                        .filter(p -> p.getKey().isDead()).map(e -> e.getKey())
                        .collect(Collectors.toList())
                    );
            }
        }, 0L, 1L);
    }
    @Override
    public void onDisable() {
        getLogger().info("Unloading Random Minigame Plugin!");
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (MinigameUtils.isGameActive) {
            Player player = event.getEntity();
            MinigameUtils.playersRemaining.remove(player);
            getServer().broadcastMessage("Players Remaining:");
            for (Player playerRemaining : MinigameUtils.playersRemaining) {
                getServer().broadcastMessage(playerRemaining.getName());
            }
            player.setGameMode(GameMode.SPECTATOR);
        }

        if (MinigameUtils.playersRemaining.size() < MinigameUtils.minPlayers) {
            MinigameUtils.isGameActive = false;
            List<Player> players = new ArrayList<>(getServer().getOnlinePlayers());
            for (Player player : players) {
                Location loc = new Location(event.getEntity().getWorld(), 300L, 100L, 300L);
                player.setBedSpawnLocation(loc, true);
                player.teleport(loc);
                player.getInventory().clear();
            }

            World world = Bukkit.getWorlds().get(0);
            WorldBorder border = world.getWorldBorder();
            border.setCenter(0, 0);
            border.setSize(900);
        }
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Entity e = event.getEntity();
        if (e.getType() == EntityType.SNOWBALL) {
            List<Player> players = new ArrayList<>(getServer().getOnlinePlayers());
            Player closestPlayer = null;
            double closestPlayerDistance = Double.POSITIVE_INFINITY;
            for (Player player : players) {
                double distance = player.getLocation().distance(e.getLocation());
                if (distance < closestPlayerDistance) {
                    closestPlayer = player;
                    closestPlayerDistance = distance;
                }
            }
            if (closestPlayer != null && 
            MinigameUtils.isNamedItem(closestPlayer.getInventory().getItemInMainHand(), Material.SNOWBALL, "Ball Lightning")) {
              
                MinigameUtils.ballLightnings.put(e, 0);
            }
        }
    }



    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        Player p = event.getPlayer();

        ItemStack item = p.getInventory().getItemInMainHand();



        // if (MinigameUtils.isNamedItem(item, Material.SNOWBALL, "Ball Lightning")) {
        //     getLogger().info("BALL LIGHTNING");
        //     if (event.getAction() == Action.)
        // }



        // Instant Platform
        if (
            item.getType() == Material.STONE_PRESSURE_PLATE &&
            item.getItemMeta().hasDisplayName() && 
            item.getItemMeta().getDisplayName().equals("Instant Platform")
        ) {
            RayTraceResult rtr = p.rayTraceBlocks(5);
            if (rtr == null) return;
            Vector pos = rtr.getHitPosition();
            MinigameUtils.clearRegion(
                p.getWorld(), Material.STONE, 
                pos.getBlockX() - 6, pos.getBlockY() - 1, pos.getBlockZ() - 6, 
                pos.getBlockX() + 7, pos.getBlockY(), pos.getBlockZ() + 7
            );
            item.setAmount(item.getAmount() - 1);
        }




        // Instant Bridge
        if (
            item.getType() == Material.BRICK &&
            item.getItemMeta().hasDisplayName() && 
            item.getItemMeta().getDisplayName().equals("Instant Bridge")
        ) {
            RayTraceResult rtr = p.rayTraceBlocks(5);
            if (rtr == null) return;
            Vector pos = rtr.getHitPosition();
            double direction = p.getLocation().getYaw() / 180 * Math.PI + Math.PI / 2;
            int y = pos.getBlockY() - 1;
            for (int i = 0; i < 30; i++) {
                int x = (int)(pos.getX() + i * Math.cos(direction));
                int z = (int)(pos.getZ() + i * Math.sin(direction));
                Block b = p.getWorld().getBlockAt(x,y,z);
                b.setType(Material.STONE);
            }
            item.setAmount(item.getAmount() - 1);
        }




        // Instant Wall
        if (
            item.getType() == Material.STONE_BRICK_WALL &&
            item.getItemMeta().hasDisplayName() && 
            item.getItemMeta().getDisplayName().equals("Instant Wall")
        ) {
            RayTraceResult rtr = p.rayTraceBlocks(5);
            if (rtr == null) return;
            Vector pos = rtr.getHitPosition();
            double direction = p.getLocation().getYaw() / 180 * Math.PI;
            for (int i = -1; i < 10; i++) {
                for (int j = -5; j < 6; j++) {
                    int x = (int)(pos.getX() + j * Math.cos(direction));
                    int y = pos.getBlockY() + i;
                    int z = (int)(pos.getZ() + j * Math.sin(direction));
                    Block b = p.getWorld().getBlockAt(x,y,z);
                    b.setType(Material.STONE);
                }
            }
            item.setAmount(item.getAmount() - 1);
        }




        // THERMONUCLEAR BOMB
        if (
            item.getType() == Material.TNT &&
            item.getItemMeta().hasDisplayName() && 
            item.getItemMeta().getDisplayName().equals("THERMONUCLEAR BOMB")
        ) {
            RayTraceResult rtr = p.rayTraceBlocks(5);
            if (rtr == null) return;
            Vector pos = rtr.getHitPosition();
            MinigameUtils.clearRegion(
                p.getWorld(), Material.TNT, 
                pos.getBlockX() - 2, pos.getBlockY() - 5, pos.getBlockZ() - 2, 
                pos.getBlockX() + 3, pos.getBlockY() - 0, pos.getBlockZ() + 3
            );
            p.getWorld().spawnEntity(
                new Location(p.getWorld(), pos.getX(), pos.getY() + 1, pos.getZ()),
                EntityType.PRIMED_TNT
            );
            item.setAmount(item.getAmount() - 1);
        }





        // THERMONUCLEAR BOMB II
        if (
            item.getType() == Material.TNT &&
            item.getItemMeta().hasDisplayName() && 
            item.getItemMeta().getDisplayName().equals("THERMONUCLEAR BOMB II")
        ) {
            RayTraceResult rtr = p.rayTraceBlocks(5);
            if (rtr == null) return;
            Vector pos = rtr.getHitPosition();
            MinigameUtils.clearRegion(
                p.getWorld(), Material.TNT, 
                pos.getBlockX() - 4, pos.getBlockY() - 9, pos.getBlockZ() - 4, 
                pos.getBlockX() + 5, pos.getBlockY() - 0, pos.getBlockZ() + 5
            );
            p.getWorld().spawnEntity(
                new Location(p.getWorld(), pos.getX(), pos.getY() + 1, pos.getZ()),
                EntityType.PRIMED_TNT
            );
            item.setAmount(item.getAmount() - 1);
        }




        // Fire Ball
        if (
            item.getType() == Material.FIRE_CHARGE &&
            item.getItemMeta().hasDisplayName() && 
            item.getItemMeta().getDisplayName().equals("Fire Ball")
        ) {
            Vector pos = p.getLocation().toVector();
            pos = pos.add(p.getLocation().getDirection());
            Fireball fireball =  (Fireball)p.getWorld().spawnEntity(
                new Location(p.getWorld(), pos.getX(), pos.getY() + 1.5, pos.getZ()),
                EntityType.FIREBALL
            );
            fireball.setIsIncendiary(true);
            fireball.setDirection(p.getLocation().getDirection());
            fireball.setBounce(true);
            fireball.setYield(3.2f);
           
            item.setAmount(item.getAmount() - 1);
        }



        // ANVIL RAIN
        if (
            item.getType() == Material.ANVIL &&
            item.getItemMeta().hasDisplayName() && 
            item.getItemMeta().getDisplayName().equals("ANVIL RAIN")
        ) {

            RayTraceResult rtr = p.rayTraceBlocks(5);
            if (rtr == null) return;
            Vector pos = rtr.getHitPosition();
            MinigameUtils.clearRegion(p.getWorld(), Material.ANVIL, 
                (int)pos.getX() -6, Math.min((int)pos.getY() + 30, 319), (int)pos.getZ() -6, 
                (int)pos.getX()+7, Math.min((int)pos.getY()+31, 320), (int)pos.getZ()+ 7);
            // for (int z = -6; z < 7; z++) {
            //     for (int x = -6; x < 7; x++) {
            //         FallingBlock fb = p.getWorld().spawnFallingBlock(
            //             new Location(p.getWorld(), pos.getX() + x, pos.getY() + 30, pos.getZ() + z),
            //             Bukkit.createBlockData(Material.ANVIL)
            //         );
            //         fb.setHurtEntities(true);
            //     }
            // }
           
            item.setAmount(item.getAmount() - 1);
        }



        // FIRE RAIN
        if (
            item.getType() == Material.BLAZE_POWDER &&
            item.getItemMeta().hasDisplayName() && 
            item.getItemMeta().getDisplayName().equals("FIRE RAIN")
        ) {

            RayTraceResult rtr = p.rayTraceBlocks(5);
            if (rtr == null) return;
            Vector pos = rtr.getHitPosition();
            for (int z = -9; z < 10; z++) {
                for (int x = -9; x < 10; x++) {
                    p.getWorld().spawnFallingBlock(
                        new Location(p.getWorld(), pos.getX() + x, pos.getY() + 30, pos.getZ() + z),
                        Bukkit.createBlockData(Material.FIRE)
                    );
                }
            }
           
            item.setAmount(item.getAmount() - 1);
        }
    }
}