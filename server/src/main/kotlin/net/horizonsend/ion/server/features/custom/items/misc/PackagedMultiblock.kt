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
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.PrePackaged
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock.Companion.getDisplayName
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.util.Locale

object PackagedMultiblock : CustomItem(
	"PACKAGED_MULTIBLOCK",
	ofChildren(Component.text("Packaged Null")),
	ItemFactory.unStackableCustomItem
) {
	override val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager).apply {
		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, rightClickListener(this@PackagedMultiblock) { event, _, itemStack ->
			handleSecondaryInteract(event.player, itemStack, event)
		})
		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, leftClickListener(this@PackagedMultiblock) { event, _, itemStack ->
			handlePrimaryInteract(event.player, itemStack, event)
		})
	}

	fun createFor(multiblock: Multiblock): ItemStack {
		return constructItemStack().updateMeta {
			PrePackaged.setTokenData(multiblock, it.persistentDataContainer)
			it.displayName(ofChildren(Component.text("Packaged "), multiblock.getDisplayName()).itemName)
			it.lore(listOf(
				Component.text("Multiblock: ${multiblock.name.replaceFirstChar { char -> char.uppercase(Locale.getDefault()) }}", NamedTextColor.GRAY).itemName,
				Component.text("Variant: ${multiblock.javaClass.simpleName}", NamedTextColor.GRAY).itemName,
				Component.text("Left click to preview", NamedTextColor.GRAY).itemName,
				Component.text("Right click to place", NamedTextColor.GRAY).itemName
			))
		}
	}

	private fun handleSecondaryInteract(livingEntity: Player, itemStack: ItemStack, event: PlayerInteractEvent) {
		if (itemStack.type.isAir) return

		val packagedData = PrePackaged.getTokenData(itemStack) ?: run {
			livingEntity.userError("The packaged multiblock has no data!")
			return
		}

		val contents = itemStack.getData(DataComponentTypes.CONTAINER) ?: return livingEntity.userError("The packaged multiblock has no data!")

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

		runCatching { PrePackaged.place(livingEntity, origin, livingEntity.facing, packagedData, contents.contents(), entityData) }.onFailure {
			livingEntity.information("ERROR: $it")
			it.printStackTrace()
		}.onSuccess {
			// Drop remaining items in packaged multi
			val dropLocation = origin.getRelative(direction.oppositeFace).location.toCenterLocation()

			for (item in contents.contents().filterNotNull()) {
				livingEntity.world.dropItem(dropLocation, item)
			}
		}

		itemStack.amount--
	}

	fun handlePrimaryInteract(livingEntity: LivingEntity, itemStack: ItemStack, event: PlayerInteractEvent) {
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
