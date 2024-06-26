package openspacecore.stellar;

import org.bukkit.World;
import org.bukkit.block.Block;


public class Orbit extends StellarObject {
    public Orbit(String name, String parent, int orbit, String dimension) {
        super(true, name, parent, orbit, dimension);
    }
    @Override
    public int[] findLandingLocation(Block core, int size, World target) {
        return new int[]{core.getX(), 128, core.getZ()};
    }
}
