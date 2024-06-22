package command;

import com.gdt.openspacecore.Main;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import util.Utils;

import java.util.ArrayList;
import java.util.List;

public class LaunchRocket implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player player))
            return true;

        if (strings.length == 0) {
            commandSender.sendMessage("§4§lПожалуйста, укажите планету!");
            return true;
        }

        World world = player.getWorld();
        Block core = world.getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY() - 1, player.getLocation().getBlockZ());
        if (core.getType() != Material.DISPENSER) {
            commandSender.sendMessage("§4§lВстаньте на раздатчик в кабине ракеты!");
            return true;
        }

        String planet = strings[0];
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
        int[] sizes = validateRocketSize(commandSender, world, player);

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

        int fuel_y = validateRocketFuel(commandSender, player, down_y, world);
        if (fuel_y == -100) return true;
        commandSender.sendMessage("[ТОПЛИВО] §6§lОК");

        Block[][][] rocket = getRocketBlocks(top_y, down_y, max_x, min_x, max_z, min_z, world);

        if (validateRocketStands(commandSender, down_y, fuel_y, world, min_x, min_z, max_x, max_z)) return true;
        commandSender.sendMessage("[СТОЙКИ] §6§lОК");

        if (validateRocketIntegrity(commandSender, rocket, down_y, fuel_y))
            return true;
        commandSender.sendMessage("[ГЕРМЕТИЧНОСТЬ] §6§lОК");

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 5, 1);
        if (!confirming) {
            commandSender.sendMessage("§6§lПроверки пройдены успешно!");
            commandSender.sendMessage("§6Ракета готова к запуску!");
            commandSender.sendMessage("§aРакета собрана успешно! Для запуска пропишите эту же команду, но добавьте в аргументах (через пробел) §6confirm");
            commandSender.sendMessage("§eРакета будет высажена в случайном месте на планете, в случае, если такого места не будет найдено, вы останетесь на этой планете.\n" +
                    "Для высадки в конкретном месте, возьмите в руку посадочный лист!");
            return true;
        }

        for (World w : Main.plugin.getServer().getWorlds())
            commandSender.sendMessage(w.getKey().toString());

        commandSender.sendMessage("§6§lПроверки пройдены успешно!");
        commandSender.sendMessage("§6Ракета готова к запуску!");

        World targetPlanet = Utils.getPlanet(planet);
        //TODO: подгрузка миров из конфига
        //TODO: проверка на существование планеты
        //TODO: подгрузка размера мира из конфига и прочей инфы о мире
        if (targetPlanet == null) {
            commandSender.sendMessage("§cПланета " + planet + " не существует!");
            return true;
        }

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
            Chunk chunk = targetPlanet.getChunkAt((int)(rx / 16), (int)(rz / 16), true);
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

    private boolean validateRocketIntegrity(CommandSender commandSender, Block[][][] blocks, int down_y, int fuel_y) {
        int xMax = blocks[0].length;
        int yMax = blocks.length;
        int zMax = blocks[0][0].length;

        List<Block> invalidBlocks = new ArrayList<>();

        int rocket_walls_iron_blocks_count = 0;
        int rocket_walls_total_blocks_count = 0;

        int fuel_local_y = fuel_y - down_y - 2;

        for (int x = 0; x < xMax; x++) {
            for (int z = 0; z < zMax; z++) {
                rocket_walls_total_blocks_count++;
                if (!validateWallBlock(blocks[yMax - 1][x][z])) {
                    invalidBlocks.add(blocks[yMax - 1][x][z]);
                }
                if (blocks[yMax - 1][x][z].getType() == Material.IRON_BLOCK)
                    rocket_walls_iron_blocks_count++;
            }
        }

        for (int x = 0; x < xMax; x++) {
            for (int z = 0; z < zMax; z++) {
                rocket_walls_total_blocks_count++;
                if (!validateWallBlock(blocks[fuel_local_y][x][z])) {
                    invalidBlocks.add(blocks[fuel_local_y][x][z]);
                }
                if (blocks[fuel_local_y][x][z].getType() == Material.IRON_BLOCK)
                    rocket_walls_iron_blocks_count++;
            }
        }

        for (int y = fuel_local_y; y < yMax - 1; y++) {
            for (int x = 0; x < xMax; x++) {
                rocket_walls_total_blocks_count++;
                if (!validateWallBlock(blocks[y][x][0]))
                    invalidBlocks.add(blocks[y][x][0]);
                if (blocks[y][x][0].getType() == Material.IRON_BLOCK)
                    rocket_walls_iron_blocks_count++;
                rocket_walls_total_blocks_count++;
                if (!validateWallBlock(blocks[y][x][zMax - 1]))
                    invalidBlocks.add(blocks[y][x][zMax - 1]);
                if (blocks[y][x][zMax - 1].getType() == Material.IRON_BLOCK)
                    rocket_walls_iron_blocks_count++;
            }
        }

        for (int y = fuel_local_y; y < yMax - 1; y++) {
            for (int z = 0; z < zMax; z++) {
                rocket_walls_total_blocks_count++;
                if (!validateWallBlock(blocks[y][0][z]))
                    invalidBlocks.add(blocks[y][0][z]);
                if (blocks[y][0][z].getType() == Material.IRON_BLOCK)
                    rocket_walls_iron_blocks_count++;
                rocket_walls_total_blocks_count++;
                if (!validateWallBlock(blocks[y][xMax - 1][z]))
                    invalidBlocks.add(blocks[y][xMax - 1][z]);
                if (blocks[y][xMax - 1][z].getType() == Material.IRON_BLOCK)
                    rocket_walls_iron_blocks_count++;
            }
        }

        boolean failed = false;

        if ((double) rocket_walls_iron_blocks_count / rocket_walls_total_blocks_count < 0.5) {
            commandSender.sendMessage("В стенах должно быть хотя бы 50% блоков железа!");
            failed = true;
        }
        if (!invalidBlocks.isEmpty()) {
            commandSender.sendMessage("§4Ракета не герметична!");
            commandSender.sendMessage("§6§lНеверные блоки:");
            for (Block block : invalidBlocks) {
                commandSender.sendMessage(" - " + block.getType().toString() + " на " + block.getX()+" "+block.getY()+" "+block.getZ());
            }
            failed = true;
        }
        if(failed)
            commandSender.sendMessage("[ГЕРМЕТИЧНОСТЬ] §4§lПровал");

        return failed;
    }

    private boolean validateRocketStands(CommandSender commandSender, int down_y, int fuel_y, World world, int min_x, int min_z, int max_x, int max_z) {
        for (int y = down_y + 1; y < fuel_y; y++) {
            if (!(validateWallBlock(world.getBlockAt(min_x, y, min_z)) &&
                    validateWallBlock(world.getBlockAt(max_x, y, min_z)) &&
                    validateWallBlock(world.getBlockAt(min_x, y, max_z)) &&
                    validateWallBlock(world.getBlockAt(max_x, y, max_z)))
            ) {
                commandSender.sendMessage("[СТОЙКИ] §4§lПровал");
                commandSender.sendMessage("§4По углам ракеты до слоя с хранилищем топлива должны быть стойки ракеты из блоков корпуса");
                return true;
            }
        }
        return false;
    }

    private static Block[][][] getRocketBlocks(int top_y, int down_y, int max_x, int min_x, int max_z, int min_z, World world) {
        Block[][][] rocket = new Block[top_y - down_y][max_x - min_x + 1][max_z - min_z + 1];
        for (int y = down_y + 1; y <= top_y; y++) {
            for (int x = min_x; x <= max_x; x++) {
                for (int z = min_z; z <= max_z; z++) {
                    rocket[y - down_y - 1][x - min_x][z - min_z] = world.getBlockAt(x, y, z);
                }
            }
        }
        return rocket;
    }

    private static int validateRocketFuel(CommandSender commandSender, Player player, int down_y, World world) {
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
            commandSender.sendMessage("[ТОПЛИВО] §4§lПровал");
            commandSender.sendMessage("§4В полу ракеты должно стоять хранилище для топлива!");
            return -100;
        }
        return fuel_y;
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

    public int[] validateRocketSize(CommandSender commandSender, World world, Player player) {
        boolean failed = false;

        int top_y  = -2147483647;
        int down_y = -2147483647;
        int min_x  = -2147483647;
        int max_x  = -2147483647;
        int min_z  = -2147483647;
        int max_z  = -2147483647;

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

        if (top_y <= -2147483647) {
            commandSender.sendMessage("§4На макушке ракеты (ровно над раздатчиком) должен стоять магнетит!");
            failed = true;
        }
        if (down_y <= -2147483647) {
            commandSender.sendMessage("§4Под ракетой (ровно под раздатчиком) должен стоять магнетит!");
            failed = true;
        }
        if (top_y - down_y > 33) {
            commandSender.sendMessage("§4Ракета должна быть не более 32 блоков высотой!");
            failed = true;
        }

        if (max_x == -2147483647 || min_x == -2147483647) {
            commandSender.sendMessage("§4По бокам ракеты в стенах на уровне раздатчика должны стоять блоки магнетита! Ось x не подходит по этому требованию!");
            failed = true;
        }
        if (max_z == -2147483647 || min_z == -2147483647) {
            commandSender.sendMessage("§4По бокам ракеты в стенах на уровне раздатчика должны стоять блоки магнетита! Ось z не подходит под эти требования!");
            failed = true;
        }
        if (player.getLocation().getBlockX()-min_x != max_x - player.getLocation().getBlockX() || player.getLocation().getBlockZ()-min_z != max_z - player.getLocation().getBlockZ()) {
            commandSender.sendMessage("§4Ядро ракеты должно быть ровно по середине");
            failed = true;
        }
        if (max_x-min_x != max_z-min_z) {
            commandSender.sendMessage("§4Стороны ракеты по X и Z должны быть равны в длине.");
            failed = true;
        }
        if (failed) return new int[]{-1};
        return new int[]{top_y, down_y, max_x, min_x, max_z, min_z};
    }

    public boolean isAir(Block block) {
        return block.getType() == Material.AIR || block.getType() == Material.CAVE_AIR || block.getType() == Material.VOID_AIR;
    }

}
