package net.horizonsend.ion.server.command.starship

import co.aikar.commands.annotation.CommandAlias
import net.horizonsend.ion.server.features.starship.StarshipDetection
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.actualType
import net.horizonsend.ion.server.miscellaneous.utils.isConcrete
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import kotlin.math.round
import kotlin.math.roundToInt

object StarshipInfoCommand : net.horizonsend.ion.server.command.SLCommand() {
	@Suppress("Unused")
	@CommandAlias("starshipinfo|starship")
	fun onExecute(sender: Player) {
		val ship = getStarshipPiloting(sender)

		val blocks = ship.blocks.map { Vec3i(it) }.associateWith { it.toLocation(ship.world).block.state }

		val size = ship.initialBlockCount

		sender.sendRichMessage(
			"<aqua>${ship.getDisplayName()} <white>(${
				ship.data.starshipType.actualType.displayName}) ($size blocks)\n" +
				"   <gray>Mass:<white> ${"%.2f".format(ship.mass)}\n" +
				"   <gray>World:<white> ${ship.world.name}\n" +
				"   <gray>Pilot:<white> ${ship.controller.name}"
		)

		val passengers = ship.onlinePassengers.map { it.name }.joinToString()
		if (passengers.any()) {
			sender.sendRichMessage("   <gray>Passengers: <white>$passengers")
		}

		sender.sendRichMessage("   <gray>Concrete Percent: <white>${createPercent(blocks.values.count { it.type.isConcrete }, size)}")

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
			"Shield" to ship.reactor.powerDistributor.thrusterPortion,
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
				val shieldClass = shield.multiblock.signText[3]?.let { miniMessage().serialize(it) }

				val shieldName = miniMessage().serialize(legacyAmpersand().deserialize(shield.name))

				sender.sendRichMessage("      <gray>$shieldName: <aqua>$percent ($shieldClass)")
			}

			sender.sendRichMessage("   <gray>Shield Regen Efficiency: <aqua>${ship.shieldEfficiency}")
			sender.sendRichMessage("   <gray>Maximum Shields the starship can handle: <aqua>${ship.maxShields}")
		}

		sender.sendRichMessage("   <gray>Hull Integrity: <white>${ship.hullIntegrity().times(100).roundToInt()}%")
		sender.sendRichMessage("   <gray>Center of Mass: <white>${ship.centerOfMass}")

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
}
