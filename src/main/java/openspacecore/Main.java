package openspacecore;

import openspacecore.command.completer.LaunchRocketTabCompleter;
import openspacecore.stellar.StellarObject;
import openspacecore.util.Utils;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import openspacecore.command.LaunchRocket;

import java.io.*;
import java.util.HashMap;
import java.util.Objects;
import java.util.zip.ZipFile;

public final class Main extends JavaPlugin {
    public static Plugin plugin;
    public static HashMap<String, StellarObject> stellars = new HashMap<>();

    @Override
    public void onEnable() {
        plugin = this;
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        PluginCommand lr_command = Objects.requireNonNull(getServer().getPluginCommand("launchrocket"));
        lr_command.setExecutor(new LaunchRocket());
        lr_command.setTabCompleter(new LaunchRocketTabCompleter());

        File container = plugin.getServer().getWorldContainer();
        String s_world = "world";
        String datapack_path = container.getAbsolutePath() + File.separator + s_world + File.separator +
                "datapacks" + File.separator;
        File datapacks_file = new File(datapack_path);
        //noinspection ResultOfMethodCallIgnored
        datapacks_file.mkdirs();
        File datapack_file = new File(datapack_path + "openspacecore_dp.zip");
        boolean invalid_hash = !datapack_file.exists();
        if (!invalid_hash) {
            int hash1, hash2;
            try (InputStream datapack_is = plugin.getResource("datapack.zip")) {
                assert datapack_is != null;
                byte[] buf = new byte[1024];
                StringBuilder tohash = new StringBuilder();
                try {
                    while (datapack_is.read(buf) > 0) {
                        tohash.append(new String(buf));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                hash1 = tohash.hashCode();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try (InputStream datapack_is = new FileInputStream(datapack_file)) {
                byte[] buf = new byte[1024];
                StringBuilder tohash = new StringBuilder();
                try {
                    while (datapack_is.read(buf) > 0) {
                        tohash.append(new String(buf));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                hash2 = tohash.hashCode();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            invalid_hash = hash1 != hash2;
        }
        if (invalid_hash) {
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
            plugin.getLogger().warning("Unpacked openspace core datapack.");
            // plugin.getServer().shutdown();
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

        Utils.completeInit();

        plugin.getLogger().info("Parsed celestial bodies: ");
        Utils.printStellars(null);
    }
}
