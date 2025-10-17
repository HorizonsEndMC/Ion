package net.horizonsend.ion.server.miscellaneous.utils.hooks

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.StateFlag
import org.bukkit.Location
import org.bukkit.entity.Player

fun isWorldGuardDenied(player: Player, location: Location, vararg flags: StateFlag): Boolean {
	val worldguard = WorldGuard.getInstance()
	val wrappedPlayer = WorldGuardPlugin.inst().wrapPlayer(player)
	return !worldguard.platform.regionContainer.createQuery().testState(BukkitAdapter.adapt(location), wrappedPlayer, *flags)
}
