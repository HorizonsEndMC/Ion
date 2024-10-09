package net.horizonsend.ion.server.features.multiblock.type.power.storage

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplay
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.NewPoweredMultiblock
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataAdapterContext

object PowerCellMultiblock : Multiblock(), NewPoweredMultiblock<PowerCellMultiblock.PowerCellEntity> {
	override val name = "powercell"

	override val signText = createSignText(
		line1 = "&6Power &8Cell",
		line2 = "------",
		line3 = null,
		line4 = "&cCompact Power"
	)

	override val maxPower = 50_000

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(+0) {
				x(-1).anyGlassPane()
				x(+0).wireInputComputer()
				x(+1).anyGlassPane()
			}
		}

		z(+1) {
			y(+0) {
				x(-1).anyGlassPane()
				x(+0).redstoneBlock()
				x(+1).anyGlassPane()
			}
		}
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): PowerCellEntity {
		return PowerCellEntity(
			data,
			manager,
			this,
			x,
			y,
			z,
			world,
			structureDirection
		)
	}

	class PowerCellEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		override val multiblock: PowerCellMultiblock,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace
	) : MultiblockEntity(manager, multiblock, x, y, z, world, structureDirection), PoweredMultiblockEntity {
		override val storage: PowerStorage = loadStoredPower(data)

		private val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			PowerEntityDisplay(this, +0.0, +0.0, +0.0, 0.5f)
		).register()

		override fun onLoad() {
			displayHandler.update()
		}

		override fun onUnload() {
			displayHandler.remove()
		}

		override fun handleRemoval() {
			displayHandler.remove()
		}

		override fun displaceAdditional(movement: StarshipMovement) {
			displayHandler.displace(movement)
		}

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			savePowerData(store)
		}

	}
}
