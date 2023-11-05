package net.horizonsend.ion.server.legacy.starshipweapon.multiblock

import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.starshipweapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.CthulhuBeamSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.block.BlockFace

sealed class CthulhuBeamMutliblock : SignlessStarshipWeaponMultiblock<CthulhuBeamSubsystem>() {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): CthulhuBeamSubsystem {
		val adjustedFace = getAdjustedFace(face)
		return CthulhuBeamSubsystem(starship, pos, adjustedFace)
	}

	protected abstract fun getAdjustedFace(originalFace: BlockFace): BlockFace
}

object CthulhuBeamStarshipWeaponMultiblockTop : CthulhuBeamMutliblock() {
	override fun getAdjustedFace(originalFace: BlockFace): BlockFace = BlockFace.UP

	override fun MultiblockShape.buildStructure() {
		at(+0, +0, +0).noteBlock()
		at(+0, +1, +0).ironBlock()
		at(+0, +2, +0).lodestone()
	}
}

object CthulhuBeamStarshipWeaponMultiblockBottom : CthulhuBeamMutliblock() {
	override fun getAdjustedFace(originalFace: BlockFace): BlockFace = BlockFace.DOWN

	override fun MultiblockShape.buildStructure() {
		at(+0, +0, +0).noteBlock()
		at(+0, -1, +0).ironBlock()
		at(+0, -2, +0).lodestone()
	}
}

object CthulhuBeamStarshipWeaponMultiblockSide : CthulhuBeamMutliblock() {
	override fun getAdjustedFace(originalFace: BlockFace): BlockFace = originalFace

	override fun MultiblockShape.buildStructure() {
		at(+0, +0, +0).noteBlock()
		at(+0, +0, +1).ironBlock()
		at(+0, +0, +2).lodestone()
	}
}
