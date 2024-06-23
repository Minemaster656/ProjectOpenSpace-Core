package openspacecore.command;

import openspacecore.Main;
import openspacecore.rocket.RocketValidation;
import openspacecore.rocket.RocketUtils;
import openspacecore.util.Utils;

import org.bukkit.Location;
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

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

public class LaunchRocket implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player player))
            return true;

        if (strings.length == 0) {
            commandSender.sendMessage("§4§lПожалуйста, укажите планету!");
            return true;
        }

        String planet = strings[0];
        World targetPlanet = Utils.getPlanet(planet);
        //TODO: подгрузка миров из конфига
        //TODO: проверка на существование планеты
        if (targetPlanet == null) {
            commandSender.sendMessage("§cПланета " + planet + " не существует!");
            commandSender.sendMessage("§6Доступные планеты:");
            for (World w : Main.plugin.getServer().getWorlds())
                commandSender.sendMessage(" - "+w.getKey().toString());
            return true;
        }

        World world = player.getWorld();
        Block core = world.getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY() - 1, player.getLocation().getBlockZ());
        if (core.getType() != Material.DISPENSER) {
            commandSender.sendMessage("§4§lВстаньте на раздатчик в кабине ракеты!");
            return true;
        }

        boolean confirming = strings.length > 1 && strings[1].equalsIgnoreCase("confirm");

        commandSender.sendMessage("§6§lCборка ракеты...");
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 5, 1);

        Inventory core_inv;
        if (core.getState() instanceof InventoryHolder) {
            core_inv = ((InventoryHolder) (core.getState())).getInventory();
        } else return true;

        // ItemStack rocket_core_itemstack = core_inv.getItem(4);
        //TODO: сделать чекер ядра
        if (false) {
            commandSender.sendMessage("В центральном слоте раздатчика должно лежать ядро ракеты!");
            return true;
        }

        boolean failed = false;
        int[] sizes = RocketValidation.validateRocketSize(commandSender, world, player);

        if(sizes.length == 1) {
            commandSender.sendMessage("§4§lСборка провалена! Устраните выявленные ошибки и повторите снова.");
            return true;
        }
        int top_y = sizes[0],
            down_y = sizes[1],
            max_x = sizes[2],
            min_x = sizes[3],
            max_z = sizes[4],
            min_z = sizes[5];
        commandSender.sendMessage("§6§lРакета собрана, проверка компонентов...");

        int fuel_y = RocketValidation.validateRocketFuel(commandSender, player, down_y, world);
        if (fuel_y == -100) return true;


        Block[][][] rocket = RocketUtils.getRocketBlocks(top_y, down_y, max_x, min_x, max_z, min_z, world);

        if (RocketValidation.validateRocketStands(commandSender, down_y, fuel_y, world, min_x, min_z, max_x, max_z)) return true;


        if (RocketValidation.validateRocketIntegrity(commandSender, rocket, down_y, fuel_y))
            return true;


        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 5, 1);
        if (!confirming) {
            commandSender.sendMessage("§6§lПроверки пройдены успешно!");
            commandSender.sendMessage("§6Ракета готова к запуску!");
            commandSender.sendMessage("§aРакета собрана успешно! Для запуска пропишите эту же команду, но добавьте в аргументах (через пробел) §6confirm");
            commandSender.sendMessage("§eРакета будет высажена в случайном месте на планете, в случае, если такого места не будет найдено, вы останетесь на этой планете.\n" +
                    "Для высадки в конкретном месте, возьмите в руку посадочный лист!");
            return true;
        }

        commandSender.sendMessage("§6§lПроверки пройдены успешно!");
        commandSender.sendMessage("§6Ракета готова к запуску!");

        int rocket_x = core.getY(), rocket_z = core.getZ();
        int size = min_x - max_x;

        int rx = -1;
        int rz = -1;
        int ry = -1;
        boolean found = false;
        random_location_find: for (int i = 0; i < 10; i++) {
            int shift_x = Utils.randomRangeRandom(-2000, 2000),
                shift_z = Utils.randomRangeRandom(-2000, 2000);
            rx = rocket_x + shift_x;
            rz = rocket_z + shift_z;
            int highest_y = targetPlanet.getHighestBlockAt(rx, rz).getY();
            if (highest_y > 280) continue;
            for(int x = -size >> 1; x < size >> 1; x++) {
                for(int z = -size >> 1; z < size >> 1; z++) {
                    int highest_y_here = targetPlanet.getHighestBlockAt(rx+x, rz+z).getY();
                    if (abs(highest_y_here - highest_y) > 2) continue random_location_find;
                }
            }
            found = true;
            ry = highest_y;
            break;
        }
        if (!found) {
            commandSender.sendMessage("§6§lЛокация для безопасной высадки не найдена... попробуйте ещё раз?");
            return true;
        }
        commandSender.sendMessage("§6§lНашли локацию для высадки! "+rx+" "+ry+" "+rz);
        rx -= size >> 1;
        rz -= size >> 1;

        for(int x = rx; x < rx+size; x++) {
            int local_x = x - rx;
            for(int z = rz; z < rz+size; z++) {
                int local_z = z - rz;
                for(int y = ry; y < ry + rocket.length; y++) {
                    int local_y = y - ry;
                    Location curloc = new Location(world, x, y, z);
                    Location newloc = new Location(targetPlanet, x, y, z);
                    Block block = rocket[local_y][local_x][local_z];
                    newloc.getBlock().setType(block.getType());
                    curloc.getBlock().setType(Material.AIR);
                }
            }
        }
        int local_cx = rx - core.getX(), local_cy = ry - core.getY(), local_cz = rz - core.getZ();
        player.teleport(new Location(targetPlanet, rx+local_cx, ry+local_cy, rz+local_cz));

// ЭТО ВСЕ НАДО ПЕРЕПИСАТЬ!
// Жрет много ресурсов, вынести в отдельный поток?...
/*
        int rshiftx = core.getX() - min_x;
        int rshiftz = core.getZ() - min_z;

        for (int i = 0; i < 50; i++) { //TODO: конфиг количества попыток
            //TODO: проверка на нерушимые блоки
            //TODO: перенос магнетита
            //TODO: стоимость полёта
            commandSender.sendMessage("§6Попытка No" + (i + 1));
            int rx = Utils.randomRangeRandom(-2000, 2000);
            int rz = Utils.randomRangeRandom(-2000, 2000);

            int ty = Utils.getHighestY(targetPlanet, rx, rz);
            if (ty > 280) {
                commandSender.sendMessage("§cНе удалось разместить ракету: слишком высокая точка высадки");
                continue;
            }
            Location loc = new Location(targetPlanet, rx, ty, rz);
            //Проверка области над стойками
            boolean isCheckFailed = false;

            for (int x = loc.getBlockX() - rshiftx; x < loc.getBlockX() + (rocket[0].length - rshiftx); x++) {
                for (int z = loc.getBlockZ() - rshiftz; z < loc.getBlockZ() + (rocket[0][0].length - rshiftz); z++) {
                    for (int y = loc.getBlockY() + (fuel_y - down_y - 1); y <= loc.getBlockY() + (top_y - down_y); y++) {
                        if (!isAir(world.getBlockAt(x, y, z))) {
                            isCheckFailed = true;
//                            commandSender.sendMessage(world.getBlockAt(x, y, z).getType().toString() + " X: " + x + " Y: " + y + " Z: " + z);
                            commandSender.sendMessage("§cНе удалось разместить ракету");
                            break;
                        }
                    }
                    if (isCheckFailed) break;
                }
                if (isCheckFailed) break;
            }
            if (isCheckFailed) continue;
            Block[][][] trocket = new Block[rocket.length][rocket[0].length][rocket[0][0].length];
            ArrayList<Block> trocket_blocks = new ArrayList<>();
//            ArrayList<Chunk> chunks = new ArrayList<>();

            commandSender.sendMessage("§6Нашли локацию, не перемещаемся из-за лагов. "+rx+" "+ty+" "+rz);

            for (int y = rocket.length-1; y >=0 ; y--) {
                for (int x = 0; x < rocket[0].length; x++) {
                    for (int z = 0; z < rocket[0][0].length; z++) {
//                        if (!(loc.getChunk().isLoaded()))
//                        {
//                            loc.getChunk().load();
//                            chunks.add(loc.getChunk());
//                        }

                        Block tblock = targetPlanet.getBlockAt(loc.getBlockX() + (x - rshiftx), loc.getBlockY() + ( y+1), loc.getBlockZ() + (z - rshiftz));
                        commandSender.sendMessage("WORLD: " + targetPlanet.getName() + " TYPE: " + tblock.getType().toString() + " X: " + tblock.getX() + " Y: " + tblock.getY() + " Z: " + tblock.getZ());

                        tblock.breakNaturally();

                        Material mat = rocket[y][x][z].getType();
                        BlockData data = rocket[y][x][z].getBlockData();
//                        BlockState state = tblock.getState().copy();

                        tblock.setType(rocket[y][x][z].getType(), false);
                        tblock.setBlockData(data);

//                        tblock.setType(Material.COAL_ORE);
//                        tblock.setBlockData(rocket[y][x][z].getBlockData());
                        trocket[y][x][z] = tblock;
                        trocket_blocks.add(tblock);
                        rocket[y][x][z].setType(Material.AIR, false);
                        commandSender.sendMessage("x: " + tblock.getX() + " y: " + tblock.getY() + " z: " + tblock.getZ());
                    }
                }
            }
            core.getWorld().getBlockAt(core.getX(), down_y, core.getZ()).setType(Material.AIR, false);

            targetPlanet.getBlockAt(rx, ty, rz).breakNaturally();
            targetPlanet.getBlockAt(rx, ty, rz).setType(Material.LODESTONE, false);
//            for (Chunk chunk: chunks){
//                chunk.unload();
//            }
//            for (int j = 0; j < trocket.length; j++) {
//                for (int k = 0; k < trocket[0].length; k++) {
//                    for (int l = 0; l < trocket[0][0].length; l++) {
//                        trocket[j][k][l].setType(rocket[j][k][l].getType());
//                    }
//                }
//            }
            //TODO: нормальное перемещение сущностей
            player.teleport(loc);
            player.playSound(loc, Sound.BLOCK_END_PORTAL_SPAWN, 1, 1);
            break;
        }

*/
        return true;
    }
}
