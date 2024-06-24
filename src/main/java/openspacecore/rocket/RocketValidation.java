package openspacecore.rocket;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;

public class RocketValidation {
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
        for (int y = down_y; y < player.getLocation().getBlockY(); y++) {
            if (world.getBlockAt(player.getLocation().getBlockX(), y, player.getLocation().getBlockZ()).getState() instanceof InventoryHolder) {
                fuel_container = ((InventoryHolder) (world.getBlockAt(player.getLocation().getBlockX(), y, player.getLocation().getBlockZ()).getState())).getInventory();
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