package openspacecore;

import openspacecore.stellar.StellarObject;
import openspacecore.util.Utils;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import openspacecore.command.LaunchRocket;

import java.io.*;
import java.util.HashMap;
import java.util.Objects;
import java.util.zip.ZipFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class Main extends JavaPlugin {
    public static Plugin plugin;
    public static HashMap<String, StellarObject> stellars = new HashMap<>();

    @Override
    public void onEnable() {
        plugin = this;
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Objects.requireNonNull(getServer().getPluginCommand("launchrocket")).setExecutor(new LaunchRocket());

        File container = plugin.getServer().getWorldContainer();
        String s_world = plugin.getServer().getWorlds().get(0).getName();
        String datapack_path = container.getAbsolutePath() + File.separator + s_world + File.separator +
                "datapacks" + File.separator;
        File datapacks_file = new File(datapack_path);
        //noinspection ResultOfMethodCallIgnored
        datapacks_file.mkdirs();
        File datapack_file = new File(datapack_path + "openspacecore_dp.zip");
        if (!datapack_file.exists()) {
            try (InputStream datapack_is = plugin.getResource("datapack.zip")) {
                try (FileOutputStream datapack_fw = new FileOutputStream(datapack_file)) {
                    assert datapack_is != null;
                    byte[] buf = new byte[1024];
                    int len;
                    try {
                        while ((len = datapack_is.read(buf)) > 0) {
                            datapack_fw.write(buf, 0, len);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            plugin.getLogger().warning("Unpacked openspace core datapack. Restart the server to apply changes.");
            plugin.getServer().shutdown();
        }

        for (final File fileEntry : Objects.requireNonNull(datapacks_file.listFiles())) {
            if (fileEntry.isDirectory()) {
                try (InputStream openspace_meta = new FileInputStream(fileEntry.getAbsolutePath() + File.separator + "openspace.json")) {
                    String meta = new String(openspace_meta.readAllBytes());
                    Utils.parseMeta(meta);
                    plugin.getLogger().info("Parsed openspace metadata from datapack " + fileEntry.getName());
                } catch (IOException ignored) {}
            } else {
                if (!fileEntry.getName().endsWith(".zip")) {
                    plugin.getLogger().warning("Non-zip or directory found in datapacks folder. Skipping");
                    continue;
                }
                try (ZipFile datapack_zip = new ZipFile(fileEntry)) {
                    InputStream openspace_meta = datapack_zip.getInputStream(
                            datapack_zip.getEntry("openspace.json")
                    );
                    String meta = new String(openspace_meta.readAllBytes());
                    Utils.parseMeta(meta);
                    plugin.getLogger().info("Parsed openspace metadata from datapack " + fileEntry.getName());
                }  catch (IOException ignored) {}
            }
        }

        plugin.getLogger().info("Parsed "+this.stellars.size()+" celeatial bodies: ");

        // TODO: скопировать для UI в игре

        for (Map.Entry<String, StellarObject> stellare : this.stellars.entrySet()) {
            StellarObject stellar = stellare.getValue();
            if (stellar.getParent() != null) continue;
            String path = stellar.getPathedName();
            plugin.getLogger().info(path);
            printStellars(stellar.getChildren(), new boolean[0]);
        }
    }
    void printStellars(Set<Map.Entry<String, StellarObject>> stellars, boolean[] progress) {
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
            String indent = "";
            for (int i = 0; i < progress.length; i++) {
                boolean progressing = progress[i];
                if (!progressing) {
                    if (i == progress.length - 1) indent = indent + "`- ";
                    else indent = indent + "   ";
                    continue;
                }
                if (i == progress.length - 1) indent = indent + "|- ";
                else indent = indent + "|  ";
            }
            plugin.getLogger().info(indent + stellar.getName());
            if (!stellar.getChildren().isEmpty()) printStellars(stellar.getChildren(), progress);
        }
    }
}
