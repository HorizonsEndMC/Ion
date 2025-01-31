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
import org.bukkit.World
import org.bukkit.block.BlockFace


object CentrifugeMultiblock : Multiblock(), EntityMultiblock<CentrifugeMultiblock.CentrifugeMultiblockEntity>, DisplayNameMultilblock {
	override val name = "centrifuge"

	override val signText = createSignText(
		line1 = "&6Centrifuge",
		line2 = null,
		line3 = null,
		line4 = null
	)

	override val displayName: Component get() = text("Centrifuge")
	override val description: Component get() = text("Applies centrifugal force to materials to refine them.")

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-2).steelBlock()
				x(-1).anyGlassPane()
				x(+0).powerInput()
				x(+1).anyGlassPane()
				x(+2).steelBlock()
			}
			y(+0) {
				x(-2).steelBlock()
				x(-1).anyGlassPane()
				x(+0).machineFurnace()
				x(+1).anyGlassPane()
				x(+2).steelBlock()
			}
		}
		z(+1) {
			y(-1) {
				x(-2).anyGlassPane()
				x(-1).anyCopperVariant()
				x(+0).endRod()
				x(+1).anyCopperVariant()
				x(+2).anyGlassPane()
			}
			y(+0) {
				x(-2).anyGlassPane()
				x(-1).anyStairs()
				x(+0).anyStairs()
				x(+1).anyStairs()
				x(+2).anyGlassPane()
			}
		}
		z(+2) {
			y(-1) {
				x(-2).ironBlock()
				x(-1).anyCopperVariant()
				x(+0).sponge()
				x(+1).anyCopperVariant()
				x(+2).ironBlock()
			}
			y(+0) {
				x(-2).ironBlock()
				x(-1).anyStairs()
				x(+0).sculkCatalyst()
				x(+1).anyStairs()
				x(+2).ironBlock()
			}
		}
		z(+3) {
			y(-1) {
				x(-2).anyGlassPane()
				x(-1).anyCopperVariant()
				x(+0).endRod()
				x(+1).anyCopperVariant()
				x(+2).anyGlassPane()
			}
			y(+0) {
				x(-2).anyGlassPane()
				x(-1).anyStairs()
				x(+0).anyStairs()
				x(+1).anyStairs()
				x(+2).anyGlassPane()
			}
		}
		z(+4) {
			y(-1) {
				x(-2).steelBlock()
				x(-1).anyGlassPane()
				x(+0).sponge()
				x(+1).anyGlassPane()
				x(+2).steelBlock()
			}
			y(+0) {
				x(-2).steelBlock()
				x(-1).anyGlassPane()
				x(+0).ironBlock()
				x(+1).anyGlassPane()
				x(+2).steelBlock()
			}
		}
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): CentrifugeMultiblockEntity {
		return CentrifugeMultiblockEntity(data, manager, x, y, z, world, structureDirection)
	}

	class CentrifugeMultiblockEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace
	) : IndustryEntity(data, CentrifugeMultiblock, manager, x, y, z, world, structureDirection, 300_000)
}
