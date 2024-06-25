package openspacecore.stellar;

public class Planet extends StellarObject{
    public Planet(String name, String parent, int orbit, String dimension) {
        super(true, name, parent, orbit, dimension);
    }
}
