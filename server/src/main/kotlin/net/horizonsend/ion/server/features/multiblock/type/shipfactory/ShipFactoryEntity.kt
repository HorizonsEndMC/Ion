package net.horizonsend.ion.server.features.multiblock.type.shipfactory

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.UserManagedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.StatusTickedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.kyori.adventure.text.Component
import net.minecraft.world.level.block.Rotation
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataAdapterContext

abstract class ShipFactoryEntity(
	data: PersistentMultiblockData,
	multiblock: AbstractShipFactoryMultiblock<*>,
	manager: MultiblockManager,
	world: World,
	x: Int,
	y: Int,
	z: Int,
	structureDirection: BlockFace
) : MultiblockEntity(
	manager,
	multiblock,
	world,
	x,
	y,
	z,
	structureDirection
), StatusTickedMultiblockEntity, SyncTickingMultiblockEntity, UserManagedMultiblockEntity, DisplayMultiblockEntity {
	val settings = ShipFactorySettings.load(data)

	override val userManager: UserManagedMultiblockEntity.UserManager = UserManagedMultiblockEntity.UserManager(data, true)
	override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(5)
	override val statusManager: StatusMultiblockEntity.StatusManager = StatusMultiblockEntity.StatusManager()

	protected companion object {
		fun Rotation.displayName(): Component = when (this) {
			Rotation.NONE -> Component.text("None")
			Rotation.CLOCKWISE_90 -> Component.text("Clockwise 90")
			Rotation.CLOCKWISE_180 -> Component.text("Clockwise 180")
			Rotation.COUNTERCLOCKWISE_90 -> Component.text("Counter Clockwise 90")
		}
	}

	fun openMenu(player: Player) {
		if (userManager.currentlyUsed()) {
			userManager.clear()
			return
		}

		userManager.setUser(player)
	}

	override fun tick() {
		if (!userManager.currentlyUsed()) return
		println("Toggled on")
	}

	override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
		userManager.saveUserData(store)
		settings.save(store)
	}
}
