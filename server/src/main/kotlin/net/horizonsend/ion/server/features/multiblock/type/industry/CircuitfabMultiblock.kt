package net.horizonsend.ion.server.features.multiblock.type.industry

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplay
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.SimplePoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.NewPoweredMultiblock
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign

object CircuitfabMultiblock : Multiblock(), NewPoweredMultiblock<CircuitfabMultiblock.CircuitfabMultiblockEntity> {
	override val name = "circuitfab"

	override val signText = createSignText(
		line1 = "&6Circuitfab",
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
				x(+1).anyGlass()
				x(+2).anyStairs()
			}
			y(+0) {
				x(-2).anyStairs()
				x(-1).craftingTable()
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
				x(+0).anvil()
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
	) : SimplePoweredMultiblockEntity(data, manager, CircuitfabMultiblock, x, y, z, world, structureDirection), LegacyMultiblockEntity {
		override val poweredMultiblock: CircuitfabMultiblock = CircuitfabMultiblock
		override val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			PowerEntityDisplay(this, +0.0, +0.0, +0.0, 0.5f)
		).register()

		override fun loadFromSign(sign: Sign) {
			migrateLegacyPower(sign)
		}
	}
}
