package net.horizonsend.ion.server.features.multiblock.type.misc

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import org.bukkit.Material

object FuelTankMultiblock : Multiblock() {
	override val name: String = "tank"
	override val signText = createSignText(
			"&7-=[&c==&a==&b==&7]=-",
			"&0Supercapital",
			"&6Fuel Tank",
			"&7-=[&c==&a==&b==&7]=-"
	)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-2).anyStairs()
				x(-1).ironBlock()
				x(+0).anyPipedInventory()
				x(+1).ironBlock()
				x(+2).anyStairs()
			}
			y(+0) {
				x(-2).anyStairs()
				x(-1).type(Material.END_PORTAL_FRAME)
				x(+0).anyGlass()
				x(+1).type(Material.END_PORTAL_FRAME)
				x(+2).anyStairs()
			}
		}
		z(+1) {
			y(-1) {
				x(-2).titaniumBlock()
				x(-1).titaniumBlock()
				x(+0).redstoneBlock()
				x(+1).titaniumBlock()
				x(+2).titaniumBlock()
			}
			y(+0) {
				x(-2).titaniumBlock()
				x(-1).titaniumBlock()
				x(+0).redstoneBlock()
				x(+1).titaniumBlock()
				x(+2).titaniumBlock()
			}
		}
		z(+2) {
			y(-1) {
				x(-2).anyStairs()
				x(-1).ironBlock()
				x(+0).anyGlass()
				x(+1).ironBlock()
				x(+2).anyStairs()
			}
			y(+0) {
				x(-2).anyStairs()
				x(-1).ironBlock()
				x(+0).anyGlass()
				x(+1).ironBlock()
				x(+2).anyStairs()
			}
		}
	}
}
