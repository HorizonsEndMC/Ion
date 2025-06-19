package net.horizonsend.ion.server.features.starship.dealers

import com.sk89q.worldedit.extent.clipboard.Clipboard
import net.horizonsend.ion.server.features.starship.StarshipType
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.time.Duration

abstract class DealerShip(
	open val displayName: Component,
	open val cooldown: Duration,
	open val price: Double,
	open val protectionCanBypass: Boolean,
	open val starshipType: StarshipType
) {
	abstract fun getClipboard(): Clipboard
	abstract fun getIcon(): ItemStack

	open fun onPurchase(purchaser: Player) {}
}
