package net.horizonsend.ion.server.features.multiblock.type.shipfactory

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.gui.item.FeedbackItem.FeedbackItemResult
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.task.TaskHandlingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.UserManagedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.StatusTickedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.player.CombatTimer
import net.horizonsend.ion.server.features.starship.factory.BoundingBoxTask
import net.horizonsend.ion.server.features.starship.factory.NewShipFactoryTask
import net.horizonsend.ion.server.features.starship.factory.PreviewTask
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.canAccess
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.id.WrappedObjectId
import java.util.UUID

abstract class ShipFactoryEntity(
	data: PersistentMultiblockData,
	override val multiblock: AbstractShipFactoryMultiblock<*>,
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
), StatusTickedMultiblockEntity, AsyncTickingMultiblockEntity, UserManagedMultiblockEntity, DisplayMultiblockEntity, TaskHandlingMultiblockEntity<NewShipFactoryTask> {
	val settings = ShipFactorySettings.load(data)

	override val userManager: UserManagedMultiblockEntity.UserManager = UserManagedMultiblockEntity.UserManager(data, persistent = false)
	override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(5)
	override val statusManager: StatusMultiblockEntity.StatusManager = StatusMultiblockEntity.StatusManager()

	abstract val guiTitle: String

	fun openMenu(player: Player) {
		ShipFactoryGui(player, this).open()
	}

	val isRunning get() = userManager.currentlyUsed()

	var blueprintName: String = data.getAdditionalDataOrDefault(NamespacedKeys.BLUEPRINT_NAME, PersistentDataType.STRING, "?"); private set
	private var currentBlueprint: Oid<Blueprint>? = data.getAdditionalData(NamespacedKeys.BLUEPRINT_ID, PersistentDataType.STRING)?.let { WrappedObjectId(it) }

	fun setBlueprint(blueprint: Blueprint) {
		blueprintName = blueprint.name
		currentBlueprint = blueprint._id
		cachedBlueprintData = blueprint
	}

	// Not saved, loaded async when #ensureBlueprintLoaded is called
	var cachedBlueprintData: Blueprint? = null; private set

	override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
		userManager.saveUserData(store)
		settings.save(store)

		currentBlueprint?.let { store.addAdditionalData(NamespacedKeys.BLUEPRINT_ID, PersistentDataType.STRING, it.toString()) }
		store.addAdditionalData(NamespacedKeys.BLUEPRINT_NAME, PersistentDataType.STRING, blueprintName)
	}

	override var task: NewShipFactoryTask? = null

	fun enable(user: Player) {
		if (userManager.currentlyUsed()) return

		if (!ensureBlueprintLoaded(user)) return
		val blueprint = cachedBlueprintData ?: return

		if (!checkBlueprintPermissions(blueprint, user)) return

		userManager.setUser(user)
		startTask(NewShipFactoryTask(blueprint, settings,this, getInventories(), user))
	}

	fun disable() {
		if (!userManager.currentlyUsed()) return
		userManager.clear()
		stopTask()
	}

	override fun tickAsync() {
		tickBoundingBoxTasks()

		if (!userManager.currentlyUsed()) return
		val player = userManager.getUserPlayer() ?: return

		if (!ensureBlueprintLoaded(player)) return

		task?.tick()
	}

	// Util section
	private fun tryResolveBlueprint(player: Player, blueprintName: String): Blueprint? {
		val blueprint = Blueprint.col.findOne(and(Blueprint::name eq blueprintName, Blueprint::owner eq player.slPlayerId))

		if (blueprint == null) {
			player.userError("Blueprint not found")
			return null
		}

		return blueprint
	}

	fun ensureBlueprintLoaded(player: Player): Boolean {
		if (cachedBlueprintData != null && cachedBlueprintData!!._id == currentBlueprint) return true

		val id = currentBlueprint
		if (id != null) {
			val result = Blueprint.findById(id)

			if (result != null) {
				blueprintName = result.name
				cachedBlueprintData = result
				return true
			}
		}

		val byName = tryResolveBlueprint(player, blueprintName)
		if (byName != null) {
			blueprintName = byName.name
			cachedBlueprintData = byName

			return true
		}

		sleepWithStatus(Component.text("Blueprint $blueprintName not found!", NamedTextColor.RED), 20)
		return false
	}

	private fun checkBlueprintPermissions(blueprint: Blueprint, user: Player): Boolean {
		if (!blueprint.canAccess(user)) {
			user.userError("You don't have access to that blueprint")
			return false
		}

		if (CombatTimer.isPvpCombatTagged(user)) {
			user.userError("Cannot activate Ship Factories while in combat")
			return false
		}

		return true
	}

	fun checkEnableButton(user: Player): FeedbackItemResult? {
		if (CombatTimer.isPvpCombatTagged(user)) return FeedbackItemResult.FailureLore(listOf(Component.text("Cannot activate Ship Factories while in combat!", NamedTextColor.RED)))

		val cached = cachedBlueprintData ?: return FeedbackItemResult.FailureLore(listOf(Component.text("Blueprint not found!", NamedTextColor.RED)))
		if (!cached.canAccess(user)) return FeedbackItemResult.FailureLore(listOf(Component.text("You don't have access to that blueprint!", NamedTextColor.RED)))

		return null
	}

	fun getPreview(player: Player, duration: Long): PreviewTask? {
		return PreviewTask(cachedBlueprintData ?: return null, settings, this, player, duration)
	}

	private val boundingBoxPreviews = mutableMapOf<UUID, BoundingBoxTask>()

	fun tickBoundingBoxTasks() {
		for ((_, boundingBox) in boundingBoxPreviews) {
			boundingBox.tick()
		}
	}

	fun toggleBoundingBox(player: Player): Boolean {
		if (boundingBoxPreviews.containsKey(player.uniqueId)) {
			return disableBoundingBox(player)
		}

		return enableBoundingBox(player)
	}

	private fun enableBoundingBox(player: Player): Boolean {
		val blueprint = cachedBlueprintData ?: return false

		val existing = boundingBoxPreviews[player.uniqueId]
		if (existing != null && existing.getBlueprintId() == currentBlueprint) return true

		Tasks.async {
			boundingBoxPreviews[player.uniqueId] = BoundingBoxTask(blueprint, settings, this, player)
		}
		return true
	}

	private fun disableBoundingBox(player: Player): Boolean {
		boundingBoxPreviews.remove(player.uniqueId)
		return true
	}

	fun reCalculate() {
		Tasks.async { boundingBoxPreviews.values.forEach { it.recalculate() } }
	}

	fun canEditSettings() = task == null

	abstract fun getInventories(): Set<InventoryReference>

	sealed interface InventoryReference {
		val inventory: CraftInventory
		fun isAvailable(itemStack: ItemStack): Boolean

		data class StandardInventoryReference(override val inventory: CraftInventory): InventoryReference {
			override fun isAvailable(itemStack: ItemStack): Boolean = true
		}

		data class RemoteInventoryReference(
			override val inventory: CraftInventory,
			val extractorKey: BlockKey,
			val connectedFrom: BlockKey,
			val entity: AdvancedShipFactoryMultiblock.AdvancedShipFactoryEntity
		): InventoryReference {
			override fun isAvailable(itemStack: ItemStack): Boolean = entity.canRemoveFromDestination(extractorKey, connectedFrom, itemStack)
		}
	}
}
