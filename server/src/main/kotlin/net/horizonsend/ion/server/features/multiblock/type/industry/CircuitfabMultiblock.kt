package net.horizonsend.ion.server.features.multiblock.type.industry

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.power.IndustryEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace

object CircuitfabMultiblock : Multiblock(), EntityMultiblock<CircuitfabMultiblock.CircuitfabMultiblockEntity>, DisplayNameMultilblock {
	override val name = "circuitfab"

	override val signText = createSignText(
		line1 = "&6Circuitfab",
		line2 = null,
		line3 = null,
		line4 = null
	)

	override val displayName: Component get() = text("Circuitfab")
	override val description: Component get() = text("Manufactures refined circuit components.")

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-2).anyStairs()
				x(-1).ironBlock()
				x(+0).powerInput()
				x(+1).anyGlass()
				x(+2).anyStairs()
			}
			y(+0) {
				x(-2).anyStairs()
				x(-1).extractor()
				x(+0).machineFurnace()
				x(+1).anyGlass()
				x(+2).anyStairs()
			}
		}
		z(+1) {
			y(-1) {
				x(-2).ironBlock()
				x(-1).redstoneBlock()
				x(+0).redstoneBlock()
				x(+1).redstoneBlock()
				x(+2).ironBlock()
			}
			y(+0) {
				x(-2).ironBlock()
				x(-1).endRod()
				x(+0).type(Material.ANVIL)
				x(+1).endRod()
				x(+2).ironBlock()
			}
		}
		z(+2) {
			y(-1) {
				x(-2).anyStairs()
				x(-1).ironBlock()
				x(+0).titaniumBlock()
				x(+1).ironBlock()
				x(+2).anyStairs()
			}
			y(+0) {
				x(-2).anyStairs()
				x(-1).ironBlock()
				x(+0).titaniumBlock()
				x(+1).ironBlock()
				x(+2).anyStairs()
			}
		}
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): CircuitfabMultiblockEntity {
		return CircuitfabMultiblockEntity(data, manager, x, y, z, world, structureDirection)
	}

	class CircuitfabMultiblockEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace
	) : IndustryEntity(data, CircuitfabMultiblock, manager, x, y, z, world, structureDirection, 300_000)
}
