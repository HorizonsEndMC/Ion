package net.horizonsend.ion.server.starships.control

import net.horizonsend.ion.server.starships.Starship
import net.minecraft.server.level.ServerPlayer

@Deprecated("Legacy control methods should be migrated to controller system")
class LegacyController(
	override val starship: Starship,
	override val serverPlayer: ServerPlayer
) : PlayerController {
	override val name: String = "Legacy"
}
