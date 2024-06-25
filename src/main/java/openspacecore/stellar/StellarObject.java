package openspacecore.stellar;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StellarObject {
    String name;
    String dimension;
    StellarObject parent;
    int orbit;
    boolean traversable;
    HashMap<String, StellarObject> children = new HashMap<>();
    public StellarObject(boolean traversable, String name, StellarObject parent, int orbit, String dimension) {
        this.traversable = traversable;
        this.name = name;
        this.parent = parent;
        this.orbit = orbit;
        this.dimension = dimension;
    }

    public String getName() {
        return this.name;
    }
    public String getDimension() {
        return this.dimension;
    }
    public boolean getUsable() {
        return this.traversable;
    }
    public void addChild(String name, StellarObject child) {
        this.children.put(name, child);
    }
    public void removeChild(String name) {
        this.children.remove(name);
    }
    public Set<Map.Entry<String, StellarObject>> getChildren() {
        return this.children.entrySet();
    }
    public StellarObject getParent() {
        return this.parent;
    }
}
