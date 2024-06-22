package command;

import com.gdt.pospcore.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import util.Utils;

import java.util.ArrayList;

public class LaunchRocket implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return true;
        boolean confirm = false;
        String planet = "";
//        int confirmArgID = 0;
//        for (int i = 0; i < strings.length; i++) {
//            if (strings[i].equalsIgnoreCase("confirm")) {
//                confirm = true;
//                confirmArgID = i;
//                break;
//            }
//        }
//        if (confirmArgID == 0) planet = strings[0];
//
//        else {
//            if (strings.length > 1)
//            planet = strings[1];
//            else {
//                commandSender.sendMessage("§4§lПожалуйста, укажите планету!");
//                return true;
//            }
//        }
        if (strings.length > 0) planet = strings[0];
        if (strings.length > 1) confirm = strings[1].equalsIgnoreCase("confirm");

        if (planet.isEmpty()) {
            commandSender.sendMessage("§4§lПожалуйста, укажите планету!");
            return true;
        }
        Player player = (Player) commandSender;

        commandSender.sendMessage("§6§lЗапуск рассчёта целостности ракеты...");
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 5, 1);
        Inventory core_inv;
        World world = player.getWorld();
        Block core = world.getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY() - 1, player.getLocation().getBlockZ());
        if (core.getType() != Material.DISPENSER) {
            commandSender.sendMessage("Встаньте РОВНО НА раздатчик в КАБИНЕ ракеты!");
            return true;
        }

        if (core.getState() instanceof InventoryHolder) {
            core_inv = ((InventoryHolder) (core.getState())).getInventory();
        } else return true;

        ItemStack rocket_core_itemstack = core_inv.getItem(4);
        //TODO: сделать чекер ядра
        if (!true) {
            commandSender.sendMessage("В центральном слоте раздатчика должно лежать ядро ракеты!");
            return true;
        }

        //if (ProjectClosedSpace.debug) commandSender.sendMessage("§4§lЗапуск рассчёта y");
        int top_y = -10000;
        int down_y = -10000;
        for (int i = player.getLocation().getBlockY(); i < 321; i++) {
            if (world.getBlockAt(player.getLocation().getBlockX(), i, player.getLocation().getBlockZ()).getType() == Material.LODESTONE) {
                top_y = i;
                break;
            }
        }
        for (int i = player.getLocation().getBlockY(); i >= -64; i--) {
            if (world.getBlockAt(player.getLocation().getBlockX(), i, player.getLocation().getBlockZ()).getType() == Material.LODESTONE) {
                down_y = i;
                break;
            }
        }
        if (top_y <= -10000) {
            commandSender.sendMessage("На макушке ракеты ровно над раздатчиком должен стоять магнетит!");
            return true;
        }
        if (down_y <= -10000) {
            commandSender.sendMessage("Под ракетой ровно под раздатчиком должен стоять магнетит!");
            return true;
        }
        if (top_y - down_y > 33) {
            commandSender.sendMessage("Ракета должна быть не более 32 блоков высотой!");
            return true;

        }
        //if (ProjectClosedSpace.debug) commandSender.sendMessage("§2§lРассчёт y закончен.");
        //if (ProjectClosedSpace.debug) commandSender.sendMessage("§4§lЗапуск рассчёта x");
        int min_x = -2147483647;
        int max_x = -2147483647;
        for (int x = player.getLocation().getBlockX() ; x <= player.getLocation().getBlockX()+5; x++) {
            if (world.getBlockAt(x, player.getLocation().getBlockY() - 1, player.getLocation().getBlockZ()).getType() == Material.LODESTONE) {
                max_x = x;
                break;
            }
        }
        for (int x = player.getLocation().getBlockX(); x >= player.getLocation().getBlockX()-5; x--) {
            if (world.getBlockAt(x, player.getLocation().getBlockY() - 1, player.getLocation().getBlockZ()).getType() == Material.LODESTONE) {
                min_x = x;
                break;
            }
        }

        if (max_x == -2147483647 || min_x == -2147483647) {
            commandSender.sendMessage("По бокам ракеты в стенах на уровне раздатчика должны стоять блоки магнетита! Ось x не подходит по этому требованию!");
            return true;
        }
        //if (ProjectClosedSpace.debug) commandSender.sendMessage("§2§lРассчёт x закончен.");
        //if (ProjectClosedSpace.debug) commandSender.sendMessage("§4§lЗапуск рассчёта z");
        int min_z = -2147483647;
        int max_z = -2147483647;

        for (int z = player.getLocation().getBlockZ() + 5; z >= player.getLocation().getBlockZ(); z--) {
            if (world.getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY() - 1, z).getType() == Material.LODESTONE) {
                max_z = z;
                break;
            }
        }
        for (int z = player.getLocation().getBlockZ() - 5; z <= player.getLocation().getBlockZ(); z++) {
            if (world.getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY() - 1, z).getType() == Material.LODESTONE) {
                min_z = z;
                break;
            }
        }
        if (max_z == -2147483647 || min_z == -2147483647) {
            commandSender.sendMessage("По бокам ракеты в стенах на уровне раздатчика должны стоять блоки магнетита! Ось z не подходит под эти требования!");
            return true;
        }

        if (player.getLocation().getBlockX()-min_x != max_x - player.getLocation().getBlockX() || player.getLocation().getBlockZ()-min_z != max_z - player.getLocation().getBlockZ()) {
            commandSender.sendMessage("Ядро ракеты должно быть ровно по середине");
            return true;
        }
        if (max_x-min_x != max_z-min_z) {
            commandSender.sendMessage("Ракета должна быть квадратной, а не прямоугольной!");
            return true;
        }

        //if (ProjectClosedSpace.debug) commandSender.sendMessage("§2§lРассчёт z закончен.");
        //if (ProjectClosedSpace.debug) commandSender.sendMessage("§4§lЗапуск рассчёта топливного бака...");
        Inventory fuel_container = null;
        int fuel_y = -100;
        for (int y = down_y; y < player.getLocation().getBlockY(); y++) {
            if (world.getBlockAt(player.getLocation().getBlockX(), y, player.getLocation().getBlockZ()).getState() instanceof InventoryHolder) {
                fuel_container = ((InventoryHolder) (world.getBlockAt(player.getLocation().getBlockX(), y, player.getLocation().getBlockZ()).getState())).getInventory();
                fuel_y = y;
                break;
            }
        }
        if (fuel_container == null || fuel_y == -100) {
            commandSender.sendMessage("Внизу ракеты должно стоять хранилище для топлива (В ПОЛУ)!");
            return true;
        }
        //if (ProjectClosedSpace.debug) commandSender.sendMessage("§2§lРассчёт топливного бака закончен.");
        //if (ProjectClosedSpace.debug) commandSender.sendMessage("§4§lЗапуск построения ракеты...");
        Block[][][] rocket = new Block[top_y - down_y][max_x - min_x + 1][max_z - min_z + 1];
        ArrayList<Block> rocket_blocks = new ArrayList<>();
        //if (ProjectClosedSpace.debug) commandSender.sendMessage("§e§lИнициализация массивов завершена. Запуск сканера...");

        for (int y = down_y + 1; y <= top_y; y++) {
            for (int x = min_x; x <= max_x; x++) {
                for (int z = min_z; z <= max_z; z++) {
                    rocket[y - down_y - 1][x - min_x][z - min_z] = world.getBlockAt(x, y, z);
                    rocket_blocks.add(world.getBlockAt(x, y, z));
                }
            }
        }

        //if (ProjectClosedSpace.debug) commandSender.sendMessage("§2§lПостроение ракеты закончено.");
        //if (ProjectClosedSpace.debug) commandSender.sendMessage("§4§lЗапуск проверки стоек ракеты...");
        for (int y = down_y + 1; y < fuel_y; y++) {
            if (!(validateWallBlock(world.getBlockAt(min_x, y, min_z)) && validateWallBlock(world.getBlockAt(max_x, y, min_z)) &&
                    validateWallBlock(world.getBlockAt(min_x, y, max_z)) && validateWallBlock(world.getBlockAt(max_x, y, max_z)))
            ) {

                commandSender.sendMessage("По углам ракеты до слоя с хранилищем топлива должны быть ножки из блоков корупса");
                return true;
            }
        }
        //if (ProjectClosedSpace.debug) commandSender.sendMessage("§2§lПроверка стоек ракеты закончена.");
        //if (ProjectClosedSpace.debug) commandSender.sendMessage("§4§lЗапуск рассчёта корпуса...");


        int rocket_top_height = (int) (double) ((max_x - min_x - 1) / 2);
        //if (ProjectClosedSpace.debug) commandSender.sendMessage("§e§lВысота конуса ракеты - " + rocket_top_height);
        int rocket_walls_iron_blocks_count = 0;
        int rocket_walls_quartz_blocks_count = 0;
//        int rocket_walls_copper_blocks_count = 0;
        int rocket_walls_total_blocks_count = 0;
        int rocket_walls_glass_blocks_count = 0;
        int rocket_walls_lamps_blocks_count = 0;
        int rocket_walls_redstone_blocks_count = 0;
        int rocket_walls_glowstone_blocks_count = 0;
        int rocket_walls_doors_blocks_count = 0;
        int rocket_walls_lodestone_blocks_count = 0;


        for (int y = 0; y < rocket.length - rocket_top_height; y++) {
            for (int x = 0; x < rocket[0].length; x++) {
                for (int z = 0; z < rocket[0][0].length; z++) {
                    if (y == 0 || y == rocket.length - rocket_top_height - 1 || x == 0 || x == rocket[0].length - 1 || z == 0 || z == rocket[0][0].length - 1) {
                        if (isAir(rocket[y][x][z]) && !(rocket[y][x][z].getY() < fuel_y)) {
                            if (y != fuel_y & x != player.getLocation().getBlockX() & z != player.getLocation().getBlockZ()) {


                                commandSender.sendMessage("В стенах, потолке и полу ракеты должны быть подходящие блоки! (блоки корпуса, двери, стекло, освещение)");
                                commandSender.sendMessage("Координаты неверного блока: " + rocket[y][x][z].getX() + " " + rocket[y][x][z].getY() + " " + rocket[y][x][z].getZ());
                                return true;
                            } else {
                                rocket_walls_total_blocks_count++;
                                if (rocket[y][x][z].getType() == Material.IRON_BLOCK) {
                                    rocket_walls_iron_blocks_count++;
                                } else if (rocket[y][x][z].getType() == Material.QUARTZ_BLOCK || rocket[y][x][z].getType() == Material.SMOOTH_QUARTZ) {
                                    rocket_walls_quartz_blocks_count++;
//                                } else if (rocket[y][x][z].getType() == Material.COPPER_BLOCK ||
//                                        rocket[y][x][z].getType() == Material.CUT_COPPER||
//                                        rocket[y][x][z].getType() == Material.WAXED_CUT_COPPER ||
//                                        rocket[y][x][z].getType() == Material.WEATHERED_COPPER ||
//
//
//
//                                ) {
//                                    rocket_walls_grass_blocks_count++;
                                } else if (rocket[y][x][z].getType() == Material.GLOWSTONE) {
                                    rocket_walls_glowstone_blocks_count++;
                                } else if (rocket[y][x][z].getType() == Material.REDSTONE_LAMP) {
                                    rocket_walls_lamps_blocks_count++;
                                } else if (rocket[y][x][z].getType() == Material.GLASS || rocket[y][x][z].getType() == Material.TINTED_GLASS) {
                                    rocket_walls_glass_blocks_count++;
                                } else if (rocket[y][x][z].getType() == Material.REDSTONE_BLOCK) {
                                    rocket_walls_redstone_blocks_count++;
                                } else if (rocket[y][x][z].getType() == Material.WARPED_DOOR || rocket[y][x][z].getType() == Material.CRIMSON_DOOR || rocket[y][x][z].getType() == Material.IRON_DOOR) {
                                    rocket_walls_doors_blocks_count++;
                                } else if (rocket[y][x][z].getType() == Material.LODESTONE) {
                                    rocket_walls_lodestone_blocks_count++;
                                }
                            }
                        }
                    }
                }
            }
        }
        if ((double) rocket_walls_iron_blocks_count / rocket_walls_total_blocks_count < 0.5) {
            commandSender.sendMessage("В стенах должно быть хотя бы 50% блоков железа!");
            return true;
        }
        //if (ProjectClosedSpace.debug) commandSender.sendMessage("§2§lРассчёт корпуса ракеты закончен!");


        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 5, 1);
        if (!confirm) {
            commandSender.sendMessage("§aРакета собрана успешно! Для запуска пропишите эту же команду, но добавьте в аргументах (через пробел) §6confirm");
            commandSender.sendMessage("§eРакета будет высажена в случайном месте на планете, в случае, если такого места не будет найдено, вы останетесь на этой планете.\n" +
                    "Для высадки в конкретном месте, возьмите в руку посадочный лист!");
            return true;
        } else
            commandSender.sendMessage("§aРакета собрана успешно!");
        commandSender.sendMessage("§4§lЗАПУСК РАКЕТЫ!!! ПЛАНЕТА: " + planet);

        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //Это ИИ придумал написать ^
        // Так, а вообще, пора бы и ракету запустить. Причём по-настоящему!

        World targetPlanet = Main.plugin.getServer().getWorld(planet);
        //TODO: подгрузка миров из конфига
        //TODO: проверка на существование планеты
        //TODO: подгрузка размера мира из конфига и прочей инфы о мире
        if (targetPlanet == null) {
            commandSender.sendMessage("§cПланета " + planet + " не существует!");
            return true;
        }
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
            //TODO: вынести проверку и перемещение ракеты в отдельные функции.
        }


        return true;
    }

    public boolean validateWallBlock(Block block) {
        ArrayList<Material> validWallBlocks = new ArrayList<>();
        validWallBlocks.add(Material.IRON_BLOCK);
        validWallBlocks.add(Material.QUARTZ_BLOCK);
        validWallBlocks.add(Material.SMOOTH_QUARTZ);
        validWallBlocks.add(Material.GLOWSTONE);
        validWallBlocks.add(Material.REDSTONE_LAMP);
        validWallBlocks.add(Material.LODESTONE);
        validWallBlocks.add(Material.GLASS);
        validWallBlocks.add(Material.TINTED_GLASS);
        for (int i = 0; i < validWallBlocks.size(); i++) {
            if (validWallBlocks.get(i) == block.getType()) {
                return true;
            }
        }
        return false;
    }

    public boolean isAir(Block block) {
        return block.getType() == Material.AIR || block.getType() == Material.CAVE_AIR || block.getType() == Material.VOID_AIR;
    }

}
