package net.horizonsend.ion.server.features.multiblock.type.shipfactory

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.UserManagedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.StatusTickedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.world.level.block.Rotation
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.findOne

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

	fun openMenu(player: Player) {
		ShipFactoryGui(player, this).open()
	}

	var blueprintName: String = data.getAdditionalDataOrDefault(NamespacedKeys.BLUEPRINT_NAME, PersistentDataType.STRING, "?")
	protected var currentBlueprint: Oid<Blueprint>? = null

	override fun tick() {
		if (!userManager.currentlyUsed()) return
		val player = userManager.getUserPlayer() ?: return

		if (!ensureBlueprintLoaded(player)) return
		setStatus(Component.text(blueprintName))
		println("Toggled on")
	}

	override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
		userManager.saveUserData(store)
		settings.save(store)
		store.addAdditionalData(NamespacedKeys.BLUEPRINT_NAME, PersistentDataType.STRING, blueprintName)
	}

	fun enable(user: Player) {
		if (userManager.currentlyUsed()) return
		userManager.setUser(user)
	}

	fun disable() {
		if (!userManager.currentlyUsed()) return
		userManager.clear()
	}

	private fun tryResolveBlueprint(player: Player, blueprintName: String): Blueprint? {
		val blueprint = Blueprint.col.findOne(and(Blueprint::name eq blueprintName, Blueprint::owner eq player.slPlayerId))

		if (blueprint == null) {
			player.userError("Blueprint not found")
			return null
		}

		return blueprint
	}

	fun ensureBlueprintLoaded(player: Player): Boolean {
		if (currentBlueprint != null) return true

		val result = tryResolveBlueprint(player, blueprintName)

		if (result == null) {
			sleepWithStatus(Component.text("Blueprint $blueprintName not found!", NamedTextColor.RED), 20)
			return false
		}

		currentBlueprint = result._id

		return true
	}

	protected companion object {
		fun Rotation.displayName(): Component = when (this) {
			Rotation.NONE -> Component.text("None")
			Rotation.CLOCKWISE_90 -> Component.text("Clockwise 90")
			Rotation.CLOCKWISE_180 -> Component.text("Clockwise 180")
			Rotation.COUNTERCLOCKWISE_90 -> Component.text("Counter Clockwise 90")
		}
	}
}
