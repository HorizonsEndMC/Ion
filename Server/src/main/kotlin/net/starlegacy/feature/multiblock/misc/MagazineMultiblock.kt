package net.starlegacy.feature.multiblock.misc

import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.Multiblock
import org.bukkit.Material

object MagazineMultiblock : Multiblock() {
	override val name: String = "magazine"
	override val signText = createSignText(
		"&7-=[&c=&a=&b=&7]=-",
		"&0Starship",
		"&0Magazine",
		"&7-=[&c=&a=&b=&7]=-",
	)

	override fun LegacyMultiblockShape.buildStructure() {
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