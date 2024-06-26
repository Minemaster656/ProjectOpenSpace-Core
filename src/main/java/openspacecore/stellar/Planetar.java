package openspacecore.stellar;

import openspacecore.util.Utils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import static java.lang.Math.abs;

public class Planetar extends StellarObject {
    public Planetar(String name, String parent, int orbit, String dimension) {
        super(true, name, parent, orbit, dimension);
    }

    @Override
    public int[] findLandingLocation(Block core, int size, World target) {
        int rocket_x = core.getY(), rocket_z = core.getZ();

        int rx = -1;
        int rz = -1;
        boolean found = false;
        int ry = -1;
        random_location_find:
        for (int i = 0; i < 10; i++) {
            int shift_x = Utils.randomRangeRandom(-2000, 2000),
                    shift_z = Utils.randomRangeRandom(-2000, 2000);
            rx = rocket_x + shift_x;
            rz = rocket_z + shift_z;
            Block highest_block = target.getHighestBlockAt(rx, rz);
            int highest_y = highest_block.getY();
            if (highest_y > 280) continue;
            if (highest_y < 5) continue;
            if (highest_block.getType() == Material.LAVA ||
                    highest_block.getType() == Material.WATER) continue;
            for (int x = -size >> 1; x < size >> 1; x++) {
                for (int z = -size >> 1; z < size >> 1; z++) {
                    int highest_y_here = target.getHighestBlockAt(rx + x, rz + z).getY();
                    if (abs(highest_y_here - highest_y) > 2) continue random_location_find;
                }
            }
            found = true;
            ry = highest_y;
            break;
        }
        if (!found) return null;
        return new int[]{rx, ry, rz};
    }
}
