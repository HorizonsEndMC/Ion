package net.horizonsend.ion.server.features.economy.bazaar.event

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList

class BazaarDeleteBuyOrderEvent(
    val player: Player,
    val refund: Double,
) : BazaarEvent() {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}