package net.horizonsend.ion.server.features.custom.items.misc

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlocks
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.PrePackaged
import net.horizonsend.ion.server.features.multiblock.PrePackaged.getPackagedData
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock.Companion.getDisplayName
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.CUSTOM_ITEM
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.Locale

object PackagedMultiblockItem : CustomItem("PACKAGED_MULTIBLOCK") {
	override fun constructItemStack(): ItemStack {
		val base = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
			it.persistentDataContainer.set(CUSTOM_ITEM, PersistentDataType.STRING, identifier)
			it.displayName(text("Pre-Packaged Multiblock").decoration(ITALIC, false))
		}

		return base
	}

	fun constructFor(multiblock: Multiblock): ItemStack {
		val base = constructItemStack()

		return base.updateMeta {
			PrePackaged.packageData(PrePackaged.PackagedMultiblockData(multiblock), it.persistentDataContainer)
			it.displayName(text()
				.decoration(ITALIC, false)
				.color(WHITE)
				.append(text("Pre-packaged "))
				.append(multiblock.getDisplayName())
				.build()
			)
			it.lore(listOf(
				text("Multiblock: ${multiblock.name.replaceFirstChar { char -> char.uppercase(Locale.getDefault()) }}", GRAY).decoration(ITALIC, false),
				text("Variant: ${multiblock.javaClass.simpleName}", GRAY).decoration(ITALIC, false)
			))
		}
	}

	override fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack, event: PlayerInteractEvent?) {
		if (itemStack.type.isAir) return

		val packagedData = getPackagedData(itemStack) ?: run {
			livingEntity.userError("The packaged multiblock has no data!")
			return
		}

		if (livingEntity !is Player) return

		if (event == null) return

		val origin = PrePackaged.getOriginFromPlacement(
			event.clickedBlock ?: return,
			livingEntity.facing,
			packagedData.multiblock.shape
		)

		val obstructions = PrePackaged.checkObstructions(origin, livingEntity.facing, packagedData.multiblock.shape)

		if (obstructions.isNotEmpty()) {
			livingEntity.userError("Placement is obstructed!")
			livingEntity.highlightBlocks(obstructions, 50L)
			return
		}

		PrePackaged.place(livingEntity, origin, livingEntity.facing, packagedData)
		livingEntity.inventory.remove(itemStack)
	}
}
