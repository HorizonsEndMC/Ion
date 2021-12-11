package net.starlegacy.environments;

import net.starlegacy.feature.space.CachedPlanet;
import net.starlegacy.feature.space.Space;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public class Environments extends JavaPlugin implements Listener {

    public static Environments getInstance() {
        return (Environments) Bukkit.getPluginManager().getPlugin("Environments");
    }

    public static boolean isWearingEnvironmentSuit(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet == null) {
            return false;
        }

        List<String> lore = helmet.getLore();

        if (lore != null && (lore.contains("Module: environment") || lore.contains("Module: ENVIRONMENT"))) {
            return true;
        }

        return Arrays.stream(player.getInventory().getArmorContents())
                .allMatch(i -> i != null && i.getType().name().contains("CHAIN"));
    }

    public static boolean isBreathable(World world) {
        return getPlanetConfig(world.getName()).getBoolean("breathable");
    }

    public static boolean isRadioactive(World world) {
        return getPlanetConfig(world.getName()).getBoolean("radioactive");
    }

    public static int getTemperature(World world) {
        return getPlanetConfig(world.getName()).getInt("temperature");
    }

    public static double getGravity(World world) {
        return getPlanetConfig(world.getName()).getInt("gravity");
    }

    public static boolean isCold(World world) {
        return getTemperature(world) < 0;
    }

    public static boolean isHot(World world) {
        return getTemperature(world) > 0;
    }

    private static ConfigurationSection getPlanetConfig(String planet) {
        return getInstance().getConfig().getConfigurationSection("planets." + planet.toLowerCase());
    }

    @Override
    public void onEnable() {
        loadConfig();
        DebuffManager.init();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.DROWNING && event.getEntity() instanceof Player
                && isWearingEnvironmentSuit((Player) event.getEntity()))
            event.setCancelled(true);
    }

    private void loadConfig() {
        saveConfig();
        for (CachedPlanet planet : Space.INSTANCE.getPlanets()) {
            if (planet.getPlanetWorld() == null) {
                continue;
            }

            String prefix = "planets." + planet.getId() + ".";
            getConfig().set(prefix + "breathable", getConfig().getBoolean(prefix + "breathable", true));
            getConfig().set(prefix + "radioactive", getConfig().getBoolean(prefix + "radioactive", false));
            getConfig().set(prefix + "temperature", getConfig().getInt(prefix + "temperature", 0));
            getConfig().set(prefix + "gravity", getConfig().getDouble(prefix + "gravity", 1));
        }
        saveConfig();
    }
}
