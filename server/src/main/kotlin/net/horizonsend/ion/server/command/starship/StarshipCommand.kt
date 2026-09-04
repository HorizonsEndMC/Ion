package net.horizonsend.ion.server.command.starship

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.starship.Interdiction
import net.horizonsend.ion.server.features.starship.StarshipDetection
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.features.starship.subsystem.shield.ShieldSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.AbstractCooldown
import net.horizonsend.ion.server.miscellaneous.utils.actualType
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.isConcrete
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand
import net.minecraft.core.BlockPos
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.round
import kotlin.math.roundToInt
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.CHETHERITE
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import kotlin.collections.set

@CommandAlias("starship|starshipinfo")
object StarshipCommand : net.horizonsend.ion.server.command.SLCommand() {
	@Default
	@Subcommand("info")
	fun onExecute(sender: Player) {
		val ship = getStarshipPiloting(sender)

		val blocks = ship.blocks.map { Vec3i(it) }.associateWith { it.toLocation(ship.world).block.state }

		val size = ship.initialBlockCount

		sender.sendRichMessage(
			"<aqua>${ship.getDisplayNameMiniMessage()} <white>(${
				ship.data.starshipType.actualType.displayName
			}) ($size blocks)\n" +
				"   <gray>Mass:<white> ${"%.2f".format(ship.mass)}\n" +
				"   <gray>World:<white> ${ship.world.name}\n" +
				"   <gray>Pilot:<white> ${ship.controller.name}"
		)

		val passengers = ship.onlinePassengers.joinToString { it.name }
		if (passengers.any()) {
			sender.sendRichMessage("   <gray>Passengers: <white>$passengers")
		}

		sender.sendRichMessage(
			"   <gray>Concrete Percent: <white>${
				createPercent(
					blocks.values.count { it.type.isConcrete },
					size
				)
			}"
		)

		val inventoryCount =
			blocks.values.count { StarshipDetection.isInventory(it.type) } +
				blocks.values.count { it.type == Material.CHEST || it.type == Material.TRAPPED_CHEST } * 2

		sender.sendRichMessage("   <gray>Inventory Percent: <white>${createPercent(inventoryCount, size)}")

		val hyperdrive = Hyperspace.findHyperdrive(ship)
		if (hyperdrive != null) {
			val hyperdriveClass = hyperdrive.multiblock.hyperdriveClass
			val vector = hyperdrive.pos
			sender.sendRichMessage("   <gray>Hyperdrive: <white>Class $hyperdriveClass at $vector")
		}

		if (!ship.weaponSets.isEmpty) {
			sender.sendRichMessage("   <gray>Controlled Weapon Sets:")

			for (gunner in ship.weaponSetSelections.mapNotNull { Bukkit.getPlayer(it.key) }) {
				val weaponSet = ship.weaponSetSelections[gunner.uniqueId]

				sender.sendRichMessage("         <gold>${gunner.name}: <red>$weaponSet")
			}
		}

		val powerOutput = ship.reactor.output
		sender.sendRichMessage(
			"   <gray>Power Output: <white>${"%.2f".format(powerOutput)}\n" +
				"   <gray>Weapon Capacitor Capacity: <white>${"%.2f".format(ship.reactor.weaponCapacitor.capacity)}\n" +
				"   <gray>Heavy Weapon Booster Output: <white>${"%.2f".format(ship.reactor.heavyWeaponBooster.output)}"
		)

		sender.sendRichMessage("   <gray>Power Division:")
		val powerTypes = listOf(
			"Shield" to ship.reactor.powerDistributor.shieldPortion,
			"Weapon" to ship.reactor.powerDistributor.weaponPortion,
			"Thruster" to ship.reactor.powerDistributor.thrusterPortion
		)

		for ((name, percent) in powerTypes) {
			val percentRounded = (percent * 100).roundToInt()
			val currentPower = "%.2f".format(percent * powerOutput)
			sender.sendRichMessage("      <gray>$name: <yellow>$percentRounded% ($currentPower)")
		}

		if (ship.autoTurretTargets.isNotEmpty()) {
			sender.sendRichMessage("   <gray>Auto Turret Targets:")

			for ((set, target) in ship.autoTurretTargets) {
				val targetName = target.identifier

				sender.sendRichMessage("      <gold>$set: <red>$targetName")
			}
		}

		if (ship.shields.isNotEmpty()) {
			sender.sendRichMessage("   <gray>Shields:")

			for (shield in ship.shields) {
				val percent = createPercent(shield.power, shield.maxPower)
				val (x, y, z) = shield.pos
				val shieldClass = (getBlockIfLoaded(ship.world, x, y, z)?.state as? Sign)?.line(3)

				val shieldName = miniMessage().serialize(legacyAmpersand().deserialize(shield.name))

				sender.sendMessage(
					Component.textOfChildren(
						text("$shieldName: ", NamedTextColor.GRAY),
						text("$percent (", NamedTextColor.AQUA),
						shieldClass ?: text(""),
						text(")", NamedTextColor.AQUA)
					)
				)
			}

			sender.sendRichMessage("   <gray>Shield Regen Efficiency: <aqua>${ship.shieldEfficiency}")
			sender.sendRichMessage("   <gray>Maximum Shields the starship can handle: <aqua>${ship.maxShields}")
		}

		sender.sendRichMessage("   <gray>Hull Integrity: <white>${ship.hullIntegrity.times(100).roundToInt()}%")
		sender.sendRichMessage("   <gray>Center of Mass: <white>${ship.centerOfMass}")
		sender.sendRichMessage(
			"   <gray>Interdiction Range: <white>${
				Interdiction.starshipInterdictionRangeEquation(
					ship
				).toInt()
			}"
		)

//		val worth = blocks.values
//			.sumOf { StarshipFactories.getPrice(it.blockData) ?: 0.0 }
//			.roundToInt()
//
//		sender.sendRichMessage("   <gray>Worth: <white>~$worth")
	}

	// creates a percent that goes down to the tens place
	private fun createPercent(numerator: Int, denominator: Int): String =
		createPercent(numerator.toDouble() / denominator.toDouble())

	private fun createPercent(fraction: Double) = "${round(fraction * 1000) / 10}%"

	val cooldown = object : AbstractCooldown<UUID>(10L, TimeUnit.SECONDS) {
		override fun cooldownRejected(player: UUID) {
			Bukkit.getPlayer(player)?.userError("You're doing that too often!")
		}
	}

	@Subcommand("shields")
	fun onDisplayShields(sender: Player) {
		val ship = getStarshipPiloting(sender)

		cooldown.tryExec(sender.uniqueId) {
			for ((index, subsystem) in ship.shields.withIndex()) {
				// exit if too many shields will be rendered
				if (index >= 30) return@tryExec

				val multiblock = subsystem.multiblock
				val sign = (subsystem.pos.toLocation(ship.world).block.state as? Sign) ?: continue
				multiblock.displayShieldCoverage(sign)
			}
		}
	}

	@Subcommand("download")
	fun onDownload(sender: Player) = MiscStarshipCommands.onDownload(sender)

	@Subcommand("sell")
	fun onSell(
		sender: Player,
		className: String,
		shipName: String,
		price: Double,
		@Optional priceConfirm: Double?,
		@Optional description: String?
	) = SellStarshipCommand.onSellStarship(sender, className, shipName, price, priceConfirm, description)

	@Subcommand("ammo")
	fun getAmmo(sender: Player) {
		val starship = getStarshipPiloting(sender)
		val counts = mutableMapOf<Component, Int>()

		starship.magazines.forEach { magazine ->
			val inventory = magazine.getInventoryPublic()
			if (inventory != null) {
				for (item in inventory.filterNotNull()) {
					val name = item.customItem?.displayName ?: item.displayName()
					counts[name] = counts.getOrDefault(name, 0) + item.amount
				}
			}
		}

		sender.sendRichMessage("<dark_gray><bold>=====================================")

		val message = counts.entries.joinToString("\n") { (name, amount) ->
			val plainName = name.plainText()
			val hexColor = String.format("%06X", plainName.lowercase().hashCode() and 0xFFFFFF)
			"<#$hexColor>$plainName: <white>$amount</white>"

		}
		sender.sendRichMessage(message)
		sender.sendRichMessage("<dark_gray><bold>=====================================")
	}

	@Subcommand("jumps")
	fun getRemainingJumps(sender: Player) {
		val starship = getStarshipPiloting(sender)
		val amounts = mutableListOf<Int>()

		starship.hyperdrives.forEach { hyperdrive ->
			val inventory = hyperdrive.getFuelInventories().forEach { inventory ->
				val total = inventory.inventory.asSequence()
					.filterNotNull()
					.filter { it.customItem?.key == CHETHERITE }
					.sumOf { it.amount }
				amounts.add(total)
			}
			val minAmmount = amounts.minOrNull() ?: 0
			val maxjumps = minAmmount.div(Hyperspace.getHyperMatterAmount(starship))
			sender.sendRichMessage("<gray>You have enough <light_purple><b>Chetherite</b></light_purple> <reset> <gray>for (<white>$maxjumps<gray>) jumps")
		}
	}

	@Subcommand("Diagnostics")
	fun shipDiagnostics(sender: Player) {
		val starship = getStarshipPiloting(sender)
		val brokenCounts = mutableMapOf<String, Int>()
		val intactCounts = mutableMapOf<String, Int>()

		starship.subsystems.forEach { subsystem ->
			val inTact = subsystem.isIntact()
			val message = subsystem.toString()
			val simplerName = message.substringAfterLast('.').substringBefore('@')
			if (!inTact) {
				brokenCounts[simplerName] = brokenCounts.getOrDefault(simplerName, 0) + 1
			} else {
				intactCounts[simplerName] = intactCounts.getOrDefault(simplerName, 0) + 1
			}
		}

		sender.sendRichMessage("<dark_gray><bold>=====================================")

			val allCounts = brokenCounts.keys + intactCounts.keys

			allCounts.forEach { name ->
				val broken = brokenCounts.getOrDefault(name, 0)
				val intact = intactCounts.getOrDefault(name, 0)
				val total = broken + intact

				val percent = intact.toFloat() / total.toFloat()

				val color = when {
					percent >= 1.0 -> "<green>"
					percent >= 0.75 -> "<yellow>"
					percent >= 0.5 -> "<gold>"
					percent >= 0.05 -> "<red>"
					percent < 0.05 -> "<gray>"
					else -> "<I have no idea how u achieved this but props man>"
				}
				sender.sendRichMessage("$color$name</${color.removePrefix("<")}: <white>$intact/$total")
			}

			sender.sendRichMessage("<dark_gray><bold>=====================================")
		}


	@Subcommand("coverage")
	fun onHullInfo(sender: Player) {
		val ship = getStarshipPiloting(sender)
		val mapOfBlockPosToShields = mutableMapOf<BlockPos, MutableList<ShieldSubsystem>>()
		ship.blocks.forEach {
			val pos = BlockPos(blockKeyX(it), blockKeyY(it), blockKeyZ(it))
			val vec3iPos = pos.toVec3i()
			mapOfBlockPosToShields[pos] = mutableListOf()
			for (shield in ship.shields) {
				if (shield.isIntact() && shield.containsPosition(ship.world, vec3iPos)) {
					val newList =
						mapOfBlockPosToShields[pos]
					newList?.add(shield)
					mapOfBlockPosToShields[pos] = newList ?: mutableListOf(shield)
				}
			}
		}

		/*
		tally of shield to number of blocks that are
		A. Without shield
		B. Overlapping
		C. Total Block count singular.
		 */

		val mapOfShieldToBlocksItOnlyContains = mutableMapOf<ShieldSubsystem, Int>()
		val mapOfShieldToBlocksItOverlapsWith = mutableMapOf<ShieldSubsystem, Int>()
		val totalBlocksNotShielded = mapOfBlockPosToShields.count { it.value.isEmpty() }

		for ((block, shields) in mapOfBlockPosToShields) {
			if (shields.count() == 1) {
				mapOfShieldToBlocksItOnlyContains[shields.first()] =
					(mapOfShieldToBlocksItOnlyContains[shields.first()] ?: 0) + 1
			} else {
				for (shield in shields) {
					mapOfShieldToBlocksItOverlapsWith[shield] = (mapOfShieldToBlocksItOverlapsWith[shield] ?: 0) + 1
				}
			}
		}

		sender.sendRichMessage("<dark_gray><bold>=====================================")
		sender.sendRichMessage("<gray><bold>Shield Coverage:")
		if (totalBlocksNotShielded != 0) {
			sender.sendRichMessage("<red><bold>Total Blocks not shielded: $totalBlocksNotShielded</red>")
		} else {
			sender.sendRichMessage("<green><bold>All Blocks Shielded!")
		}
		for (shield in ship.shields) {
			if (!shield.isIntact()) {
				sender.sendRichMessage("<red><bold>Shield: ${shield.name},Not Intact!</bold><dark_gray>[${shield.pos.x}, ${shield.pos.y}, ${shield.pos.z}]")
				continue
			}
			val totalBlocksCovered =
				(mapOfShieldToBlocksItOverlapsWith[shield] ?: 0).plus(mapOfShieldToBlocksItOnlyContains[shield] ?: 0)
			sender.sendRichMessage("<gray>Shield: <aqua>${shield.name} <dark_gray>[${shield.pos.x}, ${shield.pos.y}, ${shield.pos.z}]")
			sender.sendRichMessage("<gray>   Total Blocks Covered: <white>$totalBlocksCovered")
			sender.sendRichMessage("<gray>   Total Overlapped Blocks: <white>${mapOfShieldToBlocksItOverlapsWith[shield] ?: 0}")
			sender.sendRichMessage("<gray>   Total Blocks covered only by this shield: <white>${mapOfShieldToBlocksItOnlyContains[shield] ?: 0}")
			sender.sendRichMessage("<gray>   Total Hull Percentage Covered: <white>${(totalBlocksCovered.toFloat() / ship.initialBlockCount.toFloat())}")

		}
		sender.sendRichMessage("<dark_gray><bold>=====================================")
	}
}



