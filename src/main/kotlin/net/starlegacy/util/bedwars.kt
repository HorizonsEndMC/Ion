package net.starlegacy.util

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.screamingsandals.bedwars.api.BedwarsAPI

fun isInBedWarsGame(player: Player): Boolean {
	if (!Bukkit.getPluginManager().isPluginEnabled("BedWars")) {
		return false
	}

	return BedwarsAPI.getInstance().isEntityInGame(player)
}
