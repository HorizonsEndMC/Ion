package net.horizonsend.ion.server.features.starship.destruction

import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.World

data class SinkInformation(
	val world: World,
	val velocity: Vec3i,

	var iterations: Int
)
