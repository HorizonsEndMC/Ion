package net.horizonsend.ion.server.features.multiblock.entity.type.power

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplay
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.transport.util.NetworkType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
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
	override val powerInputOffsets: Array<Vec3i> = arrayOf(Vec3i(0, -1, 0))

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
		PowerEntityDisplay(entity, +0.0, +0.0, +0.0, 0.5f)
	).register()

	override fun onLoad() {
		registerInputs(NetworkType.POWER, getPowerInputLocations())
	}

	override fun handleRemoval() {
		releaseInputs(NetworkType.POWER, getPowerInputLocations())
	}

	override fun onUnload() {
		releaseInputs(NetworkType.POWER, getPowerInputLocations())
	}
}
