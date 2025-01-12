package net.horizonsend.ion.server.features.custom.items.misc

import io.papermc.paper.datacomponent.DataComponentTypes
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlocks
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.leftClickListener
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.rightClickListener
import net.horizonsend.ion.server.features.custom.items.component.StoredMultiblock
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.PrePackaged
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock.Companion.getDisplayName
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updatePersistentDataContainer
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

object PackagedMultiblock : CustomItem(
	"PACKAGED_MULTIBLOCK",
	ofChildren(text("Packaged Null")),
	ItemFactory.unStackableCustomItem
) {
	override val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager).apply {
		addComponent(CustomComponentTypes.MULTIBLOCK_TYPE, StoredMultiblock)

		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, rightClickListener(this@PackagedMultiblock) { event, _, itemStack ->
			tryPlace(event.player, itemStack, event)
		})
		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, leftClickListener(this@PackagedMultiblock) { event, _, itemStack ->
			tryPreview(event.player, itemStack, event)
		})
	}

	fun createFor(multiblock: Multiblock): ItemStack {
		return constructItemStack()
			.updatePersistentDataContainer { PrePackaged.setTokenData(multiblock, this) }
			.updateDisplayName(ofChildren(text("Packaged "), multiblock.getDisplayName()))
			.apply(::refreshLore)
	}

	private fun tryPlace(livingEntity: Player, itemStack: ItemStack, event: PlayerInteractEvent) {
		if (itemStack.type.isAir) return

		val packagedData = PrePackaged.getTokenData(itemStack) ?: run {
			livingEntity.userError("The packaged multiblock has no data!")
			return
		}

		val contents = itemStack.getData(DataComponentTypes.CONTAINER)?.contents()?.toMutableList() ?: return livingEntity.userError("The packaged multiblock has no data!")

		val direction = livingEntity.facing
		val origin = PrePackaged.getOriginFromPlacement(
			event.clickedBlock ?: return,
			direction,
			packagedData.shape
		)

		val obstructions = PrePackaged.checkObstructions(origin, livingEntity.facing, packagedData.shape, livingEntity.isSneaking)

		if (obstructions.isNotEmpty()) {
			livingEntity.userError("Placement is obstructed!  Crouch to enable block sharing.")
			livingEntity.highlightBlocks(obstructions, 50L)
			return
		}

		val entityData = itemStack.itemMeta.persistentDataContainer.get(NamespacedKeys.MULTIBLOCK_ENTITY_DATA, PersistentMultiblockData)

		runCatching { PrePackaged.place(livingEntity, origin, livingEntity.facing, packagedData, contents, entityData) }.onFailure {
			livingEntity.information("ERROR: $it")
			it.printStackTrace()
		}.onSuccess {
			// Drop remaining items in packaged multi
			val dropLocation = origin.getRelative(direction.oppositeFace).location.toCenterLocation()

			for (item in contents.filterNotNull()) {
				livingEntity.world.dropItem(dropLocation, item)
			}
		}

		itemStack.amount--
	}

	fun tryPreview(livingEntity: LivingEntity, itemStack: ItemStack, event: PlayerInteractEvent) {
		if (livingEntity !is Player) return

		val packagedData = PrePackaged.getTokenData(itemStack) ?: run {
			livingEntity.userError("The packaged multiblock has no data!")
			return
		}

		val origin = PrePackaged.getOriginFromPlacement(
			event.clickedBlock ?: return,
			livingEntity.facing,
			packagedData.shape
		)

		val locations = packagedData.shape.getLocations(livingEntity.facing).map { Vec3i(origin.x, origin.y, origin.z).plus(it) }
		livingEntity.highlightBlocks(locations, 100L)
	}
}
