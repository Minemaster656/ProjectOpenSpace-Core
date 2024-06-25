package openspacecore.stellar;

import openspacecore.Main;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    public String getName() {
        return this.name;
    }
    public String getDimension() {
        return this.dimension;
    }
    public boolean getUsable() {
        return this.traversable;
    }
    public void addChild(StellarObject child) {
        this.children.put(child.getPathedName(), child);
    }
    public void removeChild(StellarObject child) {
        this.children.remove(child.getPathedName());
    }
    public Set<Map.Entry<String, StellarObject>> getChildren() {
        return this.children.entrySet();
    }
    public StellarObject getParent() {
        return Main.stellars.get(this.parent);
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
