package net.horizonsend.ion.server.features.multiblock.type.misc

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import org.bukkit.Material

abstract class AbstractMagazineMultiblock : Multiblock() {
	override val name: String = "magazine"
	override val signText = createSignText(
		"&7-=[&c=&a=&b=&7]=-",
		"&0Starship",
		"&0Magazine",
		"&7-=[&c=&a=&b=&7]=-"
	)
	abstract val mirrored: Boolean;
}

object MagazineMultiblock : AbstractMagazineMultiblock() {
	override val mirrored = false
	override fun MultiblockShape.buildStructure() {
		y(+0) {
			z(+0) {
				x(+0).type(Material.END_PORTAL_FRAME)
				x(+1).anyPipedInventory()
			}

			z(+1) {
				x(+0).ironBlock()
			}
		}
	}
}

object MagazineMultiblockMirrored : AbstractMagazineMultiblock() {
	override val mirrored = true
	override fun MultiblockShape.buildStructure() {
		y(+0) {
			z(+0) {
				x(-1).anyPipedInventory()
				x(+0).type(Material.END_PORTAL_FRAME)
			}

			z(+1) {
				x(+0).ironBlock()
			}
		}
	}
}
