package net.starlegacy.dutymode;

import net.milkbowl.vault.permission.Permission;
import net.starlegacy.dutymode.commands.DutyModeCommand;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public class DutyMode extends JavaPlugin {
    private static Permission permissionAPI;
    private static DutyMode instance;

    public static Permission getPermissionAPI() {
        return permissionAPI;
    }

    public static DutyMode getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        getDataFolder().mkdirs();
        permissionAPI = getServer().getServicesManager().getRegistration(Permission.class).getProvider();
        DutyModeManager.init();
        registerCommands();
    }

    private void registerCommands() {
        registerCommand("dutymode", new DutyModeCommand());
    }

    private void registerCommand(String command, CommandExecutor executor) {
        getCommand(command).setExecutor(executor);
    }
}
