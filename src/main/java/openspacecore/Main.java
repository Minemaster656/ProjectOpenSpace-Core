package openspacecore;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import openspacecore.command.LaunchRocket;

import java.io.*;
import java.util.Objects;

public final class Main extends JavaPlugin {
    public static Plugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Objects.requireNonNull(getServer().getPluginCommand("launchrocket")).setExecutor(new LaunchRocket());

        File container = plugin.getServer().getWorldContainer();
        String s_world = plugin.getServer().getWorlds().get(0).getName();
        String datapack_path = container.getAbsolutePath() + File.separator + s_world + File.separator +
                "datapacks" + File.separator;

        //noinspection ResultOfMethodCallIgnored
        new File(datapack_path).mkdirs();
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
    }
}
