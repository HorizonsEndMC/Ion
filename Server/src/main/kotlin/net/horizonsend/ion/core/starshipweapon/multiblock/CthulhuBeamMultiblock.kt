package net.horizonsend.ion.core.starshipweapon.multiblock

import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.starshipweapon.SignlessStarshipWeaponMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.horizonsend.ion.core.starshipweapon.primary.CthulhuBeamSubsystem
import net.starlegacy.util.Vec3i
import org.bukkit.block.BlockFace

sealed class CthulhuBeamMutliblock : SignlessStarshipWeaponMultiblock<CthulhuBeamSubsystem>() {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): CthulhuBeamSubsystem {
		val adjustedFace = getAdjustedFace(face)
		return CthulhuBeamSubsystem(starship, pos, adjustedFace)
	}

	protected abstract fun getAdjustedFace(originalFace: BlockFace): BlockFace
}

object EnderCrystalStarshipWeaponMultiblockTop : CthulhuBeamMutliblock() {
	override fun getAdjustedFace(originalFace: BlockFace): BlockFace = BlockFace.UP

	override fun LegacyMultiblockShape.buildStructure() {
		at(+0, +0, +0).noteBlock()
		at(+0, +1, +0).ironBlock()
		at(+0, +2, +0).lodestone()
	}
}

object CthulhuBeamMultiblockBottom : CthulhuBeamMutliblock() {
	override fun getAdjustedFace(originalFace: BlockFace): BlockFace = BlockFace.DOWN

	override fun LegacyMultiblockShape.buildStructure() {
		at(+0, +0, +0).noteBlock()
		at(+0, -1, +0).ironBlock()
		at(+0, -2, +0).lodestone()
	}
}

object CthulhuBeamMultiblockSide : CthulhuBeamMutliblock() {
	override fun getAdjustedFace(originalFace: BlockFace): BlockFace = originalFace

	override fun LegacyMultiblockShape.buildStructure() {
		at(+0, +0, +0).noteBlock()
		at(+0, +0, +1).ironBlock()
		at(+0, +0, +2).lodestone()
	}
}