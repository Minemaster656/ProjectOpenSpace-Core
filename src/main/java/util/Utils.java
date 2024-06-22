package util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Random;

public class Utils {
    public static int getHighestY(World world, int x, int z) {
        for (int i = 320; i >= -64; i--) {
            if(!isAir(new Location(world, x, i, z).getBlock()))
                return i;
        }
        return -64;
    }
    public static int randomRangeRandom(int start, int end) {
        Random random = new Random();
        return random.nextInt((end - start) + 1) + start;
    }
    public static boolean isAir(Block block) {
        return block.getType() == Material.AIR || block.getType() == Material.CAVE_AIR || block.getType() == Material.VOID_AIR;
    }

}
