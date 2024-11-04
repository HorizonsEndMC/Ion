package net.horizonsend.ion.server.features.multiblock.entity.type.power

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplay
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataAdapterContext

abstract class SimplePoweredEntity(
	data: PersistentMultiblockData,
	multiblock: Multiblock,
	manager: MultiblockManager,
	x: Int,
	y: Int,
	z: Int,
	world: World,
	structureDirection: BlockFace,
) : MultiblockEntity(manager, multiblock, x, y, z, world, structureDirection), PoweredMultiblockEntity, DisplayMultiblockEntity {
	override val powerStorage: PowerStorage = this.loadStoredPower(data)

	override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
		savePowerData(store)
	}

	protected fun standardPowerDisplay(entity: SimplePoweredEntity): TextDisplayHandler = DisplayHandlers.newMultiblockSignOverlay(
		entity,
		PowerEntityDisplay(entity, +0.0, +0.0, +0.0, 0.5f)
	).register()
}
