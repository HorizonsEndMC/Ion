package net.starlegacy.dutymode;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class DutyModeManager {
    private static File file;
    private static FileConfiguration configuration;

    public static void init() {
        file = new File(DutyMode.getInstance().getDataFolder(), "dutydata.yml");
        try {
            file.createNewFile();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        configuration = YamlConfiguration.loadConfiguration(file);
    }

    public static void save() {
        try {
            configuration.save(file);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static boolean isInDutyMode(Player player) {
        return DutyMode.getPermissionAPI().playerInGroup(player, "dutymode");
    }

    public static void disableDutyMode(Player player) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        Optional<String> server = luckPerms.getContextManager().getContext(player).getAnyValue("server");
        if (server.isPresent()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " parent remove dutymode server=" + server.get());
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " parent remove dutymode world=" + player.getWorld().getName());
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dynmap show " + player.getName());
        if (configuration.getConfigurationSection(player.getUniqueId().toString()) == null) return;
        World world = Bukkit.getWorld(configuration.getString(player.getUniqueId() + ".location.world", player.getLocation().getWorld().getName()));
        double x = configuration.getDouble(player.getUniqueId() + ".location.x", player.getLocation().getX());
        double y = configuration.getDouble(player.getUniqueId() + ".location.y", player.getLocation().getY());
        double z = configuration.getDouble(player.getUniqueId() + ".location.z", player.getLocation().getZ());
        Vector direction = configuration.getVector(player.getUniqueId() + ".location.direction", player.getLocation().getDirection());
        player.setHealth(configuration.getDouble(player.getUniqueId() + ".stats.health", player.getHealth()));
        player.teleport(new Location(world, x, y, z).setDirection(direction));
        for (int i = 0; i < player.getInventory().getSize(); i++)
            player.getInventory().setItem(i, configuration.getItemStack(player.getUniqueId() + ".inventory." + i));
        player.setOp(false);
        player.setGameMode(GameMode.SURVIVAL);
    }

    public static void enableDutyMode(Player player) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        Optional<String> server = luckPerms.getContextManager().getContext(player).getAnyValue("server");
        if (server.isPresent()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " parent add dutymode server=" + server.get());
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " parent add dutymode world=" + player.getWorld().getName());
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dynmap hide " + player.getName());
        configuration.set(player.getUniqueId() + ".location.world", player.getLocation().getWorld().getName());
        configuration.set(player.getUniqueId() + ".location.x", player.getLocation().getX());
        configuration.set(player.getUniqueId() + ".location.y", player.getLocation().getY());
        configuration.set(player.getUniqueId() + ".location.z", player.getLocation().getZ());
        configuration.set(player.getUniqueId() + ".location.direction", player.getLocation().getDirection());
        configuration.set(player.getUniqueId() + ".stats.health", player.getHealth());
        for (int i = 0; i < player.getInventory().getSize(); i++)
            configuration.set(player.getUniqueId() + ".inventory." + i, player.getInventory().getItem(i));
        player.getInventory().clear();
        player.setGameMode(GameMode.CREATIVE);
        player.setOp(player.hasPermission("rudiments.dutymode.op"));
        save();
    }
}
