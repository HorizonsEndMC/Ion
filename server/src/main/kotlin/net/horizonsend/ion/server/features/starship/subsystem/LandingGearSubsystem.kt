package net.horizonsend.ion.server.features.starship.subsystem

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Observer

class LandingGearSubsystem(starship: ActiveStarship, pos: Vec3i) : StarshipSubsystem(starship, pos) {
	fun setExtended(extended: Boolean) {
		val (x, y, z) = pos

		val observer = starship.serverLevel.world.getBlockAt(x, y, z)
		if (observer.type != Material.OBSERVER) return

		val piston = observer.getRelative(BlockFace.DOWN)
		if (piston.type != Material.PISTON) return

		val belowPiston = piston.getRelative(BlockFace.DOWN)
		// When extending, if no air below, return
		if (belowPiston.type != Material.AIR && extended) return

		// When retracting, if theyre is no block below, return
		if (belowPiston.getRelative(BlockFace.DOWN).type == Material.AIR && !extended) return

		val observerData = observer.blockData as? Observer ?: return
		observerData.isPowered = extended
		observerData.facing = BlockFace.UP

		observer.blockData = observerData
	}

	override fun isIntact(): Boolean {
		val (x, y, z) = pos

		val block = starship.serverLevel.world.getBlockAt(x, y, z)
		val below = block.getRelative(BlockFace.DOWN)

		return block.type == Material.OBSERVER && below.type == Material.PISTON
	}
}
