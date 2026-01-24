package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.HeavyNeutralizerWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs

object HeavyNeutralizerStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<HeavyNeutralizerWeaponSubsystem>(),
    DisplayNameMultilblock {
	override val key: String = "heavy_neutralizer"
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): HeavyNeutralizerWeaponSubsystem {
		return HeavyNeutralizerWeaponSubsystem(starship, pos, face)
	}

	override val displayName: Component
		get() = Component.text("Heavy Neutralizer")
	override val description: Component
		get() = Component.text("A Heavy Weapon with a homing projectile. Decreases Ship Capacitor on hit")

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(0) {
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.LEFT, RelativeFace.FORWARD))
				x(0).ironBlock()
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT))
			}
		}
		z(1) {
			y(0) {
				x(1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
				x(0).ironBlock()
				x(-1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
			}
		}
		z(2) {
			y(0) {
				x(1).goldBlock()
				x(0).ironBlock()
				x(-1).goldBlock()
			}
		}
		z(3) {
			y(0) {
				x(1).goldBlock()
				x(0).goldBlock()
				x(-1).goldBlock()
			}
		}
		z(4) {
			y(0) {
				x(1).machineFurnace()
				x(0).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(-1).machineFurnace()
			}
		}
		z(5) {
			y(0) {
				x(1).hopper()
				x(-1).hopper()
			}
		}
		z(6) {
			y(0) {
				x(1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
				x(-1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
			}
		}
	}
}
