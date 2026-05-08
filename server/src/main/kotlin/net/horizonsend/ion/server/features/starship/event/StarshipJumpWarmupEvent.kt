package net.horizonsend.ion.server.features.starship.event

import net.horizonsend.ion.server.features.starship.Starship
import org.bukkit.Location
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList

class StarshipJumpWarmupEvent(ship: Starship, val from: Location, val to: Location) : StarshipEvent(ship), Cancellable {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    override fun isCancelled(): Boolean {
        return this.isCancelled
    }

    override fun setCancelled(cancelled: Boolean) {
        this.isCancelled = cancelled
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}