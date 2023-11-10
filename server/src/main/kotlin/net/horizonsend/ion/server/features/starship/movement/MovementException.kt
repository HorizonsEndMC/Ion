package net.horizonsend.ion.server.features.starship.movement

import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.minecraft.world.level.block.state.BlockState

abstract class MovementException(override val message: String) : Throwable() {
	abstract fun formatMessage(): Component
}

class BlockedException(val location: Vec3i, val blockData: BlockState) : MovementException("Blocked at ${location.x}, ${location.y}, ${location.z} by `$blockData`!") {
	override fun formatMessage(): Component {
		val (x, y, z) = location

		return Component.textOfChildren(
			text("Blocked at ", NamedTextColor.GOLD),
			text(x, NamedTextColor.WHITE),
			text(", ", NamedTextColor.GOLD),
			text(y, NamedTextColor.WHITE),
			text(", ", NamedTextColor.GOLD),
			text(z, NamedTextColor.WHITE),
			text(" by `", NamedTextColor.GOLD),
			text(blockData.toString(), NamedTextColor.WHITE),
			text("`!", NamedTextColor.GOLD),
		).hoverEvent(text(location.toString()))
	}
}

class OutOfBoundsException(message: String) : MovementException(message) {
	override fun formatMessage(): Component {
		return miniMessage().deserialize(message)
	}
}
