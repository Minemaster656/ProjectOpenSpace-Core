package openspacecore.rocket;

import openspacecore.util.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public class RocketLaunch {
    public static void launch(World current, World target, int rx, int rz, int ry, int size, Block[][][] rocket, Block core) {
        rx -= size >> 1;
        rz -= size >> 1;

        // first iteration: set all blocks that don't need supporting blocks
        // second iteration: set all other blocks
        // third iteration: remove old rocket
        for (int iter = 0; iter < 3; iter++) {
            for(int x = rx; x < rx+size; x++) {
                int local_x = x - rx;
                for(int z = rz; z < rz+size; z++) {
                    int local_z = z - rz;
                    for(int y = ry+1; y < ry + rocket.length + 1; y++) {
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
                        } else {
                            Block block = rocket[local_y][local_x][local_z];
                            Location current_loc = new Location(current, block.getX(), block.getY(), block.getZ());
                            Block curblock = current_loc.getBlock();
                            curblock.breakNaturally();
                        }
                    }
                }
            }
        }
        for (Entity ent : current.getNearbyEntities(BoundingBox.of(rocket[0][0][0], rocket[rocket.length-1][rocket[0].length-1][rocket[0][0].length-1]))) {
            Location origin = rocket[0][0][0].getLocation();
            Location offset = new Location(target, rx, ry, rz).subtract(ent.getLocation());
            Location real = new Location(target, rx + offset.getX(), ry + offset.getY(), rz + offset.getZ());
            ent.teleport(real);
        }
        target.getBlockAt(rx + size << 1, ry - 1, rz + size << 1).setType(Material.LODESTONE, false);
        current.getBlockAt(rx + size << 1, ry - 1, rz + size << 1).setType(Material.AIR, false);
        current.createExplosion(rx + size << 1, ry - 1, rz + size << 1, 4f);
    }
}
