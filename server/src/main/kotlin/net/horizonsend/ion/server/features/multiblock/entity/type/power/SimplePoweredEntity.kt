package net.horizonsend.ion.server.features.multiblock.entity.type.power

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplayModule
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputsData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

abstract class SimplePoweredEntity(
	data: PersistentMultiblockData,
	multiblock: Multiblock,
	manager: MultiblockManager,
	x: Int,
	y: Int,
	z: Int,
	world: World,
	structureDirection: BlockFace,
	final override val maxPower: Int
) : MultiblockEntity(manager, multiblock, x, y, z, world, structureDirection), PoweredMultiblockEntity, DisplayMultiblockEntity {
	@Suppress("LeakingThis") // Only a reference is needed, max power is provided in the constructor
	final override val powerStorage: PowerStorage = PowerStorage(
		this,
		data.getAdditionalDataOrDefault(NamespacedKeys.POWER, PersistentDataType.INTEGER, 0),
		maxPower
	)

	override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
		savePowerData(store)
	}

	protected fun standardPowerDisplay(entity: SimplePoweredEntity): TextDisplayHandler = DisplayHandlers.newMultiblockSignOverlay(
		entity,
		{ PowerEntityDisplayModule(it, entity) }
	)

	override val inputsData: InputsData = InputsData.Builder(this)
		.addPowerInput(0, -1, 0)
		.build()
}
