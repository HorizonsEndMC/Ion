package net.horizonsend.ion.server.features.starship.subsystem.thruster

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.StarshipSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.block.BlockFace

/** thruster subsystem representing a single thruster, the facing direction being forward */
class ThrusterSubsystem(starship: ActiveStarship, pos: Vec3i, override var face: BlockFace, val type: ThrusterType) :
	StarshipSubsystem(starship, pos), DirectionalSubsystem {
	var lastIonTurretLimited: Long = 0L

	override fun isIntact(): Boolean = type.matchesStructure(starship, pos.x, pos.y, pos.z, face)

	// TODO : remove automatically when the block gets destroyed
}
