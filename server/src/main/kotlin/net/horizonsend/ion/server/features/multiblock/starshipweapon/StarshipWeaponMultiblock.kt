package net.horizonsend.ion.server.features.multiblock.starshipweapon

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.WeaponSubsystem
import net.starlegacy.util.Vec3i
import org.bukkit.block.BlockFace

abstract class StarshipWeaponMultiblock<TSubsystem : WeaponSubsystem> : Multiblock() {
	abstract fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): TSubsystem
}
