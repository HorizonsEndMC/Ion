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


object CompressorMultiblock : Multiblock(), EntityMultiblock<CompressorMultiblock.CompressorMultiblockEntity>, DisplayNameMultilblock {
	override val name = "compressor"

	override val signText = createSignText(
		line1 = "&6Compressor",
		line2 = null,
		line3 = null,
		line4 = null
	)

	override val displayName: Component get() = text("Compressor")
	override val description: Component get() = text("Applies a high compression force to materials to refine them.")

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).ironBlock()
				x(+0).powerInput()
				x(+1).ironBlock()
			}
			y(+0) {
				x(-1).anyStairs()
				x(+0).machineFurnace()
				x(+1).anyStairs()
			}
		}
		z(+1) {
			y(-1) {
				x(-1).netheriteBlock()
				x(+0).endRod()
				x(+1).netheriteBlock()
			}
			y(+0) {
				x(-1).ironBlock()
				x(+0).extractor()
				x(+1).ironBlock()
			}
		}
		z(+2) {
			y(-1) {
				x(-1).netheriteBlock()
				x(+0).endRod()
				x(+1).netheriteBlock()
			}
			y(+0) {
				x(-1).anyGlassPane()
				x(+0).pistonBase()
				x(+1).anyGlassPane()
			}
		}
		z(+3) {
			y(-1) {
				x(-1).anyGlass()
				x(+0).sponge()
				x(+1).anyGlass()
			}
			y(+0) {
				x(-1).anyGlass()
				x(+0).lodestone()
				x(+1).anyGlass()
			}
		}
		z(+4) {
			y(-1) {
				x(-1).netheriteBlock()
				x(+0).endRod()
				x(+1).netheriteBlock()
			}
			y(+0) {
				x(-1).anyGlassPane()
				x(+0).pistonBase()
				x(+1).anyGlassPane()
			}
		}
		z(+5) {
			y(-1) {
				x(-1).netheriteBlock()
				x(+0).endRod()
				x(+1).netheriteBlock()
			}
			y(+0) {
				x(-1).ironBlock()
				x(+0).ironBlock()
				x(+1).ironBlock()
			}
		}
		z(+6) {
			y(-1) {
				x(-1).ironBlock()
				x(+0).sponge()
				x(+1).ironBlock()
			}
			y(+0) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}
		}
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): CompressorMultiblockEntity {
		return CompressorMultiblockEntity(data, manager, x, y, z, world, structureDirection)
	}

	class CompressorMultiblockEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureFace: BlockFace,
	) : IndustryEntity(data, CompressorMultiblock, manager, x, y, z, world, structureFace, 300_000)
}
