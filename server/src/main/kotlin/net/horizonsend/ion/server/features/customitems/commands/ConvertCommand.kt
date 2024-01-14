package net.horizonsend.ion.server.features.customitems.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.customitems.CustomItems
import net.horizonsend.ion.server.features.customitems.CustomItems.customItem
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@CommandAlias("convert")
@Suppress("Unused")
@CommandPermission("ion.convert")
object ConvertCommand : SLCommand() { // I imagine we'll need more than blasters in the future
	@Subcommand("blaster")
	fun onConvertBlaster(sender: Player) { // Easier than trying to figure out what the old type is.
		val heldItem = sender.inventory.itemInMainHand

		if (heldItem.type != Material.BOW ||
			!heldItem.itemMeta.hasCustomModelData() ||
			heldItem.itemMeta.customModelData == 0
		) {
			sender.userError("Not a valid custom item!")
			return
		}

		val newVersion = when (heldItem.itemMeta.customModelData) {
			1 -> CustomItems.PISTOL
			2 -> CustomItems.RIFLE
			3 -> CustomItems.SNIPER
			4 -> CustomItems.CANNON
			else -> {
				sender.information("Sorry, but there is no current equivalent for the cannon, one will come soon")
				return
			}
		}.constructItemStack()

		newVersion.amount = 1

		sender.inventory.setItemInMainHand(newVersion)
		sender.updateInventory()
	}

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
			1 -> CustomItems.DETONATOR
			else -> {
				sender.information("Wtf do you have")
				return
			}
		}.constructItemStack()

		newVersion.amount = 1

		sender.inventory.setItemInMainHand(newVersion)
		sender.updateInventory()
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
				1 -> CustomItems.ALUMINUM_INGOT
				2 -> CustomItems.CHETHERITE
				3 -> CustomItems.TITANIUM_INGOT
				4 -> CustomItems.URANIUM
				else -> {
					return null
				}
			}
			Material.IRON_ORE -> when (item.itemMeta.customModelData) {
				1 -> CustomItems.ALUMINUM_ORE
				2 -> CustomItems.CHETHERITE_ORE
				3 -> CustomItems.TITANIUM_ORE
				4 -> CustomItems.URANIUM_ORE
				else -> {
					return null
				}
			}
			Material.IRON_BLOCK -> when (item.itemMeta.customModelData) {
				1 -> CustomItems.ALUMINUM_BLOCK
				2 -> CustomItems.CHETHERITE_BLOCK
				3 -> CustomItems.TITANIUM_BLOCK
				4 -> CustomItems.URANIUM_BLOCK
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
