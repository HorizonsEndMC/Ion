package net.horizonsend.ion.server.features.multiblock.type.misc

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material

abstract class AbstractMagazineMultiblock : Multiblock(), DisplayNameMultilblock {
	override val name: String = "magazine"
	override val signText = createSignText(
		"&7-=[&c=&a=&b=&7]=-",
		"&0Starship",
		"&0Magazine",
		"&7-=[&c=&a=&b=&7]=-"
	)
	abstract val mirrored: Boolean

	override val description: Component get() = text("Stores ammunition for starship weapons.")
}

object MagazineMultiblock : AbstractMagazineMultiblock() {
	override val displayName: Component get() = text("Magazine")
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
	override val displayName: Component get() = text("Magazine (Mirrored)")
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
