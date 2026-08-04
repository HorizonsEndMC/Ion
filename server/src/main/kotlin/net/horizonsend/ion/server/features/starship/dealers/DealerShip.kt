package net.horizonsend.ion.server.features.starship.dealers

import com.sk89q.worldedit.extent.clipboard.Clipboard
import net.horizonsend.ion.common.utils.text.gui.sendWithdrawMessage
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.withdrawMoney
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

abstract class DealerShip(
	open val displayName: Component,
	open val price: Double,
	open val protectionCanBypass: Boolean,
	open val starshipType: StarshipType,
	open val pilotOffset: Vec3i = Vec3i(0, 0, 0)
) {
	abstract fun getClipboard(): Clipboard
	abstract fun getIcon(): ItemStack

	open fun canBuy(purchaser: Player): Boolean = true

	open fun onPurchase(purchaser: Player) {
		purchaser.withdrawMoney(price)
		sendWithdrawMessage(purchaser, price)
	}

	open fun postPilot(purchaser: Player, ship: Starship) {}
}
