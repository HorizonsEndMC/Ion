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

	override val displayName: Component
		get() = text("Point Defense Turret")
	override val description: Component
		get() = text("An automatic-only weapon effective against players and entities.")

	protected abstract fun getAdjustedFace(originalFace: BlockFace): BlockFace
}

object PointDefenseStarshipWeaponMultiblockTop : PointDefenseStarshipWeaponMultiblock() {
	override fun getAdjustedFace(originalFace: BlockFace): BlockFace = BlockFace.UP

	override fun MultiblockShape.buildStructure() {
		at(+0, +0, +0).dispenser()
		at(+0, +1, +0).ironBlock()
		at(+0, +2, +0).redstoneLamp()
	}
}

object PointDefenseStarshipWeaponMultiblockBottom : PointDefenseStarshipWeaponMultiblock() {
	override fun getAdjustedFace(originalFace: BlockFace): BlockFace = BlockFace.DOWN

	override fun MultiblockShape.buildStructure() {
		at(+0, +0, +0).dispenser()
		at(+0, -1, +0).ironBlock()
		at(+0, -2, +0).redstoneLamp()
	}
}

object PointDefenseStarshipWeaponMultiblockSide : PointDefenseStarshipWeaponMultiblock() {
	override fun getAdjustedFace(originalFace: BlockFace): BlockFace = originalFace

	override fun MultiblockShape.buildStructure() {
		at(+0, +0, +0).dispenser()
		at(+0, +0, +1).ironBlock()
		at(+0, +0, +2).redstoneLamp()
	}
}
