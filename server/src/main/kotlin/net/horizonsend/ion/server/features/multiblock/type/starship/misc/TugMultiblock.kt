package net.horizonsend.ion.server.features.multiblock.type.starship.misc

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.starship.SubsystemMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.misc.TugSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.BlockFace

object TugMultiblock : Multiblock(), SubsystemMultiblock<TugSubsystem> {
	override val name: String = "tug"

	override val signText: Array<Component?> = createSignText(
		Component.text("tugz"),
		null,
		null,
		null,
	)

	override fun MultiblockShape.buildStructure() {
		z(1) {
			y(0) {
				x(1).titaniumBlock()
				x(0).titaniumBlock()
				x(-1).titaniumBlock()
			}
			y(1) {
				x(1).titaniumBlock()
				x(0).titaniumBlock()
				x(-1).titaniumBlock()
			}
			y(2) {
				x(1).titaniumBlock()
				x(0).titaniumBlock()
				x(-1).titaniumBlock()
			}
			y(3) {
				x(1).titaniumBlock()
				x(0).titaniumBlock()
				x(-1).titaniumBlock()
			}
			y(4) {
				x(0).type(Material.CRIMSON_FENCE)
			}
			y(5) {
				x(0).type(Material.CRIMSON_FENCE)
			}
		}
		z(0) {
			y(0) {
				x(0).titaniumBlock()
			}
			y(1) {
				x(0).titaniumBlock()
			}
			y(2) {
				x(0).titaniumBlock()
			}
			y(3) {
				x(0).titaniumBlock()
			}
		}
		z(2) {
			y(0) {
				x(0).titaniumBlock()
			}
			y(1) {
				x(0).titaniumBlock()
			}
			y(2) {
				x(0).titaniumBlock()
			}
			y(3) {
				x(0).titaniumBlock()
			}
		}
	}

	val firePosOffset = Vec3i(0, 5, 1)

	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): TugSubsystem {
		return TugSubsystem(starship, pos, face, this)
	}
}
