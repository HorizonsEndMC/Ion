package net.horizonsend.ion.server.command.admin

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.custom.items.CustomItems.customItem
import net.horizonsend.ion.server.features.custom.items.mods.ItemModRegistry
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.objects.ModdedCustomItem
import org.bukkit.entity.Player

@CommandAlias("itemdebug")
object ItemDebugCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandContexts.registerContext(ItemModification::class.java) {
			val name = it.popFirstArg()
			return@registerContext ItemModRegistry[name] ?: throw InvalidCommandArgument("$name not found!")
		}

		manager.commandCompletions.registerCompletion("tool_mods") {
			return@registerCompletion ItemModRegistry.mods.keys
		}

		manager.commandCompletions.setDefaultCompletion("tool_mods", ItemModification::class.java)
	}

	@Subcommand("getmods")
	fun getMods(sender: Player) {
		val item = sender.inventory.itemInMainHand
		val custom = item.customItem as? ModdedCustomItem ?: fail { "${item.customItem?.identifier} is not moddable" }

		sender.information("MODS: " + custom.getMods(item).joinToString { it.identifier })
	}

	@Subcommand("addmod")
	fun addMod(sender: Player, mod: ItemModification) {
		val item = sender.inventory.itemInMainHand
		val custom = item.customItem as? ModdedCustomItem ?: fail { "${item.customItem?.identifier} is not moddable" }

		custom.addMod(item, mod)

		sender.information("Added ${mod.identifier}")
	}

	@Subcommand("removemod")
	fun removeMod(sender: Player, mod: ItemModification) {
		val item = sender.inventory.itemInMainHand
		val custom = item.customItem as? ModdedCustomItem ?: fail { "${item.customItem?.identifier} is not moddable" }

		custom.removeMod(item, mod)

		sender.information("Removed ${mod.identifier}")
	}
}
