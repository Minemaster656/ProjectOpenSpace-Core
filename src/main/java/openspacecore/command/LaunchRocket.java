package openspacecore.command;

import openspacecore.Main;
import openspacecore.rocket.RocketLaunch;
import openspacecore.rocket.RocketUtils;
import openspacecore.rocket.RocketValidation;
import openspacecore.stellar.StellarObject;
import openspacecore.util.Utils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class LaunchRocket implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player player))
            return true;

        World world = player.getWorld();
        Block core = world.getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY() - 1, player.getLocation().getBlockZ());
        if (core.getType() != Material.DISPENSER) {
            commandSender.sendMessage("§7§oI need to stand on a dispenser in rocket!");
            return true;
        }

        if(RocketValidation.validateRocketCore(commandSender, core)) return true;

        if (strings.length == 0) {
            commandSender.sendMessage("[RCPU] §cPlanet expected, got nothing!");
            return true;
        }

        String targetQuery = strings[0];
        World target = Utils.getWorld(targetQuery);
        StellarObject targetStellar = Utils.getStellar(targetQuery);
        StellarObject currentStellar = Utils.getStellarFromWorld(world);

        if (currentStellar == null) {
            commandSender.sendMessage("[RCPU] §cUNABLE TO PINPOINT CURRENT LOCATION!");
            commandSender.sendMessage("[RCPU] §cUNRECOVERABLE ERROR. SYSTEM HALTED.");
            return true;
        }

        if (target == null || targetStellar == null) {
            commandSender.sendMessage("[RCPU] §cTarget " + targetQuery + " does not exist!");
            commandSender.sendMessage("§6§oStellar objects that I know about:");
            Utils.printStellars(commandSender);
            return true;
        }

        boolean confirming = strings.length > 1 && strings[1].equalsIgnoreCase("confirm");

        commandSender.sendMessage("[RCPU] §6Assembling the rocket...");
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 5, 1);

        int[] sizes = RocketValidation.validateRocketSize(commandSender, world, player);

        if (sizes.length == 1) {
            commandSender.sendMessage("[RCPU] §4§lAssembly failed!§r Fix assembling errors above and try again.");
            return true;
        }
        int top_y = sizes[0],
                down_y = sizes[1],
                max_x = sizes[2],
                min_x = sizes[3],
                max_z = sizes[4],
                min_z = sizes[5];
        commandSender.sendMessage("[RCPU] §a§lRocket assembled, running pre-launch checks...");

        int need_fuel = Math.abs(currentStellar.getOrbit() - targetStellar.getOrbit()) / 10;
        int fuel_y = RocketValidation.validateRocketFuel(commandSender, player, down_y, world,
                need_fuel);
        if (fuel_y == -100) return true;
        Block[][][] rocket = RocketUtils.getRocketBlocks(top_y, down_y, max_x, min_x, max_z, min_z, world);
        if (RocketValidation.validateRocketStands(commandSender, down_y, fuel_y, world, min_x, min_z, max_x, max_z))
            return true;
        if (RocketValidation.validateRocketIntegrity(commandSender, rocket, down_y, fuel_y))
            return true;


        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 5, 1);
        commandSender.sendMessage("[RCPU] §a§lAll checks passed!");
        commandSender.sendMessage("[RCPU] §6Rocket ready for launch!");
        if (!confirming) {
            commandSender.sendMessage("[RCPU] §aAll good!§r You can launch now. " +
                    "Append \"§6confirm§r\" at the end to proceed with launch");
            commandSender.sendMessage("[RCPU] §eRocket will be landed on random place on the planet. " +
                    "In case if place for landing would not be found, you will not " +
                    "leave current planet, moon, or orbit.");
            return true;
        }
        int size = max_x - min_x + 1;
        int[] found = targetStellar.findLandingLocation(core, size, target);

        if (found == null) {
            commandSender.sendMessage("[RCPU] §6§lLocation for safe landing not found... try again?");
            return true;
        }
        int rx = found[0], ry = found[1], rz = found[2];
        commandSender.sendMessage("[RCPU] §a§lFound landing location! §r" + rx + " " + ry + " " + rz);

        Block container = world.getBlockAt(player.getLocation().getBlockX(), fuel_y, player.getLocation().getBlockZ());
        Inventory fuel_container = ((InventoryHolder) (container.getState())).getInventory();

        ItemStack[] contents = fuel_container.getContents();
        int target_fuel = RocketValidation.getFuelLevel(fuel_container) - need_fuel;
        fuel_depleter: for (ItemStack item : contents) {
            if (item == null) continue;
            int amount = item.getAmount();
            for (int i = 0; i < amount; i++) {
                if (RocketValidation.getFuelLevel(fuel_container) < target_fuel) break fuel_depleter;
                item.setAmount(item.getAmount()-1);
            }
        }

        commandSender.sendMessage("[RCPU] Launching in 10 seconds.");
        for (int i = 1; i < 10; i++) {
            int li = i;
            Main.plugin.getServer().getScheduler().runTaskLater(Main.plugin, () -> commandSender.sendMessage("[RCPU] " + (10 - li) + "..."), 20 * i);
        }

        final int lrx = rx, lry = ry, lrz = rz;
        Main.plugin.getServer().getScheduler().runTaskLater(Main.plugin, () -> {
            World space = Utils.getWorldFromKey("openspace:space");
            assert space != null;

            RocketLaunch.launch(world, space, min_x + (size >> 1), min_z + (size >> 1), down_y, size, rocket);

            int travelTime = 25; // in seconds, not less than 10

            for (int i = 1; i < 10; i++) {
                int li = i;
                Main.plugin.getServer().getScheduler().runTaskLater(Main.plugin, () -> commandSender.sendMessage("[RCPU] " + (10 - li) + "..."), (20 * (travelTime - 10)) + (20 * i));
            }

            commandSender.sendMessage("[RCPU] Estimated landing time: " + travelTime + " seconds.");
            Main.plugin.getServer().getScheduler().runTaskLater(Main.plugin, () -> {

                final Block[][][] lrocket = RocketUtils.getRocketBlocks(top_y, down_y, max_x, min_x, max_z, min_z, space);

                RocketLaunch.launch(space, target,
                        lrx, lrz, lry, size, lrocket);
            }, 20 * travelTime);
        }, 20 * 10);

        return true;
    }
}
