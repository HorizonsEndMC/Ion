package net.starlegacy.dutymode.commands;

import net.starlegacy.dutymode.DutyModeManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DutyModeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        if (!sender.hasPermission("rudiments.dutymode")) {
            sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
            return true;
        }
        if (args.length > 0 && !sender.hasPermission("rudiments.dutymode.others")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to toggle duty mode for other players.");
            return true;
        }
        Player player = Bukkit.getPlayer(args.length > 0 ? args[0] : sender.getName());
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
        if (DutyModeManager.isInDutyMode(player)) {
            DutyModeManager.disableDutyMode(player);
            sender.sendMessage(ChatColor.AQUA + "Disabled duty mode.");
            if (player != sender)
                player.sendMessage(ChatColor.AQUA + "Duty mode disabled.");
            Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission("rudiments.dutymode")).forEach(p -> p.sendMessage(
                    sender.getName() + " has disabled duty mode" + (player == sender ? "." : " for " + player.getName() + ".")));
        } else {
            DutyModeManager.enableDutyMode(player);
            sender.sendMessage(ChatColor.AQUA + "Enabled duty mode.");
            if (player != sender)
                player.sendMessage(ChatColor.AQUA + "Duty mode enabled.");
            Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission("rudiments.dutymode")).forEach(p -> p.sendMessage(
                    sender.getName() + " has enabled duty mode" + (player == sender ? "." : " for " + player.getName() + ".")));
        }
        return true;
    }
}
