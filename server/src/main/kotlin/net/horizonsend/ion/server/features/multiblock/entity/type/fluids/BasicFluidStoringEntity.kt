package net.horizonsend.ion.server.features.multiblock.entity.type.fluids

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.InternalStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.StorageContainer
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.kyori.adventure.text.Component
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataAdapterContext

/**
 * A simple fluid storing entity with a single main storage
 **/
abstract class BasicFluidStoringEntity(
	manager: MultiblockManager,
	multiblock: Multiblock,
	data: PersistentMultiblockData,
	x: Int,
	y: Int,
	z: Int,
	world: World,
	structureDirection: BlockFace,
	storage: InternalStorage
) : MultiblockEntity(manager, multiblock, x, y, z, world, structureDirection), FluidStoringEntity {
	@Suppress("LeakingThis")
	override val capacities: Array<StorageContainer> = arrayOf(
		loadStoredResource(data, "main", Component.text("Main Storage"), NamespacedKeys.MAIN_STORAGE, storage)
	)

	val mainStorage by lazy { getStorage(NamespacedKeys.MAIN_STORAGE) }

	override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
		storeFluidData(store, adapterContext)
	}
}
