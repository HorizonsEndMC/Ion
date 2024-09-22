package net.horizonsend.ion.server.features.custom.items.misc

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.transport.node.util.NetworkType
import net.horizonsend.ion.server.features.transport.node.util.calculatePathResistance
import net.horizonsend.ion.server.features.transport.node.util.getIdealPath
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_VARIANT
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.X
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.Z
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.persistence.PersistentDataType.INTEGER
import org.bukkit.persistence.PersistentDataType.LONG

object MultimeterItem : CustomItem("Multimeter") {
	override fun constructItemStack(): ItemStack {
		return ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
			it.displayName(Component.text("Multimeter", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
			it.persistentDataContainer.set(NamespacedKeys.CUSTOM_ITEM, PersistentDataType.STRING, identifier)
		}
	}

	override fun handlePrimaryInteract(livingEntity: LivingEntity, itemStack: ItemStack, event: PlayerInteractEvent) {
		val targeted = livingEntity.getTargetBlock(null, 10)
		val key = toBlockKey(targeted.x, targeted.y, targeted.z)

		itemStack.updateMeta {
			it.persistentDataContainer.set(X, LONG, key)
		}

		livingEntity.information("Set first point to ${toVec3i(key)}")

		tryCheckResistance(livingEntity, livingEntity.world, itemStack)
	}

	override fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack, event: PlayerInteractEvent?) {
		if (livingEntity !is Player) return
		if (livingEntity.isSneaking) {
			cycleNetworks(livingEntity, livingEntity.world, itemStack)
			return
		}

		val targeted = livingEntity.getTargetBlock(null, 10)
		val key = toBlockKey(targeted.x, targeted.y, targeted.z)

		itemStack.updateMeta {
			it.persistentDataContainer.set(Z, LONG, key)
		}

		livingEntity.information("Set second point to ${toVec3i(key)}")

		tryCheckResistance(livingEntity, livingEntity.world, itemStack)
	}

	private fun tryCheckResistance(audience: Audience, world: World, itemStack: ItemStack) {
		val firstPoint = itemStack.itemMeta.persistentDataContainer.get(X, LONG) ?: return
		val firstChunk = IonChunk[world, getX(firstPoint).shr(4), getZ(firstPoint).shr(4)] ?: return
		val secondPoint = itemStack.itemMeta.persistentDataContainer.get(Z, LONG) ?: return
		val secondChunk = IonChunk[world, getX(secondPoint).shr(4), getZ(secondPoint).shr(4)] ?: return

		val networkTypeIndex = itemStack.itemMeta.persistentDataContainer.getOrDefault(NODE_VARIANT, INTEGER, 0)
		val networkType = NetworkType.entries[networkTypeIndex]

		val firstNode = networkType.get(firstChunk).getNode(firstPoint) ?: return audience.information("There is no node at ${toVec3i(firstPoint)}")
		val secondNode = networkType.get(secondChunk).getNode(secondPoint)  ?: return audience.information("There is no node at ${toVec3i(secondPoint)}")

		val path = getIdealPath(firstNode, secondNode)
		val resistance = calculatePathResistance(path) ?: return audience.userError("There is no path connecting these nodes")
		audience.information("The resistance from ${firstNode.javaClass.simpleName} at ${toVec3i(firstPoint)} to ${secondNode.javaClass.simpleName} at ${toVec3i(secondPoint)} is $resistance")
	}

	private fun cycleNetworks(audience: Audience, world: World, itemStack: ItemStack) {
		val currentIndex = itemStack.itemMeta.persistentDataContainer.getOrDefault(NODE_VARIANT, INTEGER, 0)
		val newIndex = (currentIndex + 1) % NetworkType.entries.size
		itemStack.updateMeta {
			it.persistentDataContainer.set(NODE_VARIANT, INTEGER, newIndex)
		}

		audience.success("Set network type to ${NetworkType.entries[newIndex]}")

		tryCheckResistance(audience, world, itemStack)
	}
}
