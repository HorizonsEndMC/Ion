package net.horizonsend.ion.server.features.multiblock.starshipweapon

import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.WeaponSubsystem
import net.starlegacy.util.Vec3i
import org.bukkit.block.BlockFace

interface StarshipWeaponMultiblock<TSubsystem : WeaponSubsystem> {
	fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): TSubsystem
}
