package net.horizonsend.ion.server.features.multiblock.entity.type.power

import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataAdapterContext

abstract class SimplePoweredMultiblockEntity(
	data: PersistentMultiblockData,
	manager: MultiblockManager,
	override val multiblock: Multiblock,
	x: Int,
	y: Int,
	z: Int,
	world: World,
	structureDirection: BlockFace,
) : MultiblockEntity(manager, x, y, z, world, structureDirection), PoweredMultiblockEntity {
	override val powerStorage: PowerStorage = this.loadStoredPower(data)
	protected abstract val displayHandler: TextDisplayHandler

	override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
		savePowerData(store)
	}

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
}
