package net.horizonsend.ion.server.features.custom.items.misc

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.leftClickListener
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.rightClickListener
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_TYPE
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.X
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.Z
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.updatePersistentDataContainer
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.INTEGER
import org.bukkit.persistence.PersistentDataType.LONG

object MultimeterItem : CustomItem(CustomItemKeys.MULTIMETER, Component.text("Multimeter", NamedTextColor.YELLOW), ItemFactory.unStackableCustomItem) {
	override val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager).apply {
		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, rightClickListener(this@MultimeterItem) { event, _, itemStack ->
			if (event.player.isSneaking) {
				cycleNetworks(event.player, event.player.world, itemStack)

				return@rightClickListener
			}

			setSecondPoint(event.player, itemStack)
		})

		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, leftClickListener(this@MultimeterItem) { event, _, itemStack ->
			setFirstPoint(event.player, itemStack)
		})
	}

	private fun setFirstPoint(player: Player, itemStack: ItemStack) {
		val targeted = player.getTargetBlock(null, 10)
		val key = toBlockKey(targeted.x, targeted.y, targeted.z)

		itemStack.updatePersistentDataContainer { set(X, LONG, key) }

		player.information("Set first point to ${toVec3i(key)}")

		tryCheckResistance(player, player.world, itemStack)
	}

	private fun setSecondPoint(player: Player, itemStack: ItemStack) {
		val targeted = player.getTargetBlock(null, 10)
		val key = toBlockKey(targeted.x, targeted.y, targeted.z)

		itemStack.updatePersistentDataContainer { set(Z, LONG, key) }

		player.information("Set second point to ${toVec3i(key)}")

		tryCheckResistance(player, player.world, itemStack)
	}

	private fun tryCheckResistance(audience: Audience, world: World, itemStack: ItemStack) {
		val firstPoint = itemStack.itemMeta.persistentDataContainer.get(X, LONG) ?: return
		val firstChunk = IonChunk[world, getX(firstPoint).shr(4), getZ(firstPoint).shr(4)] ?: return
		val secondPoint = itemStack.itemMeta.persistentDataContainer.get(Z, LONG) ?: return

		val networkTypeIndex = itemStack.itemMeta.persistentDataContainer.getOrDefault(NODE_TYPE, INTEGER, 0)
		val cacheType = CacheType.entries[networkTypeIndex]

		val firstNode = cacheType.get(firstChunk).getOrCache(firstPoint)
		if (firstNode == null) {
			audience.information("There is no node at ${toVec3i(firstPoint)}")
			return
		}

		val secondNode = cacheType.get(firstChunk).getOrCache(secondPoint)
		if (secondNode == null) {
			audience.information("There is no node at ${toVec3i(secondPoint)}")
			return
		}

		NewTransport.runTask(firstPoint, world) {
			val destinations = cacheType.get(firstChunk).getNetworkDestinations(
				this,
				destinationTypeClass = secondNode::class,
				originPos = firstPoint,
				originNode = firstNode,
				retainFullPath = true,
				debug = audience
			)

			val path = destinations.firstOrNull { it.destinationPosition == secondPoint }?.trackedPath

			if (path == null) {
				audience.userError("There is no path between these points")
				return@runTask
			}

			path.forEach { audience.highlightBlock(toVec3i(it.first), 100L) }
			audience.success("There are ${path.length} nodes on the path between point 1 and 2.")
			val nodeData = path.trackedNodes.groupBy { it.second::class }

			audience.information(nodeData.entries.joinToString(separator = "\n") { (nodeType, nodes) -> "${nodeType.simpleName} : ${nodes.size}" })
		}
	}

	private fun cycleNetworks(audience: Audience, world: World, itemStack: ItemStack) {
		val currentIndex = itemStack.itemMeta.persistentDataContainer.getOrDefault(NODE_TYPE, INTEGER, 0)
		val newIndex = (currentIndex + 1) % CacheType.entries.size

		itemStack.updatePersistentDataContainer { set(NODE_TYPE, INTEGER, newIndex) }

		audience.success("Set network type to ${CacheType.entries[newIndex]}")

		tryCheckResistance(audience, world, itemStack)
	}
}
