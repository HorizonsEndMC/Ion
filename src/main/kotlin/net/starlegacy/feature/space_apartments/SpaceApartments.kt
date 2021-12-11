package net.starlegacy.feature.space_apartments

import net.starlegacy.SLComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object SpaceApartments : SLComponent() {
    fun getWorld() = Bukkit.getWorld("SpaceApartments") ?: error("Missing apartment world")

    fun send(player: Player) {
        if (!hasApartment(player)) {
            paste(player)
        }
        teleportToApartmentLocation(player)
    }

    private fun hasApartment(player: Player): Boolean {
        return ApartmentData.contains(player)
    }

    fun paste(player: Player) {
        createApartment(player)
    }

    private fun createApartment(player: Player) {
        if (!hasApartment(player)) {
            ApartmentData.addUser(player)
        }
        val index = ApartmentData.getIndex(player)
        ApartmentSchematic.paste(index)
        ApartmentSchematic.createWorldGuardRegion(index, player)
    }

    private fun teleportToApartmentLocation(player: Player) {
        val index = ApartmentData.getIndex(player)
        val location = ApartmentSchematic.getLocation(index)
        player.teleport(location)
    }
}
