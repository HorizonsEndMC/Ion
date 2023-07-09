package net.starlegacy.feature.multiblock.starshipweapon.misc

import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.starshipweapon.SignlessStarshipWeaponMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.primary.PointDefenseSubsystem
import net.horizonsend.ion.server.miscellaneous.Vec3i
import org.bukkit.block.BlockFace

sealed class PointDefenseStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<PointDefenseSubsystem>() {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): PointDefenseSubsystem {
		val adjustedFace = getAdjustedFace(face)
		return PointDefenseSubsystem(starship, pos, adjustedFace)
	}

	protected abstract fun getAdjustedFace(originalFace: BlockFace): BlockFace
}

object PointDefenseStarshipWeaponMultiblockTop : PointDefenseStarshipWeaponMultiblock() {
	override fun getAdjustedFace(originalFace: BlockFace): BlockFace = BlockFace.UP

	override fun LegacyMultiblockShape.buildStructure() {
		at(+0, +0, +0).dispenser()
		at(+0, +1, +0).ironBlock()
		at(+0, +2, +0).redstoneLamp()
	}
}

object PointDefenseStarshipWeaponMultiblockBottom : PointDefenseStarshipWeaponMultiblock() {
	override fun getAdjustedFace(originalFace: BlockFace): BlockFace = BlockFace.DOWN

	override fun LegacyMultiblockShape.buildStructure() {
		at(+0, +0, +0).dispenser()
		at(+0, -1, +0).ironBlock()
		at(+0, -2, +0).redstoneLamp()
	}
}

object PointDefenseStarshipWeaponMultiblockSide : PointDefenseStarshipWeaponMultiblock() {
	override fun getAdjustedFace(originalFace: BlockFace): BlockFace = originalFace

	override fun LegacyMultiblockShape.buildStructure() {
		at(+0, +0, +0).dispenser()
		at(+0, +0, +1).ironBlock()
		at(+0, +0, +2).redstoneLamp()
	}
}
