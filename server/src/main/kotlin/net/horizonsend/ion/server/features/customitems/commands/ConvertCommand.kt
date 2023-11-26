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

		if ((heldItem.type != Material.IRON_INGOT &&
					heldItem.type != Material.IRON_ORE &&
					heldItem.type != Material.IRON_BLOCK) ||
			!heldItem.itemMeta.hasCustomModelData() ||
			heldItem.itemMeta.customModelData == 0
		) {
			sender.userError("Not a valid custom item!")
			return
		}

		if (heldItem.customItem != null) {
			sender.userError("Item is already converted!")
			return
		}

		val newVersion = when (heldItem.type) {
			Material.IRON_INGOT -> when (heldItem.itemMeta.customModelData) {
				1 -> CustomItems.ALUMINUM_INGOT
				2 -> CustomItems.CHETHERITE
				3 -> CustomItems.TITANIUM_INGOT
				4 -> CustomItems.URANIUM
				else -> {
					sender.information("Wtf do you have")
					return
				}
			}
			Material.IRON_ORE -> when (heldItem.itemMeta.customModelData) {
				1 -> CustomItems.ALUMINUM_ORE
				2 -> CustomItems.CHETHERITE_ORE
				3 -> CustomItems.TITANIUM_ORE
				4 -> CustomItems.URANIUM_ORE
				else -> {
					sender.information("Wtf do you have")
					return
				}
			}
			Material.IRON_BLOCK -> when (heldItem.itemMeta.customModelData) {
				1 -> CustomItems.ALUMINUM_BLOCK
				2 -> CustomItems.CHETHERITE_BLOCK
				3 -> CustomItems.TITANIUM_BLOCK
				4 -> CustomItems.URANIUM_BLOCK
				else -> {
					sender.information("Wtf do you have")
					return
				}
			}
			else -> {
				sender.userError("This should not have exited here...")
				return
			}
		}.constructItemStack()

		newVersion.amount = heldItem.amount

		sender.inventory.setItemInMainHand(newVersion)
		sender.updateInventory()
	}
}
