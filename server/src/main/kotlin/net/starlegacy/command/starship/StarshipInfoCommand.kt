package net.starlegacy.command.starship

import co.aikar.commands.annotation.CommandAlias
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand
import net.starlegacy.command.SLCommand
import net.starlegacy.feature.starship.StarshipDetection
import net.starlegacy.feature.starship.factory.StarshipFactories
import net.starlegacy.feature.starship.hyperspace.Hyperspace
import net.starlegacy.util.Vec3i
import net.starlegacy.util.isConcrete
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import kotlin.math.round
import kotlin.math.roundToInt

object StarshipInfoCommand : SLCommand() {
	@Suppress("Unused")
	@CommandAlias("starshipinfo|starship")
	fun onExecute(p: Player) {
		val ship = getStarshipPiloting(p)

		val blocks = ship.blocks.map { Vec3i(it) }.associateWith { it.toLocation(ship.serverLevel.world).block.state }

		val size = ship.initialBlockCount

		p.sendRichMessage(
			"<aqua>${ship.data.name} <white>(${ship.data.starshipType.displayName}) ($size blocks)\n" +
				"   <gray>Mass:<white> ${ship.mass}\n" +
				"   <gray>World:<white> ${ship.serverLevel.world.name}\n" +
				"   <gray>Pilot:<white> ${ship.pilot?.name}"
		)

		val passengers = ship.onlinePassengers.map { it.name }.joinToString()
		if (passengers.any()) {
			p.sendRichMessage("   <gray>Passengers: <white>$passengers")
		}

		p.sendRichMessage(
			"   <gray>Carbyne Percent: <white>${createPercent(blocks.values.count { it.type.isConcrete }, size)}"
		)

		val inventoryCount = blocks.values.count { StarshipDetection.isInventory(it.type) } +
			blocks.values.count { it.type == Material.CHEST || it.type == Material.TRAPPED_CHEST } * 2
		p.sendRichMessage("   <gray>Inventory Percent: <white>${createPercent(inventoryCount, size)}")
		val hyperdrive = Hyperspace.findHyperdrive(ship)
		if (hyperdrive != null) {
			val hyperdriveClass = hyperdrive.multiblock.hyperdriveClass
			val vector = hyperdrive.pos
			p.sendRichMessage("   <gray>Hyperdrive: <white>Class $hyperdriveClass at $vector")
		}
		if (!ship.weaponSets.isEmpty) {
			p.sendRichMessage("   <gray>Controlled Weapon Sets:")
			for (gunner in ship.weaponSetSelections.mapNotNull { Bukkit.getPlayer(it.key) }) {
				val weaponSet = ship.weaponSetSelections[gunner.uniqueId]
				p.sendRichMessage("         <gold>${gunner.name}: <red>$weaponSet")
			}
		}
		val powerOutput = ship.reactor.output
		p.sendRichMessage(
			"   <gray>Power Output: <white>$powerOutput\n" +
				"   <gray>Weapon Capacitor Capacity: <white>${ship.reactor.weaponCapacitor.capacity}\n" +
				"   <gray>Heavy Weapon Booster Output: <white>${ship.reactor.heavyWeaponBooster.output}"
		)

		p.sendRichMessage("   <gray>Power Division:")
		val powerTypes = listOf(
			"Shield" to ship.reactor.powerDistributor.thrusterPortion,
			"Weapon" to ship.reactor.powerDistributor.weaponPortion,
			"Thruster" to ship.reactor.powerDistributor.thrusterPortion
		)
		for ((name, percent) in powerTypes) {
			val percentRounded = (percent * 100).roundToInt()
			val currentPower = percent * powerOutput
			p.sendRichMessage("      <gray>$name: <yellow>$percentRounded% ($currentPower)")
		}

		if (ship.autoTurretTargets.isNotEmpty()) {
			p.sendRichMessage("   <gray>Auto Turret Targets:")
			for ((set, targetId) in ship.autoTurretTargets) {
				val targetName = Bukkit.getOfflinePlayer(targetId).name
				p.sendRichMessage("      <gold>$set: <red>$targetName")
			}
		}

		if (ship.shields.isNotEmpty()) {
			p.sendRichMessage("   <gray>Shields:")
			for (shield in ship.shields) {
				val percent = createPercent(shield.power, shield.maxPower)
				val shieldClass = shield.multiblock.signText[3]?.let { legacyAmpersand().serialize(it) }
				p.sendRichMessage("      <gray>${shield.name}: <aqua>$percent ($shieldClass)")
			}
			p.sendRichMessage("   <gray>Shield Regen Efficiency: <aqua>${ship.shieldEfficiency}")
			p.sendRichMessage("   <gray>Maximum Shields the starship can handle: <aqua>${ship.maxShields}")
		}

		p.sendRichMessage("   <gray>Hull Integrity: <white>${ship.hullIntegrity().times(100).roundToInt()}%")
		p.sendRichMessage("   <gray>Center of Mass: <white>${ship.centerOfMassVec3i}")

		val worth = blocks.values
			.sumOf { StarshipFactories.getPrice(it.blockData) ?: 0.0 }
			.roundToInt()

		p.sendRichMessage("   <gray>Worth: <white>~$worth")
	}

	// creates a percent that goes down to the tens place
	private fun createPercent(numerator: Int, denominator: Int): String =
		createPercent(numerator.toDouble() / denominator.toDouble())

	private fun createPercent(fraction: Double) = "${round(fraction * 1000) / 10}%"
}
