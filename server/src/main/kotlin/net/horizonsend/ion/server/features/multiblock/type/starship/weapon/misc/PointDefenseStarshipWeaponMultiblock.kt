package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.misc

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.PointDefenseSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.block.BlockFace

sealed class PointDefenseStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<PointDefenseSubsystem>(), DisplayNameMultilblock {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): PointDefenseSubsystem {
		val adjustedFace = getAdjustedFace(face)
		return PointDefenseSubsystem(starship, pos, adjustedFace)
	}

	override val description: Component get() = text("Automated weapon system effective against players and entities. Cannot be manually controlled.")

	protected abstract fun getAdjustedFace(originalFace: BlockFace): BlockFace
}

object PointDefenseStarshipWeaponMultiblockTop : PointDefenseStarshipWeaponMultiblock() {
	override fun getAdjustedFace(originalFace: BlockFace): BlockFace = BlockFace.UP
	override val displayName: Component get() = text("Point Defense Turret (Top)")

	override fun MultiblockShape.buildStructure() {
		at(+0, +0, +0).dispenser()
		at(+0, +1, +0).ironBlock()
		at(+0, +2, +0).redstoneLamp()
	}
}

object PointDefenseStarshipWeaponMultiblockBottom : PointDefenseStarshipWeaponMultiblock() {
	override fun getAdjustedFace(originalFace: BlockFace): BlockFace = BlockFace.DOWN
	override val displayName: Component get() = text("Point Defense Turret (Bottom)")

	override fun MultiblockShape.buildStructure() {
		at(+0, +0, +0).dispenser()
		at(+0, -1, +0).ironBlock()
		at(+0, -2, +0).redstoneLamp()
	}
}

object PointDefenseStarshipWeaponMultiblockSide : PointDefenseStarshipWeaponMultiblock() {
	override fun getAdjustedFace(originalFace: BlockFace): BlockFace = originalFace
	override val displayName: Component get() = text("Point Defense Turret (Side)")

	override fun MultiblockShape.buildStructure() {
		at(+0, +0, +0).dispenser()
		at(+0, +0, +1).ironBlock()
		at(+0, +0, +2).redstoneLamp()
	}
}
