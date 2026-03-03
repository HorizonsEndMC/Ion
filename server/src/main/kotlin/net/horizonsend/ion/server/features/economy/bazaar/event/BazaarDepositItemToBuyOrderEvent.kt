package net.horizonsend.ion.server.features.economy.bazaar.event

import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList

class BazaarDepositItemToBuyOrderEvent(
    val player: Player,
    val item: BazaarOrder,
    val amount: Int,
    val subtotal: Double,
    val tax: Double,
    val total: Double,
    val city: String
) : BazaarEvent() {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}