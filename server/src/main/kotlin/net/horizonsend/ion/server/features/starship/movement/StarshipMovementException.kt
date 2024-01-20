package net.horizonsend.ion.server.features.starship.movement

import io.papermc.paper.adventure.PaperAdventure
import net.horizonsend.ion.common.utils.miscellaneous.ComponentMessageException
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.minecraft.world.level.block.state.BlockState

abstract class StarshipMovementException(override val message: String) : ComponentMessageException(message)

class StarshipBlockedException(val location: Vec3i, val blockData: BlockState) : StarshipMovementException("Blocked at ${location.x}, ${location.y}, ${location.z} by `$blockData`!") {
	override fun formatMessage(): Component {
		val (x, y, z) = location

		return template(
			text("Blocked at {0}, {1}, {2}, by {3}!", HEColorScheme.HE_LIGHT_ORANGE),
			x,
			y,
			z,
			PaperAdventure.asAdventure(blockData.block.name).color(WHITE)
		)
			.hoverEvent(location.toComponent())
			.clickEvent(ClickEvent.copyToClipboard("${location.x} ${location.z} ${location.z}"))
	}
}

class StarshipOutOfBoundsException(message: String) : StarshipMovementException(message) {
	override fun formatMessage(): Component {
		return miniMessage().deserialize(message)
	}
}
