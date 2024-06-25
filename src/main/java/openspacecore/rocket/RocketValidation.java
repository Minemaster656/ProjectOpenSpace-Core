package openspacecore.rocket;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RocketValidation {
    public static boolean validateRocketCore(CommandSender commandSender, Block core) {
        Inventory core_inv;
        if (core.getState() instanceof InventoryHolder) {
            core_inv = ((InventoryHolder) (core.getState())).getInventory();
        } else return true;

        ItemStack idx_0 = core_inv.getItem(0);
        if (idx_0 == null) idx_0 = new ItemStack(Material.AIR);
        ItemStack idx_1 = core_inv.getItem(1);
        if (idx_1 == null) idx_1 = new ItemStack(Material.AIR);
        ItemStack idx_2 = core_inv.getItem(2);
        if (idx_2 == null) idx_2 = new ItemStack(Material.AIR);
        ItemStack idx_3 = core_inv.getItem(3);
        if (idx_3 == null) idx_3 = new ItemStack(Material.AIR);
        ItemStack idx_4 = core_inv.getItem(4);
        if (idx_4 == null) idx_4 = new ItemStack(Material.AIR);
        ItemStack idx_5 = core_inv.getItem(5);
        if (idx_5 == null) idx_5 = new ItemStack(Material.AIR);
        ItemStack idx_6 = core_inv.getItem(6);
        if (idx_6 == null) idx_6 = new ItemStack(Material.AIR);
        ItemStack idx_7 = core_inv.getItem(7);
        if (idx_7 == null) idx_7 = new ItemStack(Material.AIR);
        ItemStack idx_8 = core_inv.getItem(8);
        if (idx_8 == null) idx_8 = new ItemStack(Material.AIR);

        if (idx_4.getType() != Material.OBSERVER) {
            if (core_inv.contains(Material.OBSERVER)) {
                commandSender.sendMessage("§7§oI need to put observer in the center of the core!");
                return true;
            }
            commandSender.sendMessage("§7§oIt doesn't even have control circuitry! " +
                    "Observer block should have one sufficient enough...");
            return true;
        }
        if (idx_1.getType() != Material.LEVER) {
            if (core_inv.contains(Material.LEVER)) {
                commandSender.sendMessage("§7§oI need to put lever in top center slot!");
                return true;
            }
            commandSender.sendMessage("§7§oIt doesn't even have a power switch! " +
                    "Lever should be fine...");
            return true;
        }
        if (idx_6.getType() != Material.REDSTONE_LAMP) {
            if (core_inv.contains(Material.REDSTONE_LAMP)) {
                commandSender.sendMessage("§7§oI need to put the lamp in bottom left slot!");
                return true;
            }
            commandSender.sendMessage("§7§oIt doesn't even have a power indicator! " +
                    "Maybe I should connect a redstone lamp in there...");
            return true;
        }
        if (idx_0.getType() != Material.COMPARATOR) {
            if (core_inv.contains(Material.COMPARATOR)) {
                commandSender.sendMessage("§7§oI need to put comparator in top left slot!");
                return true;
            }
            commandSender.sendMessage("§7§oIt didn't start... Looks like I forgot to add a CPU... " +
                    "Comparator should do the work...");
            return true;
        }
        if (idx_2.getType() != Material.REPEATER) {
            if (core_inv.contains(Material.REPEATER)) {
                commandSender.sendMessage("§7§oI need to put repeater in top right slot slot!");
                return true;
            }
            commandSender.sendMessage("§7§oIt made a dim red light... I need a signal amplifier. " +
                    "Repeater should be enough...");
            return true;
        }
        if (idx_3.getType() != Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
            if (core_inv.contains(Material.LIGHT_WEIGHTED_PRESSURE_PLATE)) {
                commandSender.sendMessage("§7§oI need to put the gold plate in middle left slot!");
                return true;
            }
            commandSender.sendMessage("§7§oIt made some random sounds and powered off... I forgot to add conductor. " +
                    "Gold plate should work like one.");
            return true;
        }
        if (idx_5.getType() != Material.NETHERITE_INGOT) {
            if (core_inv.contains(Material.NETHERITE_INGOT)) {
                commandSender.sendMessage("§7§oI need to put netherite ingot in middle right slot slot!");
                return true;
            }
            commandSender.sendMessage("§7§oIt powered off immediately... looks like it overheated. " +
                    "Netherite ingot can go like a radiator.");
            return true;
        }
        if (idx_7.getType() != Material.CALIBRATED_SCULK_SENSOR) {
            if (core_inv.contains(Material.CALIBRATED_SCULK_SENSOR)) {
                commandSender.sendMessage("§7§oI need to put calibrated skulk sensor in the bottom middle slot!");
                return true;
            }
            commandSender.sendMessage("§7§oIt made three beeps and halted... Maybe it needed a calibrated sound sensor? " +
                    "Calibrated skulk sensor will do.");
            return true;
        }
        if (idx_8.getType() != Material.DAYLIGHT_DETECTOR) {
            if (core_inv.contains(Material.DAYLIGHT_DETECTOR)) {
                commandSender.sendMessage("§7§oI need to put daylight detector in bottom right slot!");
                return true;
            }
            commandSender.sendMessage("§7§oIt flashed a bright light and powered off... Looks like it needed a light sensor. " +
                    "Daylight detector will do the work.");
            return true;
        }
        return false;
    }

    public static boolean validateRocketIntegrity(CommandSender commandSender, Block[][][] blocks, int down_y, int fuel_y) {
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
            commandSender.sendMessage("[RCPU] §4A-ERR§r: Rocket hull should have at least 50% of iron blocks!");
            failed = true;
        }
        if (!invalidBlocks.isEmpty()) {
            commandSender.sendMessage("[RCPU] §4A-ERR§r: Rocket is not sealed!");
            commandSender.sendMessage("[RCPU] Invalid blocks found at:");
            for (Block block : invalidBlocks) {
                commandSender.sendMessage(" - " + block.getType() + " at " + block.getX() + " " + block.getY() + " " + block.getZ());
            }
            failed = true;
        }
        if (failed)
            commandSender.sendMessage("[RCPU] [§7INTEGRITY§r] §4§lFAIL");
        else
            commandSender.sendMessage("[RCPU] [§7INTEGRITY§r] §a§lОК");

        return failed;
    }

    public static boolean validateRocketStands(CommandSender commandSender, int down_y, int fuel_y, World world, int min_x, int min_z, int max_x, int max_z) {
        for (int y = down_y + 1; y < fuel_y; y++) {
            if (!(validateWallBlock(world.getBlockAt(min_x, y, min_z)) &&
                    validateWallBlock(world.getBlockAt(max_x, y, min_z)) &&
                    validateWallBlock(world.getBlockAt(min_x, y, max_z)) &&
                    validateWallBlock(world.getBlockAt(max_x, y, max_z)))
            ) {
                commandSender.sendMessage("[RCPU] §4A-ERR§r: Rocket does not have any stands from lodestone to fuel level");
                commandSender.sendMessage("[RCPU] [§7STANDS§r] §4§lFAIL");
                return true;
            }
        }
        commandSender.sendMessage("[RCPU] [§7STANDS§r] §a§lOK");
        return false;
    }

    public static int validateRocketFuel(CommandSender commandSender, Player player, int down_y, World world) {
        Inventory fuel_container = null;
        int fuel_y = -100;
        for (int y = down_y; y < player.getLocation().getBlockY() - 1; y++) {
            Block container = world.getBlockAt(player.getLocation().getBlockX(), y, player.getLocation().getBlockZ());
            if (container.getState() instanceof InventoryHolder) {
                fuel_container = ((InventoryHolder) (container.getState())).getInventory();
                fuel_y = y;
                break;
            }
        }
        if (fuel_container == null || fuel_y == -100) {
            commandSender.sendMessage("[RCPU] §4A-ERR§r: Rocket should have fuel storage between top and bottom lodestones");
            commandSender.sendMessage("[RCPU] [§7FUEL§r] §4§lFAIL");
            return -100;
        }
        commandSender.sendMessage("[RCPU] [§7FUEL§r] §a§lOK");
        return fuel_y;
    }

    public static boolean validateWallBlock(Block block) {
        ArrayList<Material> validWallBlocks = new ArrayList<>();
        validWallBlocks.add(Material.IRON_BLOCK);
        validWallBlocks.add(Material.QUARTZ_BLOCK);
        validWallBlocks.add(Material.SMOOTH_QUARTZ);
        validWallBlocks.add(Material.GLOWSTONE);
        validWallBlocks.add(Material.REDSTONE_LAMP);
        validWallBlocks.add(Material.LODESTONE);
        validWallBlocks.add(Material.GLASS);
        validWallBlocks.add(Material.TINTED_GLASS);

        validWallBlocks.add(Material.SPRUCE_DOOR);
        validWallBlocks.add(Material.WARPED_DOOR);
        validWallBlocks.add(Material.CRIMSON_DOOR);
        validWallBlocks.add(Material.BIRCH_DOOR);
        validWallBlocks.add(Material.DARK_OAK_DOOR);
        validWallBlocks.add(Material.MANGROVE_DOOR);

        validWallBlocks.add(Material.SPRUCE_TRAPDOOR);
        validWallBlocks.add(Material.BIRCH_TRAPDOOR);
        validWallBlocks.add(Material.DARK_OAK_TRAPDOOR);

        for (Material validWallBlock : validWallBlocks) {
            if (validWallBlock == block.getType()) {
                return true;
            }
        }
        return false;
    }

    public static int[] validateRocketSize(CommandSender commandSender, World world, Player player) {
        boolean failed = false;

        int top_y = -2147483647;
        int down_y = -2147483647;
        int min_x = -2147483647;
        int max_x = -2147483647;
        int min_z = -2147483647;
        int max_z = -2147483647;

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
        for (int x = player.getLocation().getBlockX(); x <= player.getLocation().getBlockX() + 5; x++) {
            if (world.getBlockAt(x, player.getLocation().getBlockY() - 1, player.getLocation().getBlockZ()).getType() == Material.LODESTONE) {
                max_x = x;
                break;
            }
        }
        for (int x = player.getLocation().getBlockX(); x >= player.getLocation().getBlockX() - 5; x--) {
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

        if (top_y == -2147483647) {
            commandSender.sendMessage("[RCPU] Rocket should have lodestone at the top of it right above dispencer.");
            failed = true;
        }
        if (down_y == -2147483647) {
            commandSender.sendMessage("[RCPU] Rocket should have lodestone at the bottom of it right below the dispencer.");
            failed = true;
        }
        if (down_y > -2147483647 && top_y > -2147483647 && top_y - down_y > 33) {
            commandSender.sendMessage("[RCPU] Rocket should not be more than 32 blocks tall!");
            failed = true;
        }
        if (max_x == -2147483647 || min_x == -2147483647) {
            commandSender.sendMessage("[RCPU] Rocket should have lodestone in it's walls by X axis right on Y-level with dispencer.");
            failed = true;
        }
        if (max_z == -2147483647 || min_z == -2147483647) {
            commandSender.sendMessage("[RCPU] Rocket should have lodestone in it's walls by Z axis right on Y-level with dispencer.");
            failed = true;
        }
        if (player.getLocation().getBlockX() - min_x != max_x - player.getLocation().getBlockX() || player.getLocation().getBlockZ() - min_z != max_z - player.getLocation().getBlockZ()) {
            commandSender.sendMessage("[RCPU] Rocket core (dispencer) should be in it's center");
            failed = true;
        }
        if (max_x - min_x != max_z - min_z) {
            commandSender.sendMessage("[RCPU] Rocket should have equal lengths on X and Z axes");
            failed = true;
        }
        if (max_x > -2147483647 && min_x > -2147483647 && Math.max(max_x - min_x, max_z - min_z) > 16) {
            commandSender.sendMessage("[RCPU] Rocket should not be more than 16 blocks wide!");
            failed = true;
        }
        if (failed) return new int[]{-1};
        return new int[]{top_y, down_y, max_x, min_x, max_z, min_z};
    }
}
