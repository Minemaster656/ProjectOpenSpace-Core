package openspacecore.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import openspacecore.Main;
import openspacecore.stellar.Planet;
import openspacecore.stellar.Star;
import openspacecore.stellar.StellarObject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    public static void parseMeta(String meta) {
        JsonArray meta_array = (JsonArray) JsonParser.parseString(meta);
        for (Object meta_obj : meta_array) {
            JsonObject object = (JsonObject) meta_obj;
            String type = object.get("type").getAsString();
            String name = object.get("name").getAsString();

            StellarObject stellarobj = null;

            if (Objects.equals(type, "planet")) {
                String parent = object.get("parent").getAsString();
                String dimension = object.get("dimension").getAsString();
                int orbit = object.get("orbit").getAsInt();
                stellarobj = new Planet(name, parent, orbit, dimension);
                StellarObject stellarparent = Main.stellars.get(parent);
                if (stellarparent != null) {
                    stellarparent.addChild(stellarobj);
                }
            } else if (Objects.equals(type, "star")) {
                stellarobj = new Star(name);
            }
            for (Map.Entry<String, StellarObject> stelr : Main.stellars.entrySet()) {
                String stname = stelr.getKey();
                StellarObject stobj = stelr.getValue();
                StellarObject stpnt = stobj.getParent();
                if (stpnt != null) {
                    boolean contains = false;
                    for (Map.Entry<String, StellarObject> stelrchild : stpnt.getChildren()) {
                        if (stelrchild.getValue().getPathedName() == stname) {
                            contains = true;
                            break;
                        }
                    }
                    if (!contains) stpnt.addChild(stobj);
                }
            }
            if (stellarobj == null) continue;
            Main.stellars.put(stellarobj.getPathedName(), stellarobj);
        }
    }

    public static World getPlanet(String planet_name) {
        StellarObject stellar = Main.stellars.get(planet_name);
        if (stellar != null && !stellar.getUsable()) return null;
        if (stellar == null) return null;
        return getWorldFromKey(stellar.getDimension());
    }

    public static World getWorldFromKey(String key) {
        NamespacedKey kkey = NamespacedKey.fromString(key);
        for (World w : Bukkit.getWorlds()) {
            if (w.getKey().equals(kkey)) return w;
        }
        return null;
    }

    public static void printExistingStellars(CommandSender commandSender, Set<Map.Entry<String, StellarObject>> stellars) {
        for (Map.Entry<String, StellarObject> stellare : stellars) {
            StellarObject stellar = stellare.getValue();
            commandSender.sendMessage(stellar.getPathedName());
            if (!stellar.getChildren().isEmpty()) printExistingStellars(commandSender, stellar.getChildren());
        }
    }
}
