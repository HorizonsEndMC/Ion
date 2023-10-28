package net.horizonsend.ion.server.features.customitems.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.customitems.CustomItems
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
}
