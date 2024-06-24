package openspacecore.rocket;

import openspacecore.util.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.List;

public class RocketLaunch {
    public static void launch(World current, World target, int rx, int rz, int ry, int size, Block[][][] rocket) {
        rx -= size >> 1;
        rz -= size >> 1;

        // first iteration: set all blocks that don't need supporting blocks
        // second iteration: set all other blocks, teleport entities
        // third iteration: remove old rocket

        List<Entity> teleported = new ArrayList<>();

        for (int iter = 0; iter < 3; iter++) {
            for (int x = rx; x < rx + size; x++) {
                int local_x = x - rx;
                for (int z = rz; z < rz + size; z++) {
                    int local_z = z - rz;
                    for (int y = ry + 1; y < ry + rocket.length + 1; y++) {
                        int local_y = y - ry - 1;
                        if (iter == 0) {
                            Location new_loc = new Location(target, x, y, z);
                            if (!new_loc.getChunk().isLoaded()) new_loc.getChunk().load();
                            Block block = rocket[local_y][local_x][local_z];
                            Block newblock = new_loc.getBlock();
                            newblock.breakNaturally();
                            newblock.setType(block.getType(), false);
                            newblock.setBlockData(block.getBlockData());
                        } else if (iter == 1) {
                            Location new_loc = new Location(target, x, y, z);
                            Block block = rocket[local_y][local_x][local_z];
                            Block newblock = new_loc.getBlock();
                            if (Utils.isAir(newblock)) {
                                newblock.setType(block.getType(), false);
                                newblock.setBlockData(block.getBlockData());
                            }
                            for (Entity ent : current.getNearbyEntities(BoundingBox.of(block))) {
                                if (teleported.contains(ent)) continue;
                                Location new_pos = newblock.getLocation();
                                new_pos.setPitch(ent.getLocation().getPitch());
                                new_pos.setYaw(ent.getLocation().getYaw());
                                new_pos.add(0.5, 0.5, 0.5);
                                ent.teleport(new_pos);
                                teleported.add(ent);
                            }
                            if (block.getState() instanceof InventoryHolder) {
                                Inventory inventory = ((InventoryHolder) block.getState()).getInventory();
                                Inventory new_inventory = ((InventoryHolder) newblock.getState()).getInventory();
                                for (int i = 0; i < inventory.getSize(); i++) {
                                    new_inventory.setItem(i, inventory.getItem(i));
                                }
                            }
                        } else {
                            Block block = rocket[local_y][local_x][local_z];
                            Location current_loc = new Location(current, block.getX(), block.getY(), block.getZ());
                            Block curblock = current_loc.getBlock();
                            curblock.breakNaturally(new ItemStack(Material.AIR));
                        }
                    }
                }
            }
        }
        target.getBlockAt(rx + (size >> 1), ry, rz + (size >> 1)).breakNaturally(new ItemStack(Material.AIR));
        target.getBlockAt(rx + (size >> 1), ry, rz + (size >> 1)).setType(Material.LODESTONE, false);
        current.getBlockAt(rx + (size >> 1), ry, rz + (size >> 1)).breakNaturally(new ItemStack(Material.AIR));
        current.createExplosion(rx + (size >> 1), ry + 1, rz + (size >> 1), 3*4f);
    }
}

