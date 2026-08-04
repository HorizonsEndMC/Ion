package net.horizonsend.ion.server.features.economy.bazaar.event

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList

class BazaarCreateBuyOrderEvent(
    val player: Player,
    val itemString: String,
    val amount: Int,
    val cost: Double,
    val city: String,
) : BazaarEvent() {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}