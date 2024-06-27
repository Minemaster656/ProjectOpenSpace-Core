package openspacecore.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import openspacecore.Main;
import openspacecore.stellar.Orbit;
import openspacecore.stellar.Planetar;
import openspacecore.stellar.Star;
import openspacecore.stellar.StellarObject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;

import java.util.*;
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

            Main.plugin.getLogger().info("Discovered "+name+" of type "+type);

            StellarObject stellarobj = null;

            if (Objects.equals(type, "planet") || Objects.equals(type, "moon")) {
                String parent = object.get("parent").getAsString();
                Main.plugin.getLogger().info("Parented to "+parent);
                String dimension = object.get("dimension").getAsString();
                int orbit = object.get("orbit").getAsInt();
                stellarobj = new Planetar(name, parent, orbit, dimension);
                StellarObject stellarparent = Main.stellars.get(parent);
                if (stellarparent != null) {
                    stellarparent.addChild(stellarobj);
                }
            } else if (Objects.equals(type, "star")) {
                stellarobj = new Star(name);
            } else if (Objects.equals(type, "orbit")) {
                String parent = object.get("parent").getAsString();
                Main.plugin.getLogger().info("Parented to "+parent);
                String dimension = object.get("dimension").getAsString();
                int orbit = object.get("orbit").getAsInt();
                stellarobj = new Orbit(name, parent, orbit, dimension);
                StellarObject stellarparent = Main.stellars.get(parent);
                if (stellarparent != null) {
                    stellarparent.addChild(stellarobj);
                }
            }
            if (stellarobj == null) continue;
            Main.stellars.put(stellarobj.getPathedName(), stellarobj);
        }
    }
    public static void completeInit() {
        for (Map.Entry<String, StellarObject> stelr : Main.stellars.entrySet()) {
            StellarObject stobj = stelr.getValue();
            StellarObject stpnt = stobj.getParent();
            if (stpnt == null) continue;
            if (stpnt.getChild(stobj.getName()) == null) stpnt.addChild(stobj);
        }
        List<String> to_remove = new ArrayList<>();
        for (Map.Entry<String, StellarObject> stelr : Main.stellars.entrySet()) {
            if (stelr.getValue().getParent() == null) continue;
            to_remove.add(stelr.getKey());
        }
        for (String key : to_remove) {
            Main.plugin.getLogger().info("Removed parented stellar from roots: "+key);
            Main.stellars.remove(key);
        }
    }

    public static World getWorld(String targetQuery) {
        StellarObject stellar = getStellar(targetQuery);
        if (stellar == null) return null;
        return getWorldFromKey(stellar.getDimension());
    }
    public static StellarObject getStellar(String targetQuery) {
        StellarObject stellar = getStellarNoChecks(targetQuery);
        if (stellar != null && stellar.getUnusable()) return null;
        return stellar;
    }
    public static StellarObject getStellarNoChecks(String targetQuery) {
        if (targetQuery == null) return null;
        String[] parts = targetQuery.split("/");
        StellarObject stellar = Main.stellars.get(parts[0]);
        for (String part : parts) {
            if (Objects.equals(part, parts[0])) continue;
            if (stellar == null) return null;
            stellar = stellar.getChild(part);
        }
        return stellar;
    }
    public static StellarObject getStellarFromWorld(World world) {
        List<StellarObject> stellars = getAllStellars(null);
        for (StellarObject stellar : stellars) {
            if (!Objects.equals(stellar.getDimension(), world.getKey().toString()))
                return stellar;
        }
        return null;
    }

    public static List<StellarObject> getAllStellars(Set<Map.Entry<String, StellarObject>> stellars_step) {
        List<StellarObject> stellars = new ArrayList<>();
        if (stellars_step == null) {
            stellars_step = Main.stellars.entrySet();
        }
        for (Map.Entry<String, StellarObject> stellare : stellars_step) {
            stellars.add(stellare.getValue());
            if (!stellare.getValue().getChildren().isEmpty())
                stellars.addAll(getAllStellars(stellare.getValue().getChildren()));
        }
        return stellars;
    }

    public static World getWorldFromKey(String key) {
        NamespacedKey kkey = NamespacedKey.fromString(key);
        for (World w : Bukkit.getWorlds()) {
            if (w.getKey().equals(kkey)) return w;
        }
        return null;
    }

    public static void printStellars(CommandSender commandSender) {
        for (Map.Entry<String, StellarObject> stellare : Main.stellars.entrySet()) {
            StellarObject stellar = stellare.getValue();
            if (stellar.getParent() != null) continue;
            String path = stellar.getPathedName();
            if (commandSender == null) {
                Main.plugin.getLogger().info(path);
            } else {
                commandSender.sendMessage(path);
            }
            printStellars(commandSender, stellar.getChildren(), new boolean[0]);
        }
    }
    public static void printStellars(CommandSender commandSender, Set<Map.Entry<String, StellarObject>> stellars, boolean[] progress) {
        boolean[] prog = new boolean[progress.length + 1];
        prog[prog.length - 1] = true;
        System.arraycopy(progress, 0, prog, 0, progress.length);
        progress = prog;
        StellarObject last = null;
        for (Map.Entry<String, StellarObject> stellare : stellars) {
            last = stellare.getValue();
        }
        for (Map.Entry<String, StellarObject> stellare : stellars) {
            StellarObject stellar = stellare.getValue();
            if (Objects.equals(stellar, last)) {
                progress[progress.length - 1] = false;
            }
            StringBuilder indent = new StringBuilder();
            for (int i = 0; i < progress.length; i++) {
                boolean progressing = progress[i];
                if (!progressing) {
                    if (i == progress.length - 1) indent.append("`- ");
                    else indent.append("   ");
                    continue;
                }
                if (i == progress.length - 1) indent.append("|- ");
                else indent.append("|  ");
            }
            if (commandSender == null) {
                Main.plugin.getLogger().info(indent + stellar.getName());
            } else {
                commandSender.sendMessage(indent + stellar.getName());
            }
            if (!stellar.getChildren().isEmpty()) printStellars(commandSender, stellar.getChildren(), progress);
        }
    }
}
