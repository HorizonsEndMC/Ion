package net.horizonsend.ion.server.command.admin

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock.Companion.getModel
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@CommandAlias("ion")
@CommandPermission("ion.utilities")
object IonCommand : SLCommand() {
	val debugEnabledPlayers = mutableListOf<Audience>()

	@Suppress("Unused")
	@Subcommand("view set")
	fun setServerViewDistance(sender: CommandSender, renderDistance: Int) {
		if (renderDistance > 32) {
			sender.userError("View distances above 32 are not supported.")
			return
		}

		if (renderDistance < 2) {
			sender.userError("View distances below 2 are not supported.")
			return
		}

		for (world in Bukkit.getWorlds()) {
			world.viewDistance = renderDistance
		}

		sender.success("View distance set to $renderDistance.")
	}

	@Suppress("Unused")
	@Subcommand("simulation set")
	fun setServerSimulationDistance(sender: CommandSender, simulationDistance: Int) {
		if (simulationDistance > 32) {
			sender.userError("Simulation distances above 32 are not supported.")
			return
		}

		if (simulationDistance < 2) {
			sender.userError("Simulation distances below 2 are not supported.")
			return
		}

		for (world in Bukkit.getWorlds()) {
			world.viewDistance = simulationDistance
		}

		sender.success("Simulation distance set to $simulationDistance.")
	}

	@Suppress("Unused")
	@Subcommand("view get")
	fun getServerViewDistance(sender: CommandSender) {
		sender.information("View distance is currently set to ${Bukkit.getWorlds()[0].viewDistance}.")
	}

	@Suppress("Unused")
	@Subcommand("simulation get")
	fun getServerSimulationDistance(sender: CommandSender) {
		sender.information("Simulation distance is currently set to ${Bukkit.getWorlds()[0].simulationDistance}.")
	}

	@Suppress("Unused")
	@Subcommand("debug")
	fun debugToggle(sender: Player) {
		if (debugEnabledPlayers.contains(sender)) {
			debugEnabledPlayers.remove(sender)
			sender.success("Disabled debug mode")
		} else {
			debugEnabledPlayers.add(sender)
			sender.success("Enabled debug mode")
		}
	}
	@Suppress("Unused")
	@Subcommand("display")
	@CommandCompletion("@multiblocks")
	fun spawnEntity(sender: Player, multiblock: Multiblock) {
		val item = multiblock.getModel()
		val namespacedKey = NamespacedKey(item.namespace(), item.value())

		val itemStack = ItemStack(Material.PAPER).apply {
			editMeta { meta ->
				meta.setItemModel(namespacedKey)
			}
		}

		val location = sender.location

		val entity = location.world.spawn(location, ItemDisplay::class.java) { display ->
			display.setItemStack(itemStack)
			display.viewRange = 5.0f
			display.brightness = Display.Brightness(15, 15)
			display.interpolationDuration = 0
			display.itemDisplayTransform = ItemDisplay.ItemDisplayTransform.FIXED
		}
	}
}

fun Audience.debugBanner(message: String) = debug("------------------- $message -------------------")
fun Audience.debug(message: String): Unit = when (this) {
	is Player -> if (IonCommand.debugEnabledPlayers.contains(this)) information(message) else {}

	is ForwardingAudience -> audiences().filter { IonCommand.debugEnabledPlayers.contains(it) }.forEach{ _ -> information(message) }

	else -> {}
}

fun Audience.debugRed(message: String): Unit = when (this) {
	is Player -> if (IonCommand.debugEnabledPlayers.contains(this)) serverError(message) else {}

	is ForwardingAudience -> audiences().filter { IonCommand.debugEnabledPlayers.contains(it) }.forEach{ _ -> serverError(message) }

	else -> {}
}
