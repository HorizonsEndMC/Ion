package net.horizonsend.ion.server.features.economy.bazaar.event

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList

class BazaarCollectMoneyFromSellOrdersEvent(
    val player: Player,
    val amount: Int,
) : BazaarEvent() {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}