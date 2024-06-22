package util;

import com.gdt.openspacecore.Main;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.NamespacedKey;
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
        return block.getType() == Material.AIR ||
               block.getType() == Material.CAVE_AIR ||
               block.getType() == Material.VOID_AIR;
    }

    public static World getPlanet(String key) {
        NamespacedKey kkey = NamespacedKey.fromString(key);
        for (World w : Bukkit.getWorlds()) {
            if (w.getKey().equals(kkey)) return w;
        };
        return null;
    }
}
