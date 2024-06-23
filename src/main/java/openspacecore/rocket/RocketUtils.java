package openspacecore.rocket;

import org.bukkit.World;
import org.bukkit.block.Block;

public class RocketUtils {
    public static Block[][][] getRocketBlocks(int top_y, int down_y, int max_x, int min_x, int max_z, int min_z, World world) {
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
}