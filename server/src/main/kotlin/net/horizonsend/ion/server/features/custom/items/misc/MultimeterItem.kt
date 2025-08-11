package net.horizonsend.ion.server.features.custom.items.misc

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.displayBlock
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.sendEntityPacket
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.leftClickListener
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.rightClickListener
import net.horizonsend.ion.server.features.custom.items.component.TickReceiverModule
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.FIRST_POINT
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_TYPE
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.SECOND_POINT
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.text.itemLore
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.horizonsend.ion.server.miscellaneous.utils.updatePersistentDataContainer
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.INTEGER
import org.bukkit.persistence.PersistentDataType.LONG
import org.bukkit.util.Vector

object MultimeterItem : CustomItem(
	"MULTIMETER",
	Component.text("Multimeter", NamedTextColor.YELLOW),
	ItemFactory.builder(ItemFactory.unStackableCustomItem("tool/multimeter")).build()
) {
	override val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager).apply {
		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, rightClickListener(this@MultimeterItem) { event, _, itemStack ->
			if (event.player.isSneaking) {
				cycleNetworks(event.player, event.player.world, itemStack)

				return@rightClickListener
			}

			setSecondPoint(event.player, itemStack, event)
		})

		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, leftClickListener(this@MultimeterItem) { event, _, itemStack ->
			setFirstPoint(event.player, itemStack, event)
		})

		addComponent(CustomComponentTypes.TICK_RECIEVER, TickReceiverModule(10) { entity, itemStack, _, _ ->
			if (entity !is Player) return@TickReceiverModule

			getFirstPoint(itemStack)?.let {
				val location = it.toLocation(entity.world)
				val block = Material.GREEN_CONCRETE.createBlockData()

				sendEntityPacket(entity, displayBlock(entity.world.minecraft, block, Vector(location.x, location.y, location.z), 0.75f, false), 11L)
			}
			getSecondPoint(itemStack)?.let {
				val location = it.toLocation(entity.world)
				val block = Material.RED_CONCRETE.createBlockData()

				sendEntityPacket(entity, displayBlock(entity.world.minecraft, block, Vector(location.x, location.y, location.z), 0.75f, false), 11L)
			}
		})
	}

	override fun assembleLore(itemStack: ItemStack): List<Component> {
		return listOf(
			text("Sneak-right-click to change mode", HE_MEDIUM_GRAY).itemLore,
			getModeText(itemStack).itemLore
		).plus(super.assembleLore(itemStack))
	}

	private fun getFirstPoint(itemStack: ItemStack): Vec3i? {
		return itemStack.itemMeta.persistentDataContainer.get(FIRST_POINT, LONG)?.let(::toVec3i)
	}

	private fun getSecondPoint(itemStack: ItemStack): Vec3i? {
		return itemStack.itemMeta.persistentDataContainer.get(SECOND_POINT, LONG)?.let(::toVec3i)
	}

	private fun setFirstPoint(player: Player, itemStack: ItemStack, event: PlayerInteractEvent) {
		val targeted = event.clickedBlock ?: return
		val key = toBlockKey(targeted.x, targeted.y, targeted.z)

		itemStack.updatePersistentDataContainer { set(FIRST_POINT, LONG, key) }

		player.information("Set first point to ${toVec3i(key)}")

		tryCheckResistance(player, player.world, itemStack)
	}

	private fun setSecondPoint(player: Player, itemStack: ItemStack, event: PlayerInteractEvent) {
		val targeted = event.clickedBlock ?: return
		val key = toBlockKey(targeted.x, targeted.y, targeted.z)

		itemStack.updatePersistentDataContainer { set(SECOND_POINT, LONG, key) }

		player.information("Set second point to ${toVec3i(key)}")

		tryCheckResistance(player, player.world, itemStack)
	}

	private fun tryCheckResistance(audience: Audience, world: World, itemStack: ItemStack) {
		val firstPoint = getFirstPoint(itemStack) ?: return
		val firstBlockKey = toBlockKey(firstPoint)
		val firstChunk = IonChunk[world, firstPoint.x.shr(4), firstPoint.z.shr(4)] ?: return

		val secondPoint = getSecondPoint(itemStack) ?: return
		val secondBlockKey = toBlockKey(secondPoint)

		val networkTypeIndex = itemStack.itemMeta.persistentDataContainer.getOrDefault(NODE_TYPE, INTEGER, 0)
		val cacheType = caches[networkTypeIndex]

		val firstNode = cacheType.get(firstChunk).getOrCache(firstBlockKey)
		if (firstNode == null) {
			audience.information("There is no node at $firstPoint")
			return
		}

		val secondNode = cacheType.get(firstChunk).getOrCache(secondBlockKey)
		if (secondNode == null) {
			audience.information("There is no node at $secondPoint")
			return
		}

		NewTransport.runTask(firstBlockKey, world) {
			val destinations = cacheType.get(firstChunk).getNetworkDestinations(
				this,
				destinationTypeClass = secondNode::class,
				originPos = firstBlockKey,
				originNode = firstNode,
				retainFullPath = true,
				debug = audience
			)

			val path = destinations.firstOrNull { it.destinationPosition == toBlockKey(secondPoint) }?.trackedPath

			if (path == null) {
				audience.userError("There is no path between these points")
				return@runTask
			}

			path.forEach { audience.highlightBlock(toVec3i(it.first), 100L) }

			audience.success("There are ${path.length} nodes on the path between point 1 and 2.")
			val nodeData = path.trackedNodes.groupBy { it.second::class }

			audience.information(nodeData.entries.joinToString(separator = "\n") { (nodeType, nodes) -> "${nodeType.simpleName}: ${nodes.size}" })
		}
	}

	fun getModeText(itemStack: ItemStack): Component {
		return template(text("Current mode: {0}", HE_MEDIUM_GRAY), useQuotesAroundObjects = false, getCurrentMode(itemStack))
	}

	fun getCurrentMode(itemStack: ItemStack): CacheType {
		return caches[itemStack.itemMeta.persistentDataContainer.getOrDefault(NODE_TYPE, INTEGER, 0)]
	}

	private fun cycleNetworks(audience: Audience, world: World, itemStack: ItemStack) {
		val currentIndex = itemStack.itemMeta.persistentDataContainer.getOrDefault(NODE_TYPE, INTEGER, 0)
		val newIndex = (currentIndex + 1) % caches.size

		itemStack.updatePersistentDataContainer { set(NODE_TYPE, INTEGER, newIndex) }

		itemStack.updateLore(getItemFactory().loreSupplier!!.invoke(itemStack))

		audience.success("Set network type to ${caches[newIndex]}")

		tryCheckResistance(audience, world, itemStack)
	}

	private val caches = arrayOf(CacheType.POWER, CacheType.ITEMS)
}
