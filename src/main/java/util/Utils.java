package util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;

import java.util.concurrent.ThreadLocalRandom;

public class Utils {
    public static int randomRangeRandom(int start, int end) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
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
