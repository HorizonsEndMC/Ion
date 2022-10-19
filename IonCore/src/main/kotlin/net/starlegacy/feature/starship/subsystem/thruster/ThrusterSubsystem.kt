package net.starlegacy.feature.starship.subsystem.thruster

import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.DirectionalSubsystem
import net.starlegacy.feature.starship.subsystem.StarshipSubsystem
import net.starlegacy.util.Vec3i
import org.bukkit.block.BlockFace

/** thruster subsystem representing a single thruster, the facing direction being forward */
class ThrusterSubsystem(starship: ActiveStarship, pos: Vec3i, override var face: BlockFace, val type: ThrusterType) :
	StarshipSubsystem(starship, pos), DirectionalSubsystem {
	override fun isIntact(): Boolean = type.matchesStructure(starship, pos.x, pos.y, pos.z, face)

	// TODO : remove automatically when the block gets destroyed
}
