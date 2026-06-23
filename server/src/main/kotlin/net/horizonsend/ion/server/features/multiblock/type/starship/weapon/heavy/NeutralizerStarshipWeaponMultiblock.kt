package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.NeutralizerWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.block.BlockFace
import org.bukkit.Material

object NeutralizerStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<NeutralizerWeaponSubsystem>(), DisplayNameMultilblock {
	override val key: String = "neutralizer"
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): NeutralizerWeaponSubsystem {
		return NeutralizerWeaponSubsystem(starship, pos, face)
	}

	override val displayName: Component
		get() = text("Neutralizer")
	override val description: Component
		get() = text("A Heavy Weapon with a homing projectile. Decreases Ship Capacitor on hit")

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(0) {
				x(1).anyGlass()
				x(0).goldBlock()
				x(-1).anyGlass()
			}
			y(-1) {
				x(0).anyGlass()
			}
			y(1) {
				x(0).anyGlass()
			}
		}
		z(1) {
			y(0) {
				x(1).anyGlass()
				x(0).goldBlock()
				x(-1).anyGlass()
			}
			y(-1) {
				x(0).anyGlass()
			}
			y(1) {
				x(0).anyGlass()
			}
		}
		z(2) {
			y(0) {
				x(1).anyGlass()
				x(0).goldBlock()
				x(-1).anyGlass()
			}
			y(-1) {
				x(0).anyGlass()
			}
			y(1) {
				x(0).anyGlass()
			}
		}
		z(3) {
			y(0) {
				x(1).anyGlass()
				x(0).goldBlock()
				x(-1).anyGlass()
			}
			y(-1) {
				x(0).anyGlass()
			}
			y(1) {
				x(0).anyGlass()
			}
		}
		z(4) {
			y(0) {
				x(1).anyGlass()
				x(0).goldBlock()
				x(-1).anyGlass()
			}
			y(-1) {
				x(0).anyGlass()
			}
			y(1) {
				x(0).anyGlass()
			}
		}
		z(5) {
			y(0) {
				x(1).anyGlass()
				x(0).goldBlock()
				x(-1).anyGlass()
			}
			y(-1) {
				x(0).anyGlass()
			}
			y(1) {
				x(0).anyGlass()
			}
		}
		z(6) {
			y(0) {
				x(1).anyGlass()
				x(0).machineFurnace()
				x(-1).anyGlass()
			}
			y(-1) {
				x(0).anyGlass()
			}
			y(1) {
				x(0).anyGlass()
			}
		}
		z(7) {
			y(0) {
				x(0).grindstone()
			}
		}
	}
}
