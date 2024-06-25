package openspacecore.command;

import openspacecore.Main;
import openspacecore.rocket.RocketLaunch;
import openspacecore.rocket.RocketUtils;
import openspacecore.rocket.RocketValidation;
import openspacecore.util.Utils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static java.lang.Math.abs;

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

        String planet = strings[0];
        World targetPlanet = Utils.getPlanet(planet);
        //TODO: подгрузка миров из конфига
        //TODO: проверка на существование планеты
        if (targetPlanet == null) {
            commandSender.sendMessage("[RCPU] §cPlanet " + planet + " does not exist!");
            commandSender.sendMessage("§6§oStellar objects that I know about:");
            Utils.printExistingStellars(commandSender, Main.stellars.entrySet());
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

        int fuel_y = RocketValidation.validateRocketFuel(commandSender, player, down_y, world);
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
                    "In case if place for landing would not be found, you will be " +
                    "transported back to your original leaving location.");
            return true;
        }

        int rocket_x = core.getY(), rocket_z = core.getZ();
        int size = max_x - min_x + 1;

        int rx = -1;
        int rz = -1;
        boolean found = false;
        int ry = -1;
        random_location_find:
        for (int i = 0; i < 10; i++) {
            int shift_x = Utils.randomRangeRandom(-2000, 2000),
                shift_z = Utils.randomRangeRandom(-2000, 2000);
            rx = rocket_x + shift_x;
            rz = rocket_z + shift_z;
            Block highest_block = targetPlanet.getHighestBlockAt(rx, rz);
            int highest_y = highest_block.getY();
            if (highest_y > 280) continue;
            if (highest_y < 5) continue;
            if (highest_block.getType() == Material.LAVA ||
                highest_block.getType() == Material.WATER) continue;
            for (int x = -size >> 1; x < size >> 1; x++) {
                for (int z = -size >> 1; z < size >> 1; z++) {
                    int highest_y_here = targetPlanet.getHighestBlockAt(rx + x, rz + z).getY();
                    if (abs(highest_y_here - highest_y) > 2) continue random_location_find;
                }
            }
            found = true;
            ry = highest_y;
            break;
        }
        if (!found) {
            commandSender.sendMessage("[RCPU] §6§lLocation for safe landing not found... try again?");
            return true;
        }
        commandSender.sendMessage("[RCPU] §a§lFound landing location! §r" + rx + " " + ry + " " + rz);

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

                RocketLaunch.launch(space, targetPlanet,
                        lrx, lrz, lry, size, lrocket);
            }, 20 * travelTime);
        }, 20 * 10);

        return true;
    }
}
