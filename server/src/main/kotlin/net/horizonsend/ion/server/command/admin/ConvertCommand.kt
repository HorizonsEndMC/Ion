package net.horizonsend.ion.server.command.admin

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.DETONATOR
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.POWER_DRILL_BASIC
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.gear.getPower
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@CommandAlias("convert")
@Suppress("Unused")
@CommandPermission("ion.convert")
object ConvertCommand : SLCommand() { // I imagine we'll need more than blasters in the future
	@Subcommand("detonator")
	fun onConvertDetonator(sender: Player) {
		val heldItem = sender.inventory.itemInMainHand

		if (heldItem.type != Material.SHEARS ||
			!heldItem.itemMeta.hasCustomModelData() ||
			heldItem.itemMeta.customModelData == 0
		) {
			sender.userError("Not a valid custom item!")
			return
		}

		val newVersion = when (heldItem.itemMeta.customModelData) {
			1 -> DETONATOR
			else -> {
				sender.information("Wtf do you have")
				return
			}
		}.constructItemStack()

		newVersion.amount = 1

		sender.inventory.setItemInMainHand(newVersion)
		sender.updateInventory()
	}

	@Subcommand("drill")
	fun onConvertDrill(sender: Player) {
		val heldItem = sender.inventory.itemInMainHand

		val schrodingerDrill = tryConvertDrill(heldItem)

		if (schrodingerDrill == null) {
			sender.userError("Not a valid custom item!")
			return
		}

		sender.inventory.setItemInMainHand(schrodingerDrill)
		sender.updateInventory()
	}

	fun tryConvertDrill(heldItem: ItemStack): ItemStack? {
		if (heldItem.type != Material.DIAMOND_PICKAXE ||
			!heldItem.itemMeta.hasCustomModelData() ||
			heldItem.itemMeta.customModelData != 1
		) {
			return null
		}

		// Already converted
		if (heldItem.customItem != null) return null

		val oldPower = getPower(heldItem)

		val newDrill = POWER_DRILL_BASIC.constructItemStack()
		POWER_DRILL_BASIC.getComponent(CustomComponentTypes.POWER_STORAGE).setPower(POWER_DRILL_BASIC, newDrill, oldPower)

		return newDrill
	}

	@Subcommand("mineral")
	fun onConvertMineral(sender: Player) {
		val heldItem = sender.inventory.itemInMainHand

		val newVersion = convertCustomMineral(heldItem)
		if (newVersion == null) {
			sender.userError("Not a valid custom item!")
			return
		}

		sender.inventory.setItemInMainHand(newVersion)
		sender.updateInventory()
	}

	fun convertCustomMineral(item: ItemStack?) : ItemStack? {
		if (item == null) return null

		if ((item.type != Material.IRON_INGOT &&
				item.type != Material.IRON_ORE &&
				item.type != Material.IRON_BLOCK) ||
			!item.itemMeta.hasCustomModelData() ||
			item.itemMeta.customModelData == 0
			) {
			return null
		}

		if (item.customItem != null) {
			return null
		}

		val newVersion = when (item.type) {
			Material.IRON_INGOT -> when (item.itemMeta.customModelData) {
				1 -> CustomItemRegistry.ALUMINUM_INGOT
				2 -> CustomItemRegistry.CHETHERITE
				3 -> CustomItemRegistry.TITANIUM_INGOT
				4 -> CustomItemRegistry.URANIUM
				else -> {
					return null
				}
			}
			Material.IRON_ORE -> when (item.itemMeta.customModelData) {
				1 -> CustomItemRegistry.ALUMINUM_ORE
				2 -> CustomItemRegistry.CHETHERITE_ORE
				3 -> CustomItemRegistry.TITANIUM_ORE
				4 -> CustomItemRegistry.URANIUM_ORE
				else -> {
					return null
				}
			}
			Material.IRON_BLOCK -> when (item.itemMeta.customModelData) {
				1 -> CustomItemRegistry.ALUMINUM_BLOCK
				2 -> CustomItemRegistry.CHETHERITE_BLOCK
				3 -> CustomItemRegistry.TITANIUM_BLOCK
				4 -> CustomItemRegistry.URANIUM_BLOCK
				else -> {
					return null
				}
			}
			else -> {
				return null
			}
		}.constructItemStack()

		newVersion.amount = item.amount

		return newVersion
	}
}
