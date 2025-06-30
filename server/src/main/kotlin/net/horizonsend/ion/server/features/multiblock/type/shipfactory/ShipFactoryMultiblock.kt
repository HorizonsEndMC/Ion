package net.horizonsend.ion.server.features.multiblock.type.shipfactory

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplayModule
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.economy.RemotePipeMultiblock.InventoryReference
import net.horizonsend.ion.server.features.transport.inputs.InputsData
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

object ShipFactoryMultiblock : AbstractShipFactoryMultiblock<ShipFactoryMultiblock.StandardShipFactoryEntity>() {
	override val signText = createSignText(
		line1 = "&1Ship Factory",
		line2 = null,
		line3 = null,
		line4 = null
	)

	override val blockPlacementsPerTick: Int = 50

	override val displayName: Component get() = text("Ship Factory")
	override val description: Component get() = text("Print starships and other structures with materials and credits.")

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(+0).ironBlock()
				x(+1).ironBlock()
			}

			y(+0) {
				x(+0).furnace()
				x(+1).anyPipedInventory()
			}
		}
	}

	fun getStorage(sign: Sign): Inventory {
		val direction = sign.getFacing().oppositeFace
		return (sign.block.getRelative(direction).getRelative(direction.rightFace).state as InventoryHolder).inventory
	}

	override fun createEntity(
		manager: MultiblockManager,
		data: PersistentMultiblockData,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace,
	): StandardShipFactoryEntity {
		return StandardShipFactoryEntity(data, manager, x, y, z, world, structureDirection)
	}

	class StandardShipFactoryEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace
	) : ShipFactoryEntity(data, ShipFactoryMultiblock, manager, world, x, y, z, structureDirection) {
		override val inputsData: InputsData = none()
		override val guiTitle: String = "Ship Factory"

		override val displayHandler: TextDisplayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			{ StatusDisplayModule(it, statusManager) }
		).register()

		private val inventoryOffset = Vec3i(1, 0, 0)

		override fun getInventories(): Set<InventoryReference> {
			val transportManager = manager.getTransportManager()
			val itemCache = transportManager.itemPipeManager.cache

			val inv = itemCache.getInventory(toBlockKey(getPosRelative(right = inventoryOffset.x, up = inventoryOffset.y, forward = inventoryOffset.z))) ?: return setOf()
			return setOf(InventoryReference.StandardInventoryReference(inv))
		}
	}
}
