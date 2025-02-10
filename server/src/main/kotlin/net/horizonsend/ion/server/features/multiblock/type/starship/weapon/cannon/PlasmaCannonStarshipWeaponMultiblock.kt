package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.PlasmaCannonWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.block.BlockFace

object PlasmaCannonStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<PlasmaCannonWeaponSubsystem>(), DisplayNameMultilblock {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): PlasmaCannonWeaponSubsystem {
		return PlasmaCannonWeaponSubsystem(starship, pos, face)
	}

	override val displayName: Component
		get() = text("Plasma Cannon")
	override val description: Component
		get() = text("A high-power starship weapon useful on smaller starships.")

	override fun MultiblockShape.buildStructure() {
		at(+0, +0, +0).sponge()
		at(+0, +0, +1).ironBlock()
		at(+0, +0, +2).furnace()
	}
}
