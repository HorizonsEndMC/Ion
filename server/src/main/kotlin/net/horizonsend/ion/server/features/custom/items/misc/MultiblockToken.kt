package net.horizonsend.ion.server.features.custom.items.misc

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlocks
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.rightClickListener
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.PrePackaged
import net.horizonsend.ion.server.features.multiblock.PrePackaged.getTokenData
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock.Companion.getDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.horizonsend.ion.server.miscellaneous.utils.updatePersistentDataContainer
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.util.Locale

object MultiblockToken : CustomItem(
	"MULTIBLOCK_TOKEN",
	text("Pre-Packaged Multiblock"),
	ItemFactory.unStackableCustomItem
) {
	override val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager).apply {
		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, rightClickListener(this@MultiblockToken) { event, _, itemStack ->
			handleSecondaryInteract(event.player, itemStack, event)
		})
	}

	fun constructFor(multiblock: Multiblock): ItemStack {
		val base = constructItemStack()
			.updateDisplayName(ofChildren(multiblock.getDisplayName(), text(" Token")))
			.updateLore(listOf(
				text("Multiblock: ${multiblock.name.replaceFirstChar { char -> char.uppercase(Locale.getDefault()) }}", GRAY).itemName,
				text("Variant: ${multiblock.javaClass.simpleName}", GRAY).itemName
			))

		return base.updatePersistentDataContainer { PrePackaged.setTokenData(multiblock, this) }
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
		itemStack.amount--
	}
}
