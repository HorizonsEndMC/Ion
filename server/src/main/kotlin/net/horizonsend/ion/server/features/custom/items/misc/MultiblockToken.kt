package net.horizonsend.ion.server.features.custom.items.misc

import io.papermc.paper.datacomponent.DataComponentTypes
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlocks
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.rightClickListener
import net.horizonsend.ion.server.features.custom.items.component.StoredMultiblock
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.PrePackaged
import net.horizonsend.ion.server.features.multiblock.PrePackaged.getTokenData
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock.Companion.getDisplayName
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock.Companion.getModel
import net.horizonsend.ion.server.miscellaneous.utils.updateData
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updatePersistentDataContainer
import net.kyori.adventure.text.Component.text
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

object MultiblockToken : CustomItem(
	"MULTIBLOCK_TOKEN",
	text("Pre-Packaged Multiblock"),
	ItemFactory.unStackableCustomItem
) {
	override val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager).apply {
		addComponent(CustomComponentTypes.MULTIBLOCK_TYPE, StoredMultiblock)
		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, rightClickListener(this@MultiblockToken) { event, _, itemStack ->
			handleSecondaryInteract(event.player, itemStack, event)
		})
	}

	fun constructFor(multiblock: Multiblock): ItemStack {
		return constructItemStack()
			.updateDisplayName(ofChildren(text("Packaged "), multiblock.getDisplayName()))
			.updatePersistentDataContainer { PrePackaged.setTokenData(multiblock, this) }
			.updateData(DataComponentTypes.ITEM_MODEL, multiblock.getModel())
			.apply(::refreshLore)
	}

	private fun handleSecondaryInteract(livingEntity: Player, itemStack: ItemStack, event: PlayerInteractEvent) {
		if (itemStack.type.isAir) return

		val packagedData = getTokenData(itemStack) ?: run {
			livingEntity.userError("The packaged multiblock has no data!")
			return
		}

		val origin = PrePackaged.getOriginFromPlacement(
			event.clickedBlock ?: return,
			livingEntity.facing,
			packagedData.shape
		)

		val obstructions = PrePackaged.checkObstructions(origin, livingEntity.facing, packagedData.shape, livingEntity.isSneaking)

		if (obstructions.isNotEmpty()) {
			livingEntity.userError("Placement is obstructed! Crouch to enable block sharing.")
			livingEntity.highlightBlocks(obstructions, 50L)
			return
		}

		runCatching { PrePackaged.place(livingEntity, origin, livingEntity.facing, packagedData, null, null) }

		if (event.player.gameMode != GameMode.CREATIVE) {
			itemStack.amount--
		}
	}
}
