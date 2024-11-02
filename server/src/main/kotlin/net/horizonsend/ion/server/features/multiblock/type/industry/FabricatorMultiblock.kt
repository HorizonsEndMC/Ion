package net.horizonsend.ion.server.features.multiblock.type.industry

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplay
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.NewPoweredMultiblock
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign


object FabricatorMultiblock : Multiblock(), NewPoweredMultiblock<FabricatorMultiblock.FabricatorMultiblockEntity> {
	override val name = "fabricator"

	override val signText = createSignText(
		line1 = "&8Fabricator",
		line2 = null,
		line3 = null,
		line4 = null
	)

	override val maxPower = 300_000

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-2).anyStairs()
				x(-1).ironBlock()
				x(+0).wireInputComputer()
				x(+1).ironBlock()
				x(+2).anyStairs()
			}
			y(+0) {
				x(-2).anyStairs()
				x(-1).craftingTable()
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
				x(+0).anvil()
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
		structureDirection: BlockFace
	) : MultiblockEntity(manager, FabricatorMultiblock, x, y, z, world, structureDirection), LegacyMultiblockEntity, PoweredMultiblockEntity, DisplayMultiblockEntity {
		override val powerStorage: PowerStorage = loadStoredPower(data)
		override val multiblock: FabricatorMultiblock = FabricatorMultiblock

		override val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			PowerEntityDisplay(this, +0.0, +0.0, +0.0, 0.5f)
		).register()

		override fun loadFromSign(sign: Sign) {
			migrateLegacyPower(sign)
		}
	}
}
