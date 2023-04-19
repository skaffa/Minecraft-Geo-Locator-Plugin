package me.gerbert.geoloc.geo_locator;

import org.bukkit.plugin.java.JavaPlugin;

public final class Geo_locator extends JavaPlugin {

    @Override
    public void onEnable() {
        getCommand("geo").setExecutor(new GeoCommand(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
