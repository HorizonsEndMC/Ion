package net.horizonsend.ion.server.features.starship.dealers

import com.sk89q.worldedit.extent.clipboard.Clipboard
import net.horizonsend.ion.server.features.starship.StarshipType
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import java.time.Duration

class PlayerCreatedDealerShip(
	displayName: Component,
	cooldown: Duration,
	protectionCanBypass: Boolean,
	starshipType: StarshipType
) : DealerShip(displayName, cooldown, protectionCanBypass, starshipType) {
	override fun getClipboard(): Clipboard {
		TODO("Not yet implemented")
	}

	override fun getIcon(): ItemStack {
		TODO("Not yet implemented")
	}
}
