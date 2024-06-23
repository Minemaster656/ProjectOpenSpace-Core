package openspacecore;

import org.bukkit.HeightMap;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import openspacecore.command.LaunchRocket;

import java.util.Objects;
import java.util.Random;

public final class Main extends JavaPlugin {
    public static Plugin plugin;

    private static class SpaceGenerator extends ChunkGenerator {
        @Override
        public int getBaseHeight(WorldInfo worldInfo, Random random, int x, int z, HeightMap heightMap) {
            return -63;
        }
    }

    @Override
    public void onEnable() {
        plugin = this;
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Objects.requireNonNull(getServer().getPluginCommand("launchrocket")).setExecutor(new LaunchRocket());

        new WorldCreator("space")
            .type(WorldType.FLAT)
            .generator(new SpaceGenerator())
            .environment(World.Environment.CUSTOM)
            .generateStructures(false)
            .createWorld();
    }
}
