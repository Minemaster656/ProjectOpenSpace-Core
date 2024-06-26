package openspacecore.stellar;

import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static openspacecore.util.Utils.getStellarNoChecks;

public class StellarObject {
    String name;
    String dimension;
    String parent;
    int orbit;
    boolean traversable;
    HashMap<String, StellarObject> children = new HashMap<>();
    public StellarObject(boolean traversable, String name, String parent, int orbit, String dimension) {
        this.traversable = traversable;
        this.name = name;
        this.parent = parent;
        this.orbit = orbit;
        this.dimension = dimension;
    }

    public int[] findLandingLocation(Block core, int size, World target) {
        return null;
    }

    public String getName() {
        return this.name;
    }
    public String getDimension() {
        return this.dimension;
    }
    public boolean getUnusable() {
        return !this.traversable;
    }
    public void addChild(StellarObject child) {
        this.children.put(child.getName(), child);
    }
    public StellarObject getChild(String name) {
        return this.children.get(name);
    }
    public Set<Map.Entry<String, StellarObject>> getChildren() {
        return this.children.entrySet();
    }
    public StellarObject getParent() {
        return getStellarNoChecks(this.parent);
    }
    public String getPathedName() {
        StringBuilder str = new StringBuilder();
        StellarObject parent = this;
        int safty = 0;
        while (parent != null && safty < 50) {
            safty++;
            str.insert(0, parent.getName()+"/");
            parent = parent.getParent();
        }
        str.deleteCharAt(str.length() - 1);
        return new String(str);
    }
}
