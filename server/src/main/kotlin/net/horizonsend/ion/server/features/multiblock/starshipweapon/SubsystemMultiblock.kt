package net.horizonsend.ion.server.features.multiblock.starshipweapon

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.StarshipSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.block.BlockFace

interface SubsystemMultiblock<TSubsystem : StarshipSubsystem> {
	fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): TSubsystem
}
