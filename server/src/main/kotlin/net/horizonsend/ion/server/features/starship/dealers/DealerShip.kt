package net.horizonsend.ion.server.features.starship.dealers

import com.sk89q.worldedit.extent.clipboard.Clipboard
import net.horizonsend.ion.server.features.starship.StarshipType
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import java.time.Duration

abstract class DealerShip(
	val displayName: Component,
	val cooldown: Duration,
	val protectionCanBypass: Boolean,
	val starshipType: StarshipType
) {
	abstract fun getClipboard(): Clipboard
	abstract fun getIcon(): ItemStack
}
