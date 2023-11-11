package net.horizonsend.ion.server.features.starship.movement

import io.papermc.paper.adventure.PaperAdventure
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.minecraft.world.level.block.state.BlockState

abstract class StarshipMovementException(override val message: String) : RuntimeException() {
	abstract fun formatMessage(): Component
}

class StarshipBlockedException(val location: Vec3i, val blockData: BlockState) : StarshipMovementException("Blocked at ${location.x}, ${location.y}, ${location.z} by `$blockData`!") {
	override fun formatMessage(): Component {
		val (x, y, z) = location

		return Component.textOfChildren(
			text("Blocked at ", NamedTextColor.GOLD),
			text(x, NamedTextColor.WHITE),
			text(", ", NamedTextColor.GOLD),
			text(y, NamedTextColor.WHITE),
			text(", ", NamedTextColor.GOLD),
			text(z, NamedTextColor.WHITE),
			text(" by ", NamedTextColor.GOLD),
			PaperAdventure.asAdventure(blockData.block.name),
			text("!", NamedTextColor.GOLD),
		)
			.hoverEvent(text(location.toString()))
			.clickEvent(ClickEvent.copyToClipboard("${location.x} ${location.z} ${location.z}"))
	}
}

class StarshipOutOfBoundsException(message: String) : StarshipMovementException(message) {
	override fun formatMessage(): Component {
		return miniMessage().deserialize(message)
	}
}
