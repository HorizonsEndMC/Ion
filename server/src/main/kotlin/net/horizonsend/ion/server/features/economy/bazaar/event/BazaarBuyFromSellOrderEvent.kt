package net.horizonsend.ion.server.features.economy.bazaar.event

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList

class BazaarBuyFromSellOrderEvent(
    val player: Player,
    val item: BazaarItem,
    val amount: Int,
    val subtotal: Double,
    val tax: Double,
    val total: Double,
    val wasRemote: Boolean
) : BazaarEvent() {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}