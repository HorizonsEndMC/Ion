package net.horizonsend.ion.server.features.custom.items.misc

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlocks
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.PrePackaged
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock.Companion.getDisplayName
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.CUSTOM_ITEM
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.persistence.PersistentDataType.STRING
import java.util.Locale

object PackagedMultiblock : CustomItem("PACKAGED_MULTIBLOCK") {
	override fun constructItemStack(): ItemStack {
		return ItemStack(Material.CHEST).updateMeta {
			it.persistentDataContainer.set(CUSTOM_ITEM, STRING, identifier)
		}
	}

	fun createFor(multiblock: Multiblock): ItemStack {
		return constructItemStack().updateMeta {
			PrePackaged.setTokenData(multiblock, it.persistentDataContainer)
			it.displayName(ofChildren(Component.text("Packaged "), multiblock.getDisplayName()).itemName)
			it.lore(listOf(
				Component.text("Multiblock: ${multiblock.name.replaceFirstChar { char -> char.uppercase(Locale.getDefault()) }}", NamedTextColor.GRAY).itemName,
				Component.text("Variant: ${multiblock.javaClass.simpleName}", NamedTextColor.GRAY).itemName
			))
		}
	}

	override fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack, event: PlayerInteractEvent?) {
		if (itemStack.type.isAir) return

		val packagedData = PrePackaged.getTokenData(itemStack) ?: run {
			livingEntity.userError("The packaged multiblock has no data!")
			return
		}

		val inventory = ((itemStack.itemMeta as? BlockStateMeta)?.blockState as? Chest)?.inventory ?: return livingEntity.userError("The packaged multiblock has no data!")

		if (livingEntity !is Player) return

		if (event == null) return

		val origin = PrePackaged.getOriginFromPlacement(
			event.clickedBlock ?: return,
			livingEntity.facing,
			packagedData.shape
		)

		val obstructions = PrePackaged.checkObstructions(origin, livingEntity.facing, packagedData.shape)

		if (obstructions.isNotEmpty()) {
			livingEntity.userError("Placement is obstructed!")
			livingEntity.highlightBlocks(obstructions, 50L)
			return
		}

		runCatching { PrePackaged.place(livingEntity, origin, livingEntity.facing, packagedData, inventory) }.onFailure {
			livingEntity.information("ERROR: $it")
			it.printStackTrace()
		}

		itemStack.amount--
	}
}
