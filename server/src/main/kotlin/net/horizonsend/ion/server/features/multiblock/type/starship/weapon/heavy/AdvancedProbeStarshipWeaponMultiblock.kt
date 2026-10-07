package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.AdvancedProbeWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.block.BlockFace

object AdvancedProbeStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<AdvancedProbeWeaponSubsystem>(), DisplayNameMultilblock {
	override val key: String = "advanced_probe"
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): AdvancedProbeWeaponSubsystem {
		return AdvancedProbeWeaponSubsystem(starship, pos, face)
	}

	override val displayName: Component
		get() = text("Advanced Probe Launcher")
	override val description: Component
		get() = text("Fires off a probe that displays nearby players and their distance, or scans for nearby signatures.")

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(0) {
				x(0).powerInput()
				x(+1).anyStairs()
			}
		}
		z(1) {
			y(0) {
				x(0).ironBlock()
				x(+1).sponge()
			}
		}
		z(2) {
			y(0) {
				x(0).ironBlock()
				x(+1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
			}
		}
		z(3) {
			y(0) {
				x(0).dispenser()
				x(+1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
			}
		}
		z(4) {
			y(0) {
				x(+1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.END_ROD.createBlockData()))
			}
		}
		z(5) {
			y(0) {
				x(+1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.END_ROD.createBlockData()))
			}
		}
	}
}


object AdvancedProbeStarshipWeaponMultiblockMirrored : SignlessStarshipWeaponMultiblock<AdvancedProbeWeaponSubsystem>(), DisplayNameMultilblock {
	override val key: String = "advanced_probe"
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): AdvancedProbeWeaponSubsystem {
		return AdvancedProbeWeaponSubsystem(starship, pos, face)
	}

	override val displayName: Component
		get() = text("Advanced Probe Launcher (Mirrored)")
	override val description: Component
		get() = text("Fires off a probe that displays nearby players and their distance, or scans for nearby signatures.")

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(0) {
				x(0).powerInput()
				x(-1).anyStairs()
			}
		}
		z(1) {
			y(0) {
				x(0).ironBlock()
				x(-1).sponge()
			}
		}
		z(2) {
			y(0) {
				x(0).ironBlock()
				x(-1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
			}
		}
		z(3) {
			y(0) {
				x(0).dispenser()
				x(-1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
			}
		}
		z(4) {
			y(0) {
				x(-1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.END_ROD.createBlockData()))
			}
		}
		z(5) {
			y(0) {
				x(-1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.END_ROD.createBlockData()))
			}
		}
	}
}
// Vertical advanced probe launchers

object VerticalAdvancedProbeStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<AdvancedProbeWeaponSubsystem>(), DisplayNameMultilblock {
	override val key: String = "advanced_probe"
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): AdvancedProbeWeaponSubsystem {
		return AdvancedProbeWeaponSubsystem(starship, pos, face)
	}

	override val displayName: Component
		get() = text("Vertical Advanced Probe Launcher")
	override val description: Component
		get() = text("Fires off a probe that displays nearby players and their distance, or scans for nearby signatures.")

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(0) {
				x(0).powerInput()
			}
		}
		z(1) {
			y(0) {
				x(0).ironBlock()
			}
		}
		z(2) {
			y(0) {
				x(0).ironBlock()
			}
		}
		z(3) {
			y(0) {
				x(0).dispenser()
			}
		}
		z(0){
			y(1){
				x(0).anyStairs()
			}
		}
		z(1){
			y(1){
				x(0).sponge()
			}
		}
		z(2){
			y(1){
				x(0).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
			}
		}
		z(3){
			y(1){
				x(0).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
			}
		}
		z(4){
			y(1){
				x(0).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.END_ROD.createBlockData()))
			}
		}
		z(5){
			y(1){
				x(0).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.END_ROD.createBlockData()))
			}
		}
	}
}

object VerticalAdvancedProbeStarshipWeaponMultiblockMirrored : SignlessStarshipWeaponMultiblock<AdvancedProbeWeaponSubsystem>(), DisplayNameMultilblock {
	override val key: String = "advanced_probe"
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): AdvancedProbeWeaponSubsystem {
		return AdvancedProbeWeaponSubsystem(starship, pos, face)
	}

	override val displayName: Component
		get() = text("Vertical Advanced Probe Launcher(Mirrored)")
	override val description: Component
		get() = text("Fires off a probe that displays nearby players and their distance, or scans for nearby signatures.")

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(0) {
				x(0).powerInput()
			}
		}
		z(1) {
			y(0) {
				x(0).ironBlock()
			}
		}
		z(2) {
			y(0) {
				x(0).ironBlock()
			}
		}
		z(3) {
			y(0) {
				x(0).dispenser()
			}
		}
		z(0){
			y(-1){
				x(0).anyStairs()
			}
		}
		z(1){
			y(-1){
				x(0).sponge()
			}
		}
		z(2){
			y(-1){
				x(0).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
			}
		}
		z(3){
			y(-1){
				x(0).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
			}
		}
		z(4){
			y(-1){
				x(0).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.END_ROD.createBlockData()))
			}
		}
		z(5){
			y(-1){
				x(0).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.END_ROD.createBlockData()))
			}
		}
	}
}
