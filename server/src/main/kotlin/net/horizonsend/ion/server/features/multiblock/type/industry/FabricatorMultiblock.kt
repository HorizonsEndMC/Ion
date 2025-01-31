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


object FabricatorMultiblock : Multiblock(), EntityMultiblock<FabricatorMultiblock.FabricatorMultiblockEntity>, DisplayNameMultilblock {
	override val name = "fabricator"

	override val signText = createSignText(
		line1 = "&8Fabricator",
		line2 = null,
		line3 = null,
		line4 = null
	)

	override val displayName: Component
		get() = text("Fabricator")
	override val description: Component
		get() = text("Refines materials that are too advanced for simple crafting.")

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-2).anyStairs()
				x(-1).ironBlock()
				x(+0).powerInput()
				x(+1).ironBlock()
				x(+2).anyStairs()
			}
			y(+0) {
				x(-2).anyStairs()
				x(-1).extractor()
				x(+0).machineFurnace()
				x(+1).ironBlock()
				x(+2).anyStairs()
			}
		}
		z(+1) {
			y(-1) {
				x(-2).ironBlock()
				x(-1).aluminumBlock()
				x(+0).aluminumBlock()
				x(+1).aluminumBlock()
				x(+2).ironBlock()
			}
			y(+0) {
				x(-2).anyGlassPane()
				x(+2).anyGlassPane()
			}
		}
		z(+2) {
			y(-1) {
				x(-2).ironBlock()
				x(-1).aluminumBlock()
				x(+0).sculkCatalyst()
				x(+1).aluminumBlock()
				x(+2).ironBlock()
			}
			y(+0) {
				x(-2).anyGlass()
				x(-1).endRod()
				x(+0).type(Material.ANVIL)
				x(+1).endRod()
				x(+2).anyGlass()
			}
		}
		z(+3) {
			y(-1) {
				x(-2).ironBlock()
				x(-1).aluminumBlock()
				x(+0).aluminumBlock()
				x(+1).aluminumBlock()
				x(+2).ironBlock()
			}
			y(+0) {
				x(-2).anyGlassPane()
				x(+2).anyGlassPane()
			}
		}
		z(+4) {
			y(-1) {
				x(-2).anyStairs()
				x(-1).ironBlock()
				x(+0).ironBlock()
				x(+1).ironBlock()
				x(+2).anyStairs()
			}
			y(+0) {
				x(-2).anyStairs()
				x(-1).anyGlass()
				x(+0).anyGlass()
				x(+1).anyGlass()
				x(+2).anyStairs()
			}
		}
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): FabricatorMultiblockEntity {
		return FabricatorMultiblockEntity(data, manager, x, y, z, world, structureDirection)
	}

	class FabricatorMultiblockEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureFace: BlockFace
	) : IndustryEntity(data, FabricatorMultiblock, manager, x, y, z, world, structureFace, 300_000)
}
