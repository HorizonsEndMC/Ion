package net.horizonsend.ion.server.features.multiblock.type.shipfactory

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.gui.item.FeedbackItem.FeedbackItemResult
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.UserManagedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.StatusTickedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.player.CombatTimer
import net.horizonsend.ion.server.features.starship.factory.NewShipFactoryTask
import net.horizonsend.ion.server.features.starship.factory.ShipFactoryPreview
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.canAccess
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.id.WrappedObjectId

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
), StatusTickedMultiblockEntity, AsyncTickingMultiblockEntity, UserManagedMultiblockEntity, DisplayMultiblockEntity {
	val settings = ShipFactorySettings.load(data)

	override val userManager: UserManagedMultiblockEntity.UserManager = UserManagedMultiblockEntity.UserManager(data, persistent = false)
	override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(5)
	override val statusManager: StatusMultiblockEntity.StatusManager = StatusMultiblockEntity.StatusManager()

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

	var task: NewShipFactoryTask? = null

	fun enable(user: Player) {
		if (userManager.currentlyUsed()) return

		if (!ensureBlueprintLoaded(user)) return
		val blueprint = cachedBlueprintData ?: return

		if (!checkBlueprintPermissions(blueprint, user)) return

		userManager.setUser(user)

		task = NewShipFactoryTask(blueprint, settings, this, user)
		task?.onEnable()
	}

	fun disable() {
		if (!userManager.currentlyUsed()) return
		userManager.clear()

		task?.onDisable()
		task = null
	}

	override fun tickAsync() {
		if (!userManager.currentlyUsed()) return
		val player = userManager.getUserPlayer() ?: return

		if (!ensureBlueprintLoaded(player)) return
		setStatus(Component.text(blueprintName))

		task?.tickProgress()
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

		val result = tryResolveBlueprint(player, blueprintName)

		if (result == null) {
			sleepWithStatus(Component.text("Blueprint $blueprintName not found!", NamedTextColor.RED), 20)
			return false
		}

		currentBlueprint = result._id
		cachedBlueprintData = result

		return true
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

	fun getPreview(player: Player, duration: Long): ShipFactoryPreview? {
		return ShipFactoryPreview(cachedBlueprintData ?: return null, settings, this, player, duration)
	}

	fun canEditSettings() = task == null
}
