package com.gdt.pospcore;

import command.LaunchRocket;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    public static Plugin plugin;
    public static boolean debug=true;
    @Override
    public void onEnable() {

        plugin = this;

//        getServer().getLogger().info("\n"+Data.text_logo);
//        getServer().getPluginManager().registerEvents(this, this);
//        getServer().getPluginManager().registerEvents(this, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
//        getServer().getPluginManager().registerEvents(new SimpleEventHandler(), this);

        getServer().getPluginCommand("launchrocket").setExecutor(new LaunchRocket());

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
